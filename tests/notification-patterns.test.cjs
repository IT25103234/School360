const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const html = fs.readFileSync(require('node:path').join(__dirname, '../src/main/resources/static/student.html'), 'utf8');
const label = html.slice(html.indexOf('        function notificationLabel('), html.indexOf('        function renderNotificationsUI('));
const clickStart = html.indexOf('        async function handleNotifClick(');
const click = html.slice(clickStart, html.indexOf('        /*', clickStart));

test('notification labels preserve existing types and identify new events', () => {
    const context = vm.createContext({});
    vm.runInContext(label, context);
    for (const [type, expected] of Object.entries({ANNOUNCEMENT:'Announcement', BOOK_AVAILABLE:'Book Available', ENROLLMENT_STATUS:'Enrollment Update', SUPPORT_STATUS:'Ticket Status Update', SUPPORT_REPLY:'Support Reply', BOOK_RESERVED:'Book Reserved', RESERVATION_CANCELLED:'Reservation Update', INFO_REQUEST:'Details Requested', ATTACHMENT_REQUEST:'File Requested'})) {
        assert.ok(context.notificationLabel(type).includes(expected));
    }
});

test('notification clicks mark read and route to the correct existing section', async () => {
    for (const [type, tab] of Object.entries({ANNOUNCEMENT:'timetable', ENROLLMENT_STATUS:'courses', BOOK_AVAILABLE:'library', BOOK_RESERVED:'library', RESERVATION_CANCELLED:'library'})) {
        const calls = [];
        const notification = {id: 1, isRead: false};
        const context = vm.createContext({studentNotificationsList:[notification], apiCall: async (...args) => calls.push(args), renderNotificationsUI:()=>{}, document:{getElementById:()=>({style:{}})}, switchTab:t=>calls.push(t), scrollToMyReservations:()=>calls.push('reservations'), openNotifTicket:()=>assert.fail('unexpected ticket route')});
        vm.runInContext(click, context);
        await context.handleNotifClick(type, 0, 1);
        assert.equal(notification.isRead, true);
        assert.equal(calls[0][0], '/students/notifications/1/read');
        assert.ok(calls.includes(tab));
    }
    const calls = [];
    const context = vm.createContext({document:{getElementById:()=>null}, openNotifTicket:(...args)=>calls.push(args)});
    vm.runInContext(click, context);
    await context.handleNotifClick('SUPPORT_STATUS', 42, null);
    assert.equal(calls[0][0], 42);
});
