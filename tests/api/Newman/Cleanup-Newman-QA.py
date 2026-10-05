"""Standalone SQLite teardown and PythonAnywhere client; Python standard library only."""
import argparse
import base64
import hashlib
import hmac
import json
import re
import shlex
import sqlite3
import sys
import time
import uuid
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import quote, urlencode
from urllib.request import Request, urlopen


TABLES = ('contactos', 'campanias', 'usuarios', 'centros_salud', 'inscripciones')


def identifier(name):
    return '"' + name.replace('"', '""') + '"'


def verify_hash(encoded, password='Abc12!7890'):
    """Verify the fixture without Django or exposing the stored value."""
    if not encoded or encoded == password:
        return False
    parts = encoded.split('$')
    if len(parts) == 4 and parts[0] in ('pbkdf2_sha256', 'pbkdf2_sha1'):
        algorithm, iterations, salt, expected = parts
        calculated = hashlib.pbkdf2_hmac(algorithm.removeprefix('pbkdf2_'), password.encode(), salt.encode(), int(iterations))
        return hmac.compare_digest(base64.b64encode(calculated).decode(), expected)
    if len(parts) == 6 and parts[0] == 'scrypt':
        _, work, salt, block, parallel, expected = parts
        calculated = hashlib.scrypt(password.encode(), salt=salt.encode(), n=int(work), r=int(block), p=int(parallel), maxmem=128 * 1024 * 1024, dklen=64)
        return hmac.compare_digest(base64.b64encode(calculated).decode(), expected)
    raise ValueError('El algoritmo del fixture no está contemplado por la inspección independiente AUTH-14.')


def check_database(connection):
    present = {row[0] for row in connection.execute("SELECT name FROM sqlite_master WHERE type='table'")}
    missing = set(TABLES) - present
    if missing:
        raise ValueError('La base seleccionada no tiene las tablas de SangreYa: ' + ', '.join(sorted(missing)))
    return present


def cleanup_run(run_id, connection):
    if not re.fullmatch(r'\d{19}', run_id):
        raise ValueError('runId debe tener exactamente 19 dígitos.')
    tables = check_database(connection)
    connection.execute('PRAGMA foreign_keys=ON')
    if connection.in_transaction:
        raise ValueError('La limpieza requiere una conexión sin transacción previa.')
    selectors = {
        'contactos': ('(correo_electronico LIKE ? AND correo_electronico LIKE ? AND correo_electronico LIKE ?) OR nombre_completo LIKE ?', ['%' + run_id + '%', '%@example.test', 'qa%', 'QA Newman ' + run_id + '%']),
        'campanias': ('titulo LIKE ? AND (titulo LIKE ? OR titulo LIKE ?)', ['%' + run_id + '%', 'QA %', 'Campaña %']),
        'usuarios': ('email LIKE ? AND email LIKE ? AND email LIKE ?', ['%' + run_id + '%', '%@example.test', 'qa%']),
        'centros_salud': ('nombre LIKE ?', ['QA Newman ' + run_id + '%']),
    }
    labels = {'contactos': 'contacts', 'campanias': 'campaigns', 'usuarios': 'users', 'centros_salud': 'healthCenters'}
    foreign_keys = []
    primary_keys = {}
    for table in tables:
        info = list(connection.execute('PRAGMA table_info(' + identifier(table) + ')'))
        keys = [row[1] for row in info if row[5]]
        if len(keys) == 1:
            primary_keys[table] = keys[0]
        for fk in connection.execute('PRAGMA foreign_key_list(' + identifier(table) + ')'):
            foreign_keys.append((table, fk[3], fk[2], fk[4]))
    related = {}
    visiting = set()

    def delete_rows(table, ids):
        ids = list(ids)
        if not ids:
            return
        if table not in primary_keys:
            raise ValueError('No se puede identificar la clave primaria de ' + table)
        key = (table, tuple(sorted(ids)))
        if key in visiting:
            raise ValueError('Relación circular al limpiar ' + table)
        visiting.add(key)
        placeholders = ','.join('?' for _ in ids)
        for child, column, parent, target in foreign_keys:
            if parent != table:
                continue
            pk = primary_keys.get(child)
            if not pk:
                raise ValueError('Clave primaria no simple en ' + child)
            children = [row[0] for row in connection.execute('SELECT ' + identifier(pk) + ' FROM ' + identifier(child) + ' WHERE ' + identifier(column) + ' IN (' + placeholders + ')', ids)]
            if table == 'centros_salud' and children:
                raise ValueError('Centro QA referenciado por datos ajenos al conjunto de esta ejecución.')
            if children:
                related[child] = related.get(child, 0) + len(children)
                delete_rows(child, children)
        connection.execute('DELETE FROM ' + identifier(table) + ' WHERE ' + identifier(primary_keys[table]) + ' IN (' + placeholders + ')', ids)
        visiting.remove(key)

    try:
        connection.execute('BEGIN IMMEDIATE')
        selected = {table: [row[0] for row in connection.execute('SELECT id FROM ' + identifier(table) + ' WHERE ' + condition, params)] for table, (condition, params) in selectors.items()}
        hash_row = connection.execute('SELECT password FROM usuarios WHERE email=?', ['qa+hash-' + run_id + '@example.test']).fetchone()
        hash_check = verify_hash(hash_row[0]) if hash_row else None
        # ORM/API deletion may already have detached these token rows from the user.
        token_table = 'token_blacklist_outstandingtoken'
        if token_table in tables:
            token_ids = []
            for token_id, token in connection.execute('SELECT id, token FROM ' + token_table):
                try:
                    payload = token.split('.')[1]
                    payload += '=' * (-len(payload) % 4)
                    email = json.loads(base64.urlsafe_b64decode(payload)).get('email', '')
                except (ValueError, IndexError, AttributeError):
                    continue
                if email.startswith('qa') and email.endswith('@example.test') and run_id in email:
                    token_ids.append(token_id)
            delete_rows(token_table, token_ids)
        for table in ('contactos', 'campanias', 'usuarios', 'centros_salud'):
            delete_rows(table, selected[table])
        for table, (condition, params) in selectors.items():
            if connection.execute('SELECT 1 FROM ' + identifier(table) + ' WHERE ' + condition + ' LIMIT 1', params).fetchone():
                raise ValueError('Quedaron registros de la ejecución en ' + table)
        connection.commit()
        return {'deleted': {labels[table]: len(ids) for table, ids in selected.items()}, 'related': related, 'hashInspectionAUTH14': hash_check, 'cleanupComplete': True}
    except Exception:
        connection.rollback()
        raise


