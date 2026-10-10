"""Sanitize public delivery reports without re-running tests or changing results."""

import argparse
import fnmatch
import hashlib
import json
from pathlib import Path, PureWindowsPath
import re
import sys


ROOT = Path(__file__).resolve().parent.parent
REPORT_ROOTS = (ROOT / 'tests/jvm/reportes', ROOT / 'tests/api/Newman/reportes')
TEXT_SUFFIXES = {'.html', '.xml', '.json', '.md', '.txt', '.css', '.js', '.yaml', '.yml'}
LOCAL_PATTERNS = (
    '*.exec', '*.ec', '*.runtime.postman_collection.json', '*.private.*',
    '*.postman_environment.json', '*.log', '*.bak', '*.tmp', '*.orig', '*.rej', '*~',
)
HOSTNAME = re.compile(r'\s+hostname=([\'"])[^\'"]*\1')
SESSION_XML = re.compile(r'(<sessioninfo\b[^>]*\bid=)([\'"])([^\'"]+)\2')
SESSION_HTML = re.compile(r'(<td><span class="el_session">)([^<]+)(</span></td>)')
PRIVACY_PATTERNS = (
    ('absolute Windows path', re.compile(r'\b[A-Za-z]:[\\/]')),
    ('absolute user/home path', re.compile(r'/(?:home|Users)/[^\s<>"\']+')),
    ('JWT', re.compile(r'\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+')),
    ('private key', re.compile(r'-----BEGIN (?:[A-Z]+ )?PRIVATE KEY-----')),
    ('GitHub token', re.compile(r'\b(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,})')),
)


def is_public(path):
    return not any(fnmatch.fnmatch(path.name, pattern) for pattern in LOCAL_PATTERNS)


def newline_for(data):
    return '\r\n' if b'\r\n' in data else '\n'


def replace_json_fields(text, values):
    """Replace only known metadata fields, preserving formatting and other data."""
    for key, value in values.items():
        pattern = re.compile(r'("' + re.escape(key) + r'"\s*:\s*)("(?:\\.|[^"\\])*"|null)')
        text = pattern.sub(lambda match: match[1] + json.dumps(value, ensure_ascii=False), text, count=1)
    return text


def sanitize_metadata(path, text):
    if path.parent != REPORT_ROOTS[0]:
        return text
    if path.name not in ('ejecucion.json', 'planilla-actualizada.json'):
        return text
    metadata = json.loads(text)
    values = {}
    if 'excel' in metadata and isinstance(metadata['excel'], str):
        values['excel'] = PureWindowsPath(metadata['excel']).name
    if path.name == 'ejecucion.json':
        if 'project' in metadata:
            values['project'] = '.'
        if 'folder' in metadata:
            values['folder'] = 'tests/jvm/reportes'
    elif 'backup' in metadata:
        values['backup'] = None
    return replace_json_fields(text, values)


def report_files(report_root):
    return sorted(path for path in report_root.rglob('*') if path.is_file() and is_public(path))


def canonical_bytes(path, data):
    # Git may convert line endings on checkout; integrity must remain portable.
    return data.replace(b'\r\n', b'\n') if path.suffix.lower() in TEXT_SUFFIXES else data


def prepare_changes():
    changes = {}
    problems = []
    for report_root in REPORT_ROOTS:
        files = report_files(report_root)
        sessions = {}
        texts = {}
        for path in files:
            if path.name == 'sha256.json' or path.suffix.lower() not in TEXT_SUFFIXES:
                continue
            text = path.read_bytes().decode('utf-8-sig')
            texts[path] = text
            for match in SESSION_XML.finditer(text):
                session = match[3]
                if session not in sessions:
                    sessions[session] = 'qa-session-{:03d}'.format(len(sessions) + 1)
        for path, text in texts.items():
            original = path.read_bytes()
            sanitized = sanitize_metadata(path, text)
            if path.suffix.lower() == '.xml':
                sanitized = HOSTNAME.sub('', sanitized)
                sanitized = SESSION_XML.sub(
                    lambda match: match[1] + match[2] + sessions[match[3]] + match[2], sanitized)
            elif path.suffix.lower() == '.html':
                def replace_session(match):
                    session = match[2]
                    if session not in sessions:
                        sessions[session] = 'qa-session-{:03d}'.format(len(sessions) + 1)
                    return match[1] + sessions[session] + match[3]
                sanitized = SESSION_HTML.sub(replace_session, sanitized)
            for label, pattern in PRIVACY_PATTERNS:
                if pattern.search(sanitized):
                    problems.append('{}: {}'.format(path.relative_to(ROOT).as_posix(), label))
            # Preserve BOM and line endings; untouched evidence stays byte-identical.
            data = sanitized.encode('utf-8')
            if original.startswith(b'\xef\xbb\xbf'):
                data = b'\xef\xbb\xbf' + data
            if data != original:
                changes[path] = data

        manifest_path = report_root / 'sha256.json'
        if manifest_path.exists():
            original = manifest_path.read_bytes()
            old_manifest = json.loads(original.decode('utf-8-sig'))
            public_files = {path.relative_to(report_root).as_posix(): path
                            for path in files if path != manifest_path}
            ordered = [name for name in old_manifest if name in public_files]
            ordered += sorted(set(public_files) - set(ordered))
            manifest = {}
            for name in ordered:
                path = public_files[name]
                data = changes[path] if path in changes else path.read_bytes()
                manifest[name] = hashlib.sha256(canonical_bytes(path, data)).hexdigest()
            newline = newline_for(original)
            data = (json.dumps(manifest, ensure_ascii=False, indent=2) + '\n').replace('\n', newline).encode('utf-8')
            if original.startswith(b'\xef\xbb\xbf'):
                data = b'\xef\xbb\xbf' + data
            if data != original:
                changes[manifest_path] = data
    return changes, problems


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--write', action='store_true', help='sanitize public reports and refresh integrity hashes')
    mode.add_argument('--check', action='store_true', help='read-only verification (default)')
    args = parser.parse_args()
    changes, problems = prepare_changes()
    if problems:
        for problem in problems:
            print('Review public evidence: ' + problem, file=sys.stderr)
        return 1
    if args.write:
        for path, data in changes.items():
            path.write_bytes(data)
            print('Sanitized: ' + path.relative_to(ROOT).as_posix())
        print('Public evidence sanitized; {} file(s) updated.'.format(len(changes)))
        return 0
    if changes:
        for path in changes:
            print('Needs sanitation/hash refresh: ' + path.relative_to(ROOT).as_posix())
        return 1
    print('Public evidence OK: metadata sanitized and integrity hashes valid.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
