/* The runner embeds this expression in the v2.1 runtime collection. */
({
  registeredUser(pm, payload, label, done) {
    // RegistroView returns a success message, not the created user's ID.
    // Resolve only the uniquely named fixture through the authenticated admin list.
    this.request(pm, 'GET', '/usuarios/', undefined, pm.variables.get('adminAccessToken'), (error, response) => {
      let users;
      try { users = response && response.json(); } catch (_) { users = null; }
      const matches = Array.isArray(users)
        ? users.filter(user => user.email === payload.email && user.username === payload.username)
        : [];
      const user = matches.length === 1 ? matches[0] : null;
      pm.test(label + ' | recuperar ID de cuenta creada', () => {
        pm.expect(error).to.equal(null);
        pm.expect(response && response.code).to.equal(200);
        pm.expect(users).to.be.an('array');
        pm.expect(matches).to.have.length(1);
        pm.expect(user && user.id).to.be.a('number').and.greaterThan(0);
      });
      done(!error && response && response.code === 200 && user && Number.isFinite(user.id) && user.id > 0 ? user : null);
    });
  },
  quotaHistory(pm, key, windowMs, now, marginMs = 2000) {
    const stored = pm.collectionVariables.get(key);
    const history = JSON.parse(stored === undefined ? (pm.variables.get(key) || '[]') : stored);
    if (!Array.isArray(history)) throw new Error('Historial de cuota QA inválido: ' + key);
    return history.filter(time => Number.isFinite(time) && now - time < windowMs + marginMs)
      .sort((a, b) => a - b);
  },
  waitUntil(pm, label, readyAt, done) {
    // A single timer chain: no polling requests, no interval left running in Newman.
    const tick = () => {
      const remainingMs = Math.max(0, readyAt - Date.now());
      if (remainingMs === 0) {
        console.log(label + ' | espera finalizada; reanudando.');
        done();
        return;
      }
      console.log(label + ' | faltan ' + Math.ceil(remainingMs / 1000)
        + ' s; reanudación prevista ' + new Date(readyAt).toISOString() + ' (UTC).');
      setTimeout(tick, Math.min(remainingMs, 30000));
    };
    tick();
  },
  reserve(pm, path, done) {
    const policy = path === '/api/token/' ? ['__newmanLoginAttemptTimes', 5, 900000]
      : path === '/usuarios/recuperar-password/' ? ['__newmanRecoveryAttemptTimes', 3, 3600000] : null;
    if (!policy) { done(); return; }
    const [key, limit, windowMs] = policy;
    const now = Date.now();
    const history = this.quotaHistory(pm, key, windowMs, now);
    pm.collectionVariables.set(key, JSON.stringify(history)); pm.environment.set(key, JSON.stringify(history));
    if (history.length >= limit) {
      // Wait only until enough entries expire to leave one slot, not a fresh full window.
      const readyAt = history[history.length - limit] + windowMs + 2000;
      const label = 'Cuota QA ' + key + ' | ' + (pm.info.requestName || 'llamada adicional') + ' | ' + path;
      this.waitUntil(pm, label, readyAt, () => this.reserve(pm, path, done));
      return;
    }
    history.push(now);
    pm.collectionVariables.set(key, JSON.stringify(history)); pm.environment.set(key, JSON.stringify(history));
    done();
  },
  request(pm, method, path, payload, token, done, preserveToken = false) {
    const refreshKeys = { adminAccessToken: 'adminRefreshToken', userAccessToken: 'refreshToken', secondUserAccessToken: 'secondUserRefreshToken' };
    const key = !preserveToken && token && Object.keys(refreshKeys).find(name => pm.variables.get(name) === token);
    let expiry;
    try { expiry = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).exp; } catch (_) { expiry = null; }
    if (key && expiry && expiry * 1000 - Date.now() < 300000 && pm.variables.get(refreshKeys[key])) {
      this.request(pm, 'POST', '/api/token/refresh/', { refresh: pm.variables.get(refreshKeys[key]) }, null, (error, response) => {
        pm.test('SETUP | renovar autorización para llamada adicional', () => {
          pm.expect(error).to.equal(null); pm.expect(response.code).to.equal(200);
          pm.expect(response.json().access).to.be.a('string').and.not.empty;
        });
        if (error || response.code !== 200) { done(error || new Error('No se renovó la autorización'), response); return; }
        const body = response.json();
        pm.collectionVariables.set(key, body.access); pm.environment.set(key, body.access);
        if (body.refresh) { pm.collectionVariables.set(refreshKeys[key], body.refresh); pm.environment.set(refreshKeys[key], body.refresh); }
        this.request(pm, method, path, payload, body.access, done, true);
      });
      return;
    }
    const dispatch = () => {
      const request = { url: pm.variables.replaceIn('{{baseUrl}}') + path, method, header: { 'Content-Type': 'application/json' } };
      if (token) request.header.Authorization = 'Bearer ' + token;
      if (payload !== undefined) request.body = { mode: 'raw', raw: JSON.stringify(payload) };
      pm.sendRequest(request, (error, response) => {
        if (!error && response.code !== 429 && method === 'POST' && ['/api/token/', '/usuarios/recuperar-password/'].includes(path)) {
          const key = path === '/api/token/' ? '__newmanLoginAttemptTimes' : '__newmanRecoveryAttemptTimes';
          const history = JSON.parse(pm.variables.get(key) || '[]');
          if (history.length) history[history.length - 1] = Date.now();
          pm.collectionVariables.set(key, JSON.stringify(history)); pm.environment.set(key, JSON.stringify(history));
        }
        done(error, response);
      });
    };
    if (method === 'POST') this.reserve(pm, path, dispatch); else dispatch();
  },
  pre(pm) {
    const name = pm.info.requestName || '';
    const id = (name.match(/^AUT-API-([A-Z]+-\d+)/) || [])[1];
    const put = (key, value) => {
      pm.collectionVariables.set(key, value);
      pm.environment.set(key, value);
    };
    const runId = pm.variables.get('runId');
    const registration = suffix => ({ username: 'qa_' + runId + '_' + suffix, email: 'qa+' + suffix + '-' + runId + '@example.test', password: pm.variables.get('testPassword'), dni: String(10000000 + Math.floor(Math.random() * 89999999)), nombre: 'Laura', apellido: 'QA', fecha_nacimiento: '1990-04-15' });
    const newCases = ['AUTH-47', 'AUTH-48', 'AUTH-49', 'AUTH-50', 'AUTH-51', 'PERF-22', 'INS-04', 'INS-05'];
    if (newCases.includes(id) && !runId) {
      console.warn('BLOCKED TC-' + id + ': ejecutar la preparación para obtener runId.'); pm.execution.skipRequest(); return;
    }
    const prerequisites = {
      'AUTH-47': ['testPassword', 'adminAccessToken'], 'AUTH-48': ['testPassword', 'qaStandardRole'],
      'AUTH-49': ['testPassword', 'adminAccessToken'], 'AUTH-50': ['testPassword', 'adminAccessToken'],
      'AUTH-51': ['testPassword', 'adminAccessToken'],
      'PERF-22': ['userAccessToken', 'otherUserId', 'adminAccessToken'],
      'INS-05': ['enrollmentId', 'userAccessToken']
    };
    const missing = (prerequisites[id] || []).filter(key => !pm.variables.get(key));
    if (missing.length) {
      const reason = 'BLOCKED TC-' + id + ': faltan fixtures ' + missing.join(', ');
      put('blocked' + id, reason); console.warn(reason); pm.execution.skipRequest(); return;
    }
    if (['AUTH-48', 'AUTH-49', 'AUTH-51'].includes(id)) {
      const payload = registration(id.toLowerCase());
      if (id === 'AUTH-48') { payload.rol = 'Administrador'; put('auth48Email', payload.email); }
      if (id === 'AUTH-49') { payload.fecha_nacimiento = '2026-02-30'; put('auth49Email', payload.email); }
      if (id === 'AUTH-51') payload.nombre = 'N'.repeat(24);
      put(id.replace('-', '').toLowerCase() + 'Payload', JSON.stringify(payload));
    }
    if (id === 'AUTH-47' || id === 'AUTH-50') {
      const payload = registration(id.toLowerCase());
      const prefix = id === 'AUTH-47' ? 'expiry' : 'auth50';
      put(prefix + 'Email', payload.email); put(prefix + 'Password', payload.password);
      this.request(pm, 'POST', '/usuarios/registro/', payload, null, (error, response) => {
        pm.test('TC-' + id + ' | preparar cuenta descartable', () => {
          pm.expect(error).to.equal(null); pm.expect(response.code).to.equal(201);
          pm.expect(response.json().message).to.be.a('string').and.not.empty;
        });
        if (error || !response || response.code !== 201) { pm.execution.skipRequest(); return; }
        this.registeredUser(pm, payload, 'TC-' + id, user => {
          if (!user) { pm.execution.skipRequest(); return; }
          put(prefix + 'UserId', String(user.id));
          if (id === 'AUTH-50') return;
          this.request(pm, 'POST', '/api/token/', { email: payload.email, password: payload.password }, null, (loginError, login) => {
            pm.test('TC-AUTH-47 | token emitido por el servidor', () => {
              pm.expect(loginError).to.equal(null); pm.expect(login.code).to.equal(200);
              pm.expect(login.json().access).to.be.a('string').and.not.empty;
              pm.expect(login.json().user.id).to.equal(user.id);
            });
            if (loginError || !login || login.code !== 200 || !login.json().access
                || !login.json().user || login.json().user.id !== user.id) { pm.execution.skipRequest(); return; }
            const token = login.json().access;
            let expiry;
            try { expiry = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).exp; } catch (_) { expiry = null; }
            if (!Number.isFinite(expiry) || expiry * 1000 <= Date.now() || expiry * 1000 - Date.now() > 4500000) {
              pm.test('TC-AUTH-47 | expiración verificable dentro del timeout del runner', () => pm.expect.fail('JWT sin exp futura válida o espera mayor al timeout del runner.'));
              pm.execution.skipRequest(); return;
            }
            put('expiryAccessToken', token);
            this.request(pm, 'GET', '/usuarios/' + user.id + '/', undefined, token, (profileError, profile) => {
              pm.test('TC-AUTH-47 | access válido antes de expirar', () => {
                pm.expect(profileError).to.equal(null); pm.expect(profile.code).to.equal(200);
                pm.expect(profile.json().id).to.equal(user.id);
              });
              if (profileError || !profile || profile.code !== 200) { pm.execution.skipRequest(); return; }
              console.log('TC-AUTH-47 | esperar expiración real: ' + Math.ceil((expiry * 1000 - Date.now()) / 1000) + ' s');
              setTimeout(() => put('expiryReady', 'true'), Math.max(1, expiry * 1000 + 1000 - Date.now()));
            }, true);
          });
        });
      });
      return;
    }
    if (id === 'PERF-22') {
      this.request(pm, 'GET', '/usuarios/' + pm.variables.get('otherUserId') + '/', undefined, pm.variables.get('adminAccessToken'), (error, response) => {
        pm.test('TC-PERF-22 | guardar perfil B antes del intento', () => {
          pm.expect(error).to.equal(null); pm.expect(response.code).to.equal(200);
          pm.expect(response.json().id).to.equal(Number(pm.variables.get('otherUserId')));
        });
        if (error || response.code !== 200 || response.json().id !== Number(pm.variables.get('otherUserId'))) { pm.execution.skipRequest(); return; }
        put('profile22Original', JSON.stringify(response.json()));
        const original = response.json();
        const payload = {};
        ['username', 'email', 'dni', 'nombre', 'apellido', 'fecha_nacimiento'].forEach(key => { payload[key] = original[key]; });
        payload.nombre = 'QA Ajeno'; put('profile22Payload', JSON.stringify(payload));
      });
    }
    if (['CONTACT-08', 'CONTACT-09', 'CONTACT-18'].includes(id)) {
      let original;
      try { original = JSON.parse(pm.variables.get('qaOriginalContact') || 'null'); } catch (_) { original = null; }
      const owned = original && original.id === Number(pm.variables.get('contactId')) && (String(original.correo_electronico || '').includes(String(runId)) || String(original.nombre_completo || '').startsWith('QA Newman ' + runId));
      if (!owned) {
        const reason = 'BLOCKED TC-' + id + ': falló CONTACT-01; no hay contacto propio verificado para mutar.';
        console.warn(reason); put('blocked' + id, reason); pm.execution.skipRequest(); return;
      }
    }
    if (runId) {
      const prefix = 'QA Newman ' + runId + ' ';
      [99, 100, 101].forEach(n => put('contactName' + n, prefix + 'N'.repeat(n - prefix.length)));
      [499, 500, 501].forEach(n => put('contactMessage' + n, 'M'.repeat(n)));
      put('qaContactEmail', 'qa+contact-' + runId + '@example.test');
      if (!pm.variables.get('newPassword2')) put('newPassword2', 'Qa!' + runId);
      if (!pm.variables.get('password11Dni')) put('password11Dni', String(10000000 + Math.floor(Math.random() * 89999999)));
      if (!pm.variables.get('hashUserDni')) put('hashUserDni', String(10000000 + Math.floor(Math.random() * 89999999)));
      const today = new Date(Date.now() - 3 * 60 * 60 * 1000);
      [17, 18, 64, 65].forEach(age => {
        const year = today.getUTCFullYear() - age;
        let monthDay = today.toISOString().slice(5, 10);
        if (monthDay === '02-29' && !(year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0))) monthDay = '02-28';
        put('age' + age + 'BirthDate', year + '-' + monthDay);
        if (age === 18 || age === 64) {
          put('age' + age + 'Email', 'qa+age' + age + '-' + runId + '@example.test');
          if (!pm.variables.get('age' + age + 'Dni')) put('age' + age + 'Dni', String(10000000 + Math.floor(Math.random() * 89999999)));
        }
      });
    }
    if (id === 'AUTH-24') {
      put('recoveryOldAccess', pm.variables.get('secondUserAccessToken'));
      put('recoveryOldRefresh', pm.variables.get('secondUserRefreshToken'));
    }
    if (id === 'AUTH-42' || id === 'AUTH-43') {
      const login = id === 'AUTH-42';
      const key = login ? '__newmanLoginAttemptTimes' : '__newmanRecoveryAttemptTimes';
      const windowMs = login ? 900000 : 3600000;
      const limit = login ? 5 : 3;
      const fillAvailableSlots = () => {
        const history = this.quotaHistory(pm, key, windowMs, Date.now(), 0);
        put(key, JSON.stringify(history));
        if (history.length >= limit) return;
        const payload = login
          ? { email: 'qa+throttle-' + runId + '@example.test', password: 'ClaveIncorrecta123!' }
          : { email: 'qa+throttle-' + runId + '@example.test', password: pm.variables.get('newPassword'), password_confirmation: pm.variables.get('newPassword') };
        pm.sendRequest({ url: pm.variables.replaceIn('{{baseUrl}}' + (login ? '/api/token/' : '/usuarios/recuperar-password/')), method: 'POST', header: { 'Content-Type': 'application/json' }, body: { mode: 'raw', raw: JSON.stringify(payload) } }, (error, response) => {
          pm.test('TC-' + id + ' | intento dentro de cuota', () => {
            pm.expect(error).to.equal(null);
            pm.expect(response.code).to.equal(login ? 401 : 200);
            pm.expect(response.headers.get('Content-Type') || '').to.match(/application\/json/i);
          });
          if (error || response.code === 429) return;
          history.push(Date.now());
          put(key, JSON.stringify(history));
          fillAvailableSlots();
        });
      };
      const previous = this.quotaHistory(pm, key, windowMs, Date.now());
      if (login && previous.length) {
        // This quota test deliberately needs every previous login to expire.
        // Unlike a normal request, one free slot is not sufficient for its oracle.
        const readyAt = previous[previous.length - 1] + windowMs + 2000;
        this.waitUntil(pm, 'TC-AUTH-42 | ventana limpia de login', readyAt, fillAvailableSlots);
      } else fillAvailableSlots();
    }
    // Refresh only live authorization variables, never the deliberately old token of AUTH-44.
    const auth = pm.request.headers.get('Authorization') || '';
    if (id === 'AUTH-44') return;
    const refreshKeys = { adminAccessToken: 'adminRefreshToken', userAccessToken: 'refreshToken', secondUserAccessToken: 'secondUserRefreshToken', underAgeAccessToken: 'underAgeRefreshToken', overAgeAccessToken: 'overAgeRefreshToken', age18AccessToken: 'age18RefreshToken', age64AccessToken: 'age64RefreshToken' };
    const resolvedKey = Object.keys(refreshKeys).find(key => auth === 'Bearer ' + pm.variables.get(key));
    const match = id === 'AUTH-24' ? [null, 'secondUserAccessToken'] : auth.match(/\{\{(\w+AccessToken)\}\}/) || (resolvedKey ? [null, resolvedKey] : null);
    if (!match) return;
    const accessKey = match[1];
    const access = pm.variables.get(accessKey);
    const refresh = pm.variables.get(refreshKeys[accessKey]);
    if (!access || !refresh) return;
    let expiry;
    try { expiry = JSON.parse(atob(access.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).exp; } catch (_) { return; }
    if (expiry * 1000 - Date.now() > 300000) return;
    pm.sendRequest({ url: pm.variables.replaceIn('{{baseUrl}}/api/token/refresh/'), method: 'POST', header: { 'Content-Type': 'application/json' }, body: { mode: 'raw', raw: JSON.stringify({ refresh }) } }, (error, response) => {
      pm.test('SETUP | renovar token vigente antes de ' + name, () => {
        pm.expect(error).to.equal(null);
        pm.expect(response.code).to.equal(200);
        pm.expect(response.json().access).to.be.a('string').and.not.empty;
      });
      if (!error && response.code === 200) {
        put(accessKey, response.json().access);
        if (response.json().refresh) put(refreshKeys[accessKey], response.json().refresh);
        if (id === 'AUTH-24') {
          put('recoveryOldAccess', response.json().access);
          put('recoveryOldRefresh', response.json().refresh || refresh);
        }
      }
    });
  },
  test(pm) {
    const name = pm.info.requestName || '';
    const id = (name.match(/^AUT-API-([A-Z]+-\d+)/) || [])[1];
    const runId = String(pm.variables.get('runId') || '');
    const get = key => pm.variables.get(key);
    const put = (key, value) => { pm.collectionVariables.set(key, value); pm.environment.set(key, value); };
    const baseUrl = pm.variables.replaceIn('{{baseUrl}}');
    const contact = () => ({ nombre_completo: 'QA Newman ' + runId, correo_electronico: get('qaContactEmail'), motivo: 'Consulta general', mensaje: 'Consulta sintética de Newman ' + runId });
    const errorContract = (body, field) => {
      pm.expect(body).to.be.an('object');
      pm.expect(body).to.not.have.property('traceback');
      if (field) pm.expect(body[field]).to.be.an('array').and.not.empty;
      else pm.expect(Boolean(body.detail || body.mensaje || body.codigo || Object.keys(body).some(k => Array.isArray(body[k]) && body[k].length))).to.equal(true);
    };
    const check = (response, expected, label, field) => {
      pm.test(label + ' | HTTP', () => pm.expect(response.code).to.equal(expected));
      pm.test(label + ' | Content-Type y SLA', () => {
        pm.expect(response.headers.get('Content-Type') || '').to.match(/application\/json/i);
        pm.expect(response.responseTime).to.be.below(800);
      });
      pm.test(label + ' | contrato raíz', () => {
        const body = response.json();
        if (expected >= 400) errorContract(body, field);
        else if (Array.isArray(body) && expected === 200) pm.expect(body).to.be.an('array');
        else pm.expect(body).to.be.an('object');
      });
    };
    const registerContact = body => {
      if (!runId || !body || !body.id) return;
      if (!String(body.correo_electronico || '').includes(runId) && !String(body.nombre_completo || '').includes(runId)) return;
      const ids = JSON.parse(get('ownedContactIds') || '[]');
      if (!ids.includes(body.id)) ids.push(body.id);
      put('ownedContactIds', JSON.stringify(ids));
    };
    const send = (method, path, payload, token, expected, label, field, done) => {
      this.request(pm, method, path, payload, token, (error, response) => {
        pm.test(label + ' | transporte', () => pm.expect(error).to.equal(null));
        if (error) return;
        check(response, expected, label, field);
        if (method === 'POST' && path === '/contactos/' && response.code >= 200 && response.code < 300) registerContact(response.json());
        if (done) done(response);
      });
    };
    const serial = (entries, task, index = 0) => {
      if (index < entries.length) task(entries[index], () => serial(entries, task, index + 1));
    };
    const path = pm.request.url.toString();
    let body;
    try { body = pm.response.json(); } catch (_) { body = null; }
    if (id === 'PERF-09' && pm.response.code === 200) {
      const matchesFixture = body && body.id === Number(get('otherUserId')) && typeof body.email === 'string' && body.email.length > 0;
      pm.test('TC-PERF-09 | respuesta corresponde al usuario B', () => pm.expect(Boolean(matchesFixture)).to.equal(true));
      if (matchesFixture) put('otherUserEmail', body.email);
    }
    if (id && pm.response.code >= 400) pm.test('TC-' + id + ' | mensaje estructurado de primer nivel', () => errorContract(body));
    if (id && body && typeof body === 'object') pm.test('TC-' + id + ' | no expone contraseña en respuesta', () => (Array.isArray(body) ? body : [body]).forEach(value => {
      if (pm.response.code >= 400 && Object.prototype.hasOwnProperty.call(value, 'password')) {
        pm.expect(value.password).to.be.an('array').and.not.empty;
        value.password.forEach(message => pm.expect(message).to.be.a('string'));
      } else pm.expect(value).to.not.have.property('password');
    }));
    if (pm.request.method === 'POST' && path.endsWith('/usuarios/recuperar-password/') && pm.response.code !== 429) {
      const history = JSON.parse(get('__newmanRecoveryAttemptTimes') || '[]').filter(t => Date.now() - t < 3600000);
      if (history.length) history[history.length - 1] = Date.now();
      put('__newmanRecoveryAttemptTimes', JSON.stringify(history));
    }
    if (pm.request.method === 'POST' && path.endsWith('/contactos/') && pm.response.code >= 200 && pm.response.code < 300) registerContact(body);
    const roles = { 'Login usuario de pruebas': 'user', 'SETUP | Login segundo usuario estándar descartable': 'secondUser', 'SETUP | Login usuario menor de 18 descartable': 'underAge', 'SETUP | Login usuario de 65 o más descartable': 'overAge', 'SETUP | Login usuario de 18 años': 'age18', 'SETUP | Login usuario de 64 años': 'age64' };
    if (roles[name] && body && body.refresh) {
      const role = roles[name];
      put(role === 'user' ? 'refreshToken' : role + 'RefreshToken', body.refresh);
      if (role === 'user') put('qaStandardRole', body.user.rol);
      if (role === 'age18' || role === 'age64') { put(role + 'AccessToken', body.access); put(role + 'UserId', String(body.user.id)); }
    }
    if (['AUTH-14', 'AUTH-20', 'AUTH-22', 'AUTH-27', 'AUTH-29'].includes(id) && body && body.access && body.refresh) {
      put('userAccessToken', body.access); put('refreshToken', body.refresh);
    }
    const statuses = { 'CONTACT-01': 201, 'CONTACT-02': 400, 'CONTACT-03': 400, 'CONTACT-04': 400, 'CONTACT-05': 201, 'CONTACT-08': 200, 'CONTACT-09': 403, 'CONTACT-15': 403, 'CONTACT-16': 401, 'CONTACT-17': 404, 'CONTACT-18': 400, 'CONTACT-19': 404, 'CONTACT-20': 201, 'CONTACT-21': 201, 'AUTH-24': 200, 'AUTH-42': 429, 'AUTH-43': 429, 'AUTH-44': 401, 'AUTH-45': 200, 'AUTH-46': 201, 'HEALTH-03': 405, 'HEALTH-07': 406, 'CAMP-37': 400, 'CAMP-73': 406, 'CAMP-74': 401, 'INS-01': 201, 'INS-02': 201, 'INS-03': 200 };
    Object.assign(statuses, { 'AUTH-47': 401, 'AUTH-48': 201, 'AUTH-49': 400, 'AUTH-50': 400, 'AUTH-51': 201, 'INS-04': 401, 'INS-05': 401 });
    if (Object.prototype.hasOwnProperty.call(statuses, id)) {
      const fields = { 'CONTACT-03': 'correo_electronico', 'CONTACT-04': 'motivo', 'CONTACT-18': 'tracked', 'AUTH-49': 'fecha_nacimiento' };
      check(pm.response, statuses[id], 'TC-' + id, fields[id]);
    }
    if (id && pm.response.code >= 200 && pm.response.code < 300 && body) {
      const resourcePath = pm.variables.replaceIn(path).replace(baseUrl, '').split('?')[0];
      const requestMethod = String(pm.request.method).toUpperCase();
      const values = Array.isArray(body) ? body : [body];
      const strings = (value, keys, nullable = false) => keys.forEach(key => {
        pm.expect(value).to.have.property(key);
        if (nullable && value[key] === null) return;
        pm.expect(value[key]).to.be.a('string');
      });
      if (/^\/usuarios\/(?:\d+\/)?$/.test(resourcePath)) pm.test('TC-' + id + ' | modelo usuario de primer nivel', () => { pm.expect(body).to.be.an(requestMethod === 'GET' && resourcePath === '/usuarios/' ? 'array' : 'object'); values.forEach(value => { pm.expect(value.id).to.be.a('number'); strings(value, ['username','email','dni','nombre','apellido','fecha_nacimiento','fecha_registro','rol']); }); });
      if (/^\/campanias\/(?:\d+\/)?$/.test(resourcePath)) pm.test('TC-' + id + ' | modelo campaña de primer nivel', () => { pm.expect(body).to.be.an(requestMethod === 'GET' && resourcePath === '/campanias/' ? 'array' : 'object'); values.forEach(value => {
        pm.expect(value.id).to.be.a('number'); strings(value, ['titulo','descripcion','ubicacion','fecha_inicio','fecha_fin','estado_campania','estado_calculado']);
        pm.expect(value.total_inscriptos).to.be.a('number');
        ['centro_salud','cupo_maximo'].forEach(key => { pm.expect(value).to.have.property(key); if (value[key] !== null) pm.expect(value[key]).to.be.a('number'); });
        pm.expect(value).to.have.property('centro_salud_detalle'); if (value.centro_salud_detalle !== null) pm.expect(value.centro_salud_detalle).to.be.an('object');
      }); });
      if (resourcePath === '/centros-salud/') pm.test('TC-' + id + ' | centros de primer nivel', () => { pm.expect(body).to.be.an('array'); body.forEach(value => { pm.expect(value.id).to.be.a('number'); strings(value,['nombre']); strings(value,['direccion','barrio','localidad','telefono','sitio_web','latitud','longitud'],true); }); });
      if (resourcePath === '/inscripciones/mis-inscripciones/') pm.test('TC-' + id + ' | arrays raíz de inscripciones', () => { pm.expect(body.actuales).to.be.an('array'); pm.expect(body.historicas).to.be.an('array'); });
      if (/^\/inscripciones\/campanias\/\d+\/$/.test(resourcePath) && pm.request.method === 'GET') pm.test('TC-' + id + ' | listado de inscriptos raíz', () => { pm.expect(body.campania).to.be.an('object'); pm.expect(body.total_inscriptos).to.be.a('number'); pm.expect(body.usuarios).to.be.an('array'); });
    }
    if (id === 'CONTACT-01' && body && body.id) {
      put('contactId', String(body.id)); put('qaOriginalContact', JSON.stringify(body));
      pm.test('TC-CONTACT-01 | contacto público creado sin seguimiento', () => {
        pm.expect(body.tracked).to.equal(false); pm.expect(body.fecha_creacion).to.be.a('string');
      });
      send('GET', '/contactos/' + body.id + '/', undefined, get('adminAccessToken'), 200, 'TC-CONTACT-01 persistencia', undefined, response => pm.test('TC-CONTACT-01 | id persistido', () => pm.expect(response.json().id).to.equal(body.id)));
    }
    if (id === 'CONTACT-02') {
      pm.test('TC-CONTACT-02 | cuatro obligatorios', () => ['nombre_completo', 'correo_electronico', 'motivo', 'mensaje'].forEach(k => pm.expect(body[k]).to.be.an('array').and.not.empty));
      serial(['nombre_completo', 'correo_electronico', 'motivo', 'mensaje'], (field, next) => {
        const payload = contact(); delete payload[field];
        send('POST', '/contactos/', payload, null, 400, 'TC-CONTACT-02 sin ' + field, field, next);
      });
    }
    if (id === 'CONTACT-03') send('POST', '/contactos/', { ...contact(), correo_electronico: 'anaunmail.com' }, null, 400, 'TC-CONTACT-03 sin arroba', 'correo_electronico');
    if (id === 'CONTACT-05') {
      serial([{ nombre_completo: get('contactName101') }, { mensaje: get('contactMessage501') }], (change, next) => {
        const field = Object.keys(change)[0];
        send('POST', '/contactos/', { ...contact(), ...change }, null, 400, 'TC-CONTACT-05 exceso ' + field, field, next);
      });
    }
    if (['CONTACT-01', 'CONTACT-05', 'CONTACT-20', 'CONTACT-21'].includes(id) && body) {
      pm.test('TC-' + id + ' | campos directos de contacto', () => {
        pm.expect(body.id).to.be.a('number'); pm.expect(body.tracked).to.be.a('boolean');
        ['nombre_completo', 'correo_electronico', 'motivo', 'mensaje', 'fecha_creacion'].forEach(k => pm.expect(body[k]).to.be.a('string'));
      });
      const lengths = { 'CONTACT-05': [100, 500], 'CONTACT-20': [99, null], 'CONTACT-21': [null, 499] };
      if (lengths[id]) pm.test('TC-' + id + ' | longitud exacta sin truncamiento', () => {
        if (lengths[id][0]) pm.expect(body.nombre_completo.length).to.equal(lengths[id][0]);
        if (lengths[id][1]) pm.expect(body.mensaje.length).to.equal(lengths[id][1]);
      });
    }
    if (id === 'CONTACT-07') pm.test('TC-CONTACT-07 | ID solicitado', () => pm.expect(body.id).to.equal(Number(get('contactId'))));
    if (id === 'CONTACT-08') {
      const original = JSON.parse(get('qaOriginalContact'));
      const sameContent = response => pm.test('TC-CONTACT-08 | contenido original inmutable', () => ['nombre_completo', 'correo_electronico', 'motivo', 'mensaje'].forEach(k => pm.expect(response.json()[k]).to.equal(original[k])));
      pm.test('TC-CONTACT-08 | tracked cambiado', () => pm.expect(body.tracked).to.equal(true));
      send('GET', '/contactos/' + get('contactId') + '/', undefined, get('adminAccessToken'), 200, 'TC-CONTACT-08 persistencia', undefined, response => {
        pm.test('TC-CONTACT-08 | tracked persistido', () => pm.expect(response.json().tracked).to.equal(true));
        send('PUT', '/contactos/' + get('contactId') + '/', { tracked: original.tracked, nombre_completo: 'Alterado', correo_electronico: 'alterado@example.test', motivo: 'Otro', mensaje: 'Alterado' }, get('adminAccessToken'), 200, 'TC-CONTACT-08 restauración', undefined, sameContent);
      });
    }
    if (id === 'CONTACT-09') send('PUT', '/contactos/' + get('contactId') + '/', { tracked: true }, null, 401, 'TC-CONTACT-09 sin token', undefined, () => send('GET', '/contactos/' + get('contactId') + '/', undefined, get('adminAccessToken'), 200, 'TC-CONTACT-09 estado', undefined, response => pm.test('TC-CONTACT-09 | no modificado', () => pm.expect(response.json().tracked).to.equal(JSON.parse(get('qaOriginalContact')).tracked))));
    if (id === 'HEALTH-03') serial(['PUT', 'DELETE'], (method, next) => send(method, '/centros-salud/', method === 'PUT' ? { nombre: 'QA Newman ' + runId } : undefined, null, 405, 'TC-HEALTH-03 ' + method, undefined, next));
    if (id === 'CAMP-37') pm.test('TC-CAMP-37 | error de campaña finalizada', () => pm.expect(body.codigo).to.equal('campania_finalizada'));
    if (id === 'INS-01' || id === 'INS-02') {
      const age = id === 'INS-01' ? 18 : 64;
      pm.test('TC-' + id + ' | alta de inscripción', () => {
        pm.expect(body.data).to.be.an('object'); pm.expect(body.data.id).to.be.a('number');
        pm.expect(body.data.usuario).to.equal(Number(get('age' + age + 'UserId')));
        pm.expect(body.data.campania).to.equal(Number(get('campaignId')));
      });
      if (pm.response.code === 201 && body.data && body.data.id) put('age' + age + 'EnrollmentId', String(body.data.id));
    }
    if (id === 'INS-03') pm.test('TC-INS-03 | búsqueda sin coincidencias', () => pm.expect(body.usuarios).to.be.an('array').and.empty);
    if (id === 'AUTH-24' || id === 'AUTH-45') pm.test('TC-' + id + ' | respuesta neutral sin tokens', () => { pm.expect(body.message).to.be.a('string').and.not.empty; pm.expect(body).to.not.have.property('access'); pm.expect(body).to.not.have.property('refresh'); });
    if (id === 'AUTH-44') {
      send('POST', '/api/token/refresh/', { refresh: get('recoveryOldRefresh') }, null, 401, 'TC-AUTH-44 refresh previo');
    }
    if (id === 'AUTH-44' || id === 'AUTH-45') {
      const password = get(id === 'AUTH-44' ? 'newPassword' : 'newPassword2');
      send('POST', '/api/token/', { email: get('otherUserEmail'), password }, null, 200, 'TC-' + id + ' login posterior', undefined, response => {
        const history = JSON.parse(get('__newmanLoginAttemptTimes') || '[]');
        if (history.length) history[history.length - 1] = Date.now(); put('__newmanLoginAttemptTimes', JSON.stringify(history));
        if (response.code !== 200) return;
        put('secondUserAccessToken', response.json().access); put('secondUserRefreshToken', response.json().refresh); put('otherUserPassword', password);
        if (id === 'AUTH-45') {
          put('qaRecoveryBypassObserved', 'true');
          pm.test('TC-AUTH-45 | no cambia clave sin demostrar posesión del email', () => pm.expect.fail('Bypass email-only reproducido en cuenta descartable; riesgo conocido, no PASS de seguridad.'));
        }
      });
    }
    if (id === 'AUTH-42' || id === 'AUTH-43') pm.test('TC-' + id + ' | Retry-After y sin tokens', () => { pm.expect(Number(pm.response.headers.get('Retry-After'))).to.be.greaterThan(0); pm.expect(body).to.not.have.property('access'); pm.expect(body).to.not.have.property('refresh'); });
    if (id === 'AUTH-22' && body && body.access && body.refresh) {
      ['access', 'refresh'].forEach(kind => pm.test('TC-AUTH-22 | claims y fechas ' + kind, () => {
        const claims = JSON.parse(atob(body[kind].split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
        pm.expect(claims.id).to.equal(body.user.id); pm.expect(claims.email).to.equal(body.user.email); pm.expect(claims.rol).to.equal(body.user.rol);
        pm.expect(claims.token_type).to.equal(kind); pm.expect(claims.iat).to.be.a('number'); pm.expect(claims.exp).to.be.a('number').and.greaterThan(Math.floor(Date.now() / 1000));
        pm.expect(claims.iat).to.be.below(claims.exp); pm.expect(claims).to.not.have.property('password');
      }));
      send('GET', '/usuarios/' + body.user.id + '/', undefined, body.access, 200, 'TC-AUTH-22 access emitido aceptado');
      const parts = body.access.split('.'); parts[2] = (parts[2][0] === 'A' ? 'B' : 'A') + parts[2].slice(1);
      send('GET', '/usuarios/' + body.user.id + '/', undefined, parts.join('.'), 401, 'TC-AUTH-22 firma alterada rechazada');
    }
    if (id === 'AUTH-28' && get('refreshToken')) {
      const parts = get('refreshToken').split('.');
      if (parts.length === 3) {
        parts[2] = (parts[2][0] === 'A' ? 'B' : 'A') + parts[2].slice(1);
        send('POST', '/api/token/refresh/', { refresh: parts.join('.') }, null, 401, 'TC-AUTH-28 firma adulterada');
      }
    }
    if (['AUTH-21', 'AUTH-28', 'AUTH-32', 'AUTH-37'].includes(id)) pm.test('TC-' + id + ' | no emite tokens ante error', () => {
      ['access', 'refresh'].forEach(key => {
        if (!Object.prototype.hasOwnProperty.call(body, key)) return;
        pm.expect(body[key], key + ' debe contener errores, no un token').to.be.an('array').and.not.empty;
        body[key].forEach(message => pm.expect(message).to.be.a('string').and.not.empty);
      });
    });
    if (id === 'AUTH-33') {
      const variants = [{ nombre: 'Ana123' }, { apellido: 'Lopez@' }, { username: 'qa invalid ' + runId }, { nombre: 'A'.repeat(26) }, { apellido: 'A'.repeat(26) }, { username: 'A'.repeat(151) }];
      let number = 0;
      serial(variants, (change, next) => {
        const payload = { username: 'qa_' + runId + '_v' + number, email: 'qa+auth33-' + number++ + '-' + runId + '@example.test', password: get('testPassword'), dni: String(10000000 + Math.floor(Math.random() * 89999999)), nombre: 'Laura', apellido: 'QA', fecha_nacimiento: '1990-04-15', ...change };
        const field = Object.keys(change)[0];
        send('POST', '/usuarios/registro/', payload, null, 400, 'TC-AUTH-33 variante ' + number + ' ' + field, field, next);
      });
    }
    if (id === 'PERF-11') {
      pm.test('TC-PERF-11 | rol protegido', () => pm.expect(body.rol).to.equal(get('qaStandardRole')));
      send('GET', '/dashboard/', undefined, get('userAccessToken'), 403, 'TC-PERF-11 permisos administrativos denegados');
    }
    if (id === 'PERF-12' && pm.response.code === 200) {
      const profilePath = '/usuarios/' + get('ownUserId') + '/';
      const oldPassword = get('profilePasswordBeforePUT');
      const attemptedPassword = get('profileAttemptedPassword');
      const fields = ['username', 'email', 'dni', 'nombre', 'apellido', 'fecha_nacimiento'];
      const expectedProfile = JSON.parse(pm.variables.replaceIn(pm.request.body.raw));
      const assertProfile = (response, expected, label) => pm.test(label, () => {
        pm.expect(response.json().id).to.equal(Number(get('ownUserId')));
        fields.forEach(key => pm.expect(response.json()[key], key).to.equal(expected[key]));
        pm.expect(response.json()).to.not.have.property('password');
      });
      pm.test('TC-PERF-12 | clave enviada distinta de la original', () => {
        pm.expect(oldPassword).to.be.a('string').and.not.empty;
        pm.expect(attemptedPassword).to.not.equal(oldPassword);
      });
      assertProfile(pm.response, expectedProfile, 'TC-PERF-12 | datos permitidos actualizados');
      const verifyChange = (previousPassword, newPassword, oldAccess, oldRefresh, profile, label, done) => {
        this.request(pm, 'GET', profilePath, undefined, oldAccess, (error, response) => {
          pm.test(label + ' | access anterior revocado', () => {
            pm.expect(error).to.equal(null); pm.expect(response && response.code).to.equal(401);
          });
          if (error || !response) return;
          check(response, 401, label + ' access anterior');
          send('POST', '/api/token/refresh/', { refresh: oldRefresh }, null, 401, label + ' refresh anterior', undefined, () => {
            send('POST', '/api/token/', { email: get('userEmail'), password: previousPassword }, null, 401, label + ' clave anterior rechazada', undefined, () => {
              send('POST', '/api/token/', { email: get('userEmail'), password: newPassword }, null, 200, label + ' clave nueva vigente', undefined, login => {
                let session;
                try { session = login.json(); } catch (_) { session = null; }
                pm.test(label + ' | login corresponde al titular', () => {
                  pm.expect(session && session.user && session.user.id).to.equal(Number(get('ownUserId')));
                  pm.expect(session && session.access).to.be.a('string').and.not.empty;
                  pm.expect(session && session.refresh).to.be.a('string').and.not.empty;
                });
                if (login.code !== 200 || !session || !session.user
                    || session.user.id !== Number(get('ownUserId')) || !session.access || !session.refresh) return;
                put('userPassword', newPassword);
                put('userAccessToken', session.access); put('refreshToken', session.refresh);
                send('GET', profilePath, undefined, session.access, 200, label + ' persistencia', undefined, saved => {
                  if (saved.code !== 200) return;
                  assertProfile(saved, profile, label + ' | edición persistida');
                  done();
                });
              });
            });
          });
        }, true);
      };
      verifyChange(oldPassword, attemptedPassword, get('profileAccessBeforePUT'), get('profileRefreshBeforePUT'), expectedProfile, 'TC-PERF-12 titular', () => {
        const adminPassword = attemptedPassword + 'Admin';
        const adminPayload = { ...expectedProfile, apellido: 'QA Admin Permitida', password: adminPassword };
        const accessBeforeAdminPUT = get('userAccessToken');
        const refreshBeforeAdminPUT = get('refreshToken');
        send('PUT', profilePath, adminPayload, get('adminAccessToken'), 200, 'TC-PERF-12 edición admin', undefined, adminResponse => {
          if (adminResponse.code !== 200) return;
          assertProfile(adminResponse, adminPayload, 'TC-PERF-12 | datos actualizados por admin');
          verifyChange(attemptedPassword, adminPassword, accessBeforeAdminPUT, refreshBeforeAdminPUT, adminPayload, 'TC-PERF-12 administrador', () => {});
        });
      });
    }
    if (id === 'AUTH-47') pm.test('TC-AUTH-47 | se usó el token emitido tras su expiración', () => pm.expect(get('expiryReady')).to.equal('true'));
    if (['AUTH-47', 'INS-04', 'INS-05'].includes(id)) pm.test('TC-' + id + ' | rechazo sin sesión ni datos personales', () => {
      ['access', 'refresh', 'actuales', 'historicas', 'user'].forEach(key => pm.expect(body).to.not.have.property(key));
    });
    if (id === 'AUTH-48' && pm.response.code === 201) {
      send('POST', '/api/token/', { email: get('auth48Email'), password: get('testPassword') }, null, 200, 'TC-AUTH-48 login', undefined, login => {
        pm.test('TC-AUTH-48 | rol enviado ignorado', () => {
          pm.expect(login.json().user.rol).to.equal(get('qaStandardRole'));
          pm.expect(login.json().user.rol).to.not.equal('Administrador');
        });
        if (login.code === 200 && login.json().access) send('GET', '/dashboard/', undefined, login.json().access, 403, 'TC-AUTH-48 privilegios denegados');
      });
    }
    if (id === 'AUTH-49') send('GET', '/usuarios/', undefined, get('adminAccessToken'), 200, 'TC-AUTH-49 verificar ausencia', undefined, users => {
      pm.test('TC-AUTH-49 | no creó cuenta con fecha imposible', () => pm.expect(users.json().some(user => user.email === get('auth49Email'))).to.equal(false));
    });
    if (id === 'AUTH-50') {
      const originalPassword = get('auth50Password');
      const validNew = originalPassword + 'Distinta1!';
      const verifyUnchanged = (rejected, label, next) => {
        send('POST', '/api/token/', { email: get('auth50Email'), password: rejected }, null, 401, label + ' clave rechazada no permite login', undefined, () => {
          send('POST', '/api/token/', { email: get('auth50Email'), password: originalPassword }, null, 200, label + ' clave original conservada', undefined, next);
        });
      };
      verifyUnchanged('Aa123456!', 'TC-AUTH-50 débil', () => {
        send('POST', '/usuarios/recuperar-password/', { email: get('auth50Email'), password: validNew, password_confirmation: validNew + 'Otra' }, null, 400, 'TC-AUTH-50 confirmación distinta', undefined, () => verifyUnchanged(validNew, 'TC-AUTH-50 confirmación', () => {}));
      });
    }
    if (id === 'AUTH-51') {
      const registration = (field, length, suffix) => {
        const payload = { username: 'qa_' + runId + '_limits_' + suffix, email: 'qa+limits-' + suffix + '-' + runId + '@example.test', password: get('testPassword'), dni: String(10000000 + Math.floor(Math.random() * 89999999)), nombre: 'Laura', apellido: 'QA', fecha_nacimiento: '1990-04-15' };
        if (field === 'username') payload[field] += 'u'.repeat(length - payload[field].length);
        else payload[field] = 'N'.repeat(length);
        return payload;
      };
      const persistedLength = (response, payload, field, length, next) => {
        if (response.code !== 201) { next(); return; }
        this.registeredUser(pm, payload, 'TC-AUTH-51 ' + field + length, user => {
          if (!user) { next(); return; }
          send('GET', '/usuarios/' + user.id + '/', undefined, get('adminAccessToken'), 200, 'TC-AUTH-51 persistencia ' + field + length, undefined, profile => {
            pm.test('TC-AUTH-51 | ' + field + ' conserva ' + length + ' caracteres', () => pm.expect(profile.json()[field].length).to.equal(length)); next();
          });
        });
      };
      const initialPayload = JSON.parse(pm.variables.replaceIn(pm.request.body.raw));
      persistedLength(pm.response, initialPayload, 'nombre', 24, () => {
        const variants = [['nombre',25,201],['nombre',26,400],['apellido',24,201],['apellido',25,201],['apellido',26,400],['username',149,201],['username',150,201],['username',151,400]];
        serial(variants, (variant, next) => {
          const [field,length,status] = variant;
          const payload = registration(field,length,field+length);
          send('POST', '/usuarios/registro/', payload, null, status, 'TC-AUTH-51 ' + field + length, status === 400 ? field : undefined, response => {
            if (status === 201) persistedLength(response, payload, field, length, next); else next();
          });
        });
      });
    }
    if (id === 'PERF-22') {
      pm.test('TC-PERF-22 | edición ajena denegada', () => pm.expect([403,404]).to.include(pm.response.code));
      pm.test('TC-PERF-22 | Content-Type y SLA', () => { pm.expect(pm.response.headers.get('Content-Type') || '').to.match(/application\/json/i); pm.expect(pm.response.responseTime).to.be.below(800); });
      send('GET', '/usuarios/' + get('otherUserId') + '/', undefined, get('adminAccessToken'), 200, 'TC-PERF-22 consulta posterior', undefined, response => pm.test('TC-PERF-22 | perfil ajeno sin cambios', () => {
        const original = JSON.parse(get('profile22Original'));
        ['id','username','email','dni','nombre','apellido','fecha_nacimiento','fecha_registro','rol'].forEach(key => pm.expect(response.json()[key], key).to.eql(original[key]));
      }));
    }
    if (id === 'INS-05') send('GET', '/inscripciones/mis-inscripciones/', undefined, get('userAccessToken'), 200, 'TC-INS-05 inscripción conservada', undefined, response => {
      pm.test('TC-INS-05 | DELETE sin token no eliminó el registro', () => pm.expect(response.json().actuales.some(entry => entry.id === Number(get('enrollmentId')))).to.equal(true));
    });
    if (id === 'CAMP-17') pm.test('TC-CAMP-17 | estado por fechas futuras', () => pm.expect(body.estado_calculado).to.equal('Proximamente'));
    if (id === 'DASH-02') send('GET', '/campanias/', undefined, null, 200, 'TC-DASH-02 referencia de campañas', undefined, response => {
      const campaigns = response.json();
      pm.test('TC-DASH-02 | total campañas', () => pm.expect(body.total_campanias).to.equal(campaigns.length));
      send('GET', '/usuarios/', undefined, get('adminAccessToken'), 200, 'TC-DASH-02 referencia de usuarios', undefined, usersResponse => {
        const standardIds = new Set(usersResponse.json().filter(user => user.rol === get('qaStandardRole')).map(user => user.id));
        const donors = new Set(); let enrollments = 0;
        function collect(index) {
          if (index === campaigns.length) {
            pm.test('TC-DASH-02 | total inscripciones', () => pm.expect(body.total_inscripciones).to.equal(enrollments));
            pm.test('TC-DASH-02 | donantes únicos estándar', () => pm.expect(body.total_donantes).to.equal(donors.size));
            return;
          }
          send('GET', '/inscripciones/campanias/' + campaigns[index].id + '/', undefined, get('adminAccessToken'), 200, 'TC-DASH-02 inscripciones campaña ' + campaigns[index].id, undefined, enrollmentResponse => {
            const data = enrollmentResponse.json(); enrollments += data.total_inscriptos;
            data.usuarios.forEach(user => { if (standardIds.has(user.id)) donors.add(user.id); });
            collect(index + 1);
          });
        }
        collect(0);
      });
    });
    if (id === 'DASH-06') send('GET', '/campanias/', undefined, null, 200, 'TC-DASH-06 referencia de campañas', undefined, response => {
      const campaigns = response.json(); const today = new Date(Date.now() - 10800000).toISOString().slice(0, 10);
      const state = campaign => campaign.cupo_maximo !== null && campaign.total_inscriptos >= campaign.cupo_maximo ? 'Finalizada' : campaign.fecha_inicio > today ? 'Proximamente' : campaign.fecha_fin >= today ? 'Activa' : 'Finalizada';
      const expectedCounts = {}; campaigns.forEach(campaign => { const key = state(campaign); expectedCounts[key] = (expectedCounts[key] || 0) + 1; });
      const actualCounts = {}; body.campanias_por_estado.forEach(entry => { actualCounts[entry.estado] = entry.cantidad; });
      pm.test('TC-DASH-06 | cantidades por estado', () => pm.expect(actualCounts).to.eql(expectedCounts));
      pm.test('TC-DASH-06 | recientes existentes y disponibles', () => {
        if (campaigns.some(campaign => state(campaign) !== 'Finalizada')) pm.expect(body.campanias_recientes.length).to.be.greaterThan(0);
        body.campanias_recientes.forEach(recent => {
          const campaign = campaigns.find(entry => entry.id === recent.id); pm.expect(campaign).to.exist;
          pm.expect(state(campaign)).to.not.equal('Finalizada'); pm.expect(recent.estado_calculado).to.equal(state(campaign));
        });
      });
    });
  }
})