def run_database(path, run_id, check=False):
    target = Path(path).resolve()
    if not target.is_file():
        raise ValueError('No existe la base SQLite configurada: ' + str(target))
    uri = target.as_uri() + ('?mode=ro' if check else '?mode=rw')
    with sqlite3.connect(uri, uri=True, timeout=30) as connection:
        if check:
            check_database(connection)
            return {'ready': True, 'database': str(target)}
        return {'runId': run_id, **cleanup_run(run_id, connection)}


class PythonAnywhereClient:
    def __init__(self, config):
        for key in ('username', 'apiToken', 'consoleId', 'databasePath'):
            if not config.get(key):
                raise ValueError('Completar ' + key + ' en Cleanup-Connection.private.json. Token: https://www.pythonanywhere.com/account/#api_token; consoleId: número de la URL de la consola Bash abierta.')
        self.config = config
        self.api = config.get('apiHost', 'https://www.pythonanywhere.com').rstrip('/') + '/api/v0/user/' + quote(config['username'], safe='')

    def request(self, route, method='GET', data=None, content_type=None):
        headers = {'Authorization': 'Token ' + self.config['apiToken']}
        if isinstance(data, dict):
            data = urlencode(data).encode()
            content_type = 'application/x-www-form-urlencoded'
        if content_type:
            headers['Content-Type'] = content_type
        request = Request(self.api + route, data=data, headers=headers, method=method)
        try:
            with urlopen(request, timeout=30) as result:
                raw = result.read()
                return json.loads(raw) if raw else {}
        except HTTPError as error:
            raise ValueError('PythonAnywhere API respondió HTTP ' + str(error.code) + ' en ' + route) from error

    def execute(self, run_id=None, check=False):
        console = '/consoles/' + str(int(self.config['consoleId'])) + '/'
        metadata = self.request(console)
        if 'bash' not in str(metadata.get('executable', '')):
            raise ValueError('El ID configurado debe corresponder a una consola Bash.')
        remote_script = '/home/' + self.config['username'] + '/.newman-qa/Cleanup-Newman-QA.py'
        boundary = 'newman' + uuid.uuid4().hex
        content = (('--' + boundary + '\r\nContent-Disposition: form-data; name="content"; filename="Cleanup-Newman-QA.py"\r\nContent-Type: text/plain\r\n\r\n').encode() + Path(__file__).read_bytes() + ('\r\n--' + boundary + '--\r\n').encode())
        self.request('/files/path' + quote(remote_script, safe='/'), 'POST', content, 'multipart/form-data; boundary=' + boundary)
        marker = '__NEWMAN_' + uuid.uuid4().hex + '__'
        args = [self.config.get('pythonCommand', 'python3'), remote_script, '--database-path', self.config['databasePath'], '--completion-marker', marker]
        args += ['--check'] if check else ['--run-id', run_id]
        command = ' '.join(shlex.quote(value) for value in args) + '\n'
        self.request(console + 'send_input/', 'POST', {'input': command})
        deadline = time.monotonic() + 120
        while time.monotonic() < deadline:
            time.sleep(2)
            output = self.request(console + 'get_latest_output/').get('output', '')
            for line in output.splitlines():
                position = line.find(marker + '{')
                if position < 0:
                    continue
                try:
                    result = json.JSONDecoder().raw_decode(line[position + len(marker):])[0]
                except ValueError:
                    continue
                if result.get('error'):
                    raise ValueError(result['error'])
                if check and not result.get('ready'):
                    raise ValueError('La consola no confirmó acceso al dataset QA.')
                if not check and not result.get('cleanupComplete'):
                    raise ValueError('La limpieza no confirmó finalización.')
                return result
        raise ValueError('La consola no devolvió el resultado. Abrir la consola Bash configurada en el navegador y comprobar que esté esperando comandos.')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--run-id')
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--connection-file', default=str(Path(__file__).with_name('Cleanup-Connection.private.json')))
    parser.add_argument('--database-path', help='Modo independiente en el host donde reside SQLite; no requiere Django')
    parser.add_argument('--completion-marker', default='')
    args = parser.parse_args()
    if not args.check and not args.run_id:
        parser.error('--run-id es obligatorio para limpiar')
    try:
        if args.database_path:
            result = run_database(args.database_path, args.run_id, args.check)
        else:
            path = Path(args.connection_file)
            if not path.is_file():
                raise ValueError('Falta ' + str(path) + '; copiar Cleanup-Connection.template.json y completar apiToken y consoleId.')
            result = PythonAnywhereClient(json.loads(path.read_text(encoding='utf-8-sig'))).execute(args.run_id, args.check)
        print(args.completion_marker + json.dumps(result, ensure_ascii=True))
        return 1 if result.get('hashInspectionAUTH14') is False else 0
    except Exception as error:
        print(args.completion_marker + json.dumps({'error': str(error)}, ensure_ascii=True))
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
