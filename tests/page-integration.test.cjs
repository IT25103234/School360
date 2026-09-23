const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/js/app.js'), 'utf8');

function setup(page = 'student.html', user = { id: 1, role: 'STUDENT' }) {
    const storage = new Map(user ? [['s360_current_user', JSON.stringify(user)]] : []);
    const events = {};
    const documentEvents = {};
    const location = { origin: 'http://localhost:8080', protocol: 'http:', pathname: '/' + page, search: '', hash: '', href: page };
    const window = { location, addEventListener: (name, fn) => { events[name] = fn; }, history: {
        pushState: (_, __, hash) => { location.hash = hash; }
    }};
    const context = vm.createContext({ window, URLSearchParams, console,
        document: { addEventListener: (name, fn) => { documentEvents[name] = fn; },
            getElementById: id => ['tab-id', 'tab-courses'].includes(id) ? {} : null,
            querySelector: () => ({ id: 'menu-id' }) },
        localStorage: { getItem: key => storage.get(key) || null, setItem: (key, value) => storage.set(key, value), removeItem: key => storage.delete(key) },
        fetch: async () => { throw new Error('offline'); }
    });
    vm.runInContext(source, context);
    return { context, window, storage, events, run: code => vm.runInContext(code, context) };
}

test('every role routes to an existing portal', () => {
    const app = setup();
    for (const role of ['STUDENT', 'TEACHER', 'ADMIN', 'SUPPORT', 'STAFF', 'ENROLLMENT', 'OFFICER', 'LIBRARIAN']) {
        app.run(`redirectToDashboard('${role}')`);
        assert.ok(fs.existsSync(path.join(__dirname, '../src/main/resources/static', app.window.location.href)));
    }
});

test('missing, malformed, and wrong-role sessions redirect correctly', () => {
    const app = setup('student.html', null);
    app.run('checkAuth()');
    assert.equal(app.window.location.href, 'index.html');
    app.storage.set('s360_current_user', '{broken');
    assert.doesNotThrow(() => app.run('checkAuth()'));
    app.storage.set('s360_current_user', JSON.stringify({ id: 2, role: 'TEACHER' }));
    app.run('checkAuth()');
    assert.equal(app.window.location.href, 'teacher.html');
});

test('signed-in users can still visit public home and contact pages', () => {
    for (const page of ['index.html', 'contact.html']) {
        const app = setup(page);
        app.run('checkAuth()');
        assert.equal(app.window.location.href, page);
    }
});

test('section links restore on load and browser history navigation', () => {
    const app = setup();
    const selected = [];
    app.window.switchTab = section => selected.push(section);
    app.window.location.hash = '#courses';
    app.run('checkAuth(); initPageNavigation()');
    assert.equal(selected.at(-1), 'courses');
    app.window.switchTab('id');
    assert.equal(app.window.location.hash, '#id');
    app.window.location.hash = '#courses';
    app.events.popstate();
    assert.equal(selected.at(-1), 'courses');
    app.window.switchTab('invalid');
    assert.equal(selected.at(-1), 'courses');
});

test('API accepts JSON, text and empty successful responses', async () => {
    const app = setup();
    for (const [body, type, expected] of [['{"id":42}', 'application/json', { id: 42 }], ['Deleted', 'text/plain', 'Deleted'], ['', '', null]]) {
        app.context.fetch = async () => ({ ok: true, text: async () => body, headers: { get: () => type } });
        const actual = await app.run("apiCall('/admin/courses/42', 'DELETE')");
        assert.equal(JSON.stringify(actual), JSON.stringify(expected));
    }
});

test('server and network failures never mutate local demo data', async () => {
    const app = setup();
    app.storage.set('s360_courses', '[]');
    await assert.rejects(app.run("apiCall('/admin/courses', 'POST', {name: 'Example'})"), /offline/);
    app.context.fetch = async () => ({ ok: false, status: 400, text: async () => 'Rejected' });
    await assert.rejects(app.run("apiCall('/admin/courses', 'POST', {name: 'Example'})"), /Rejected/);
    assert.equal(app.storage.get('s360_courses'), '[]');
});
