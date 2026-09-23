// School360 - Frontend Core Logic & State Manager
const CONFIG = {
    apiBase: window.location.origin.includes('localhost') || window.location.origin.includes('127.0.0.1') || window.location.origin.startsWith('http') ? '/api' : null,
    isDemoMode: !window.location.protocol.startsWith('http')
};

// Global State
let currentUser = null;

// Initialize System
document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initDemoMode();
    Promise.resolve(checkAuth()).then(initPageNavigation);
    initAIChat();
    initNotifications();
    attachInputRestrictions();
});

// --- Global Input Validation Helpers ---
function validatePassword(password) {
    if (!password || password.trim().length < 8) {
        return { valid: false, message: "Password must be at least 8 characters long." };
    }
    return { valid: true };
}

function validateName(name) {
    if (!name || !name.trim()) {
        return { valid: false, message: "Name is required." };
    }
    const nameRegex = /^[a-zA-Z\s]+$/;
    if (!nameRegex.test(name.trim())) {
        return { valid: false, message: "Name must contain letters and spaces only (no numbers or symbols)." };
    }
    return { valid: true };
}

function validatePhone(phone) {
    if (!phone || !phone.trim()) {
        return { valid: false, message: "Phone number is required." };
    }
    const phoneRegex = /^\d{1,16}$/;
    const cleanPhone = phone.trim();
    if (!phoneRegex.test(cleanPhone)) {
        return { valid: false, message: "Phone number must contain numbers only and be at most 16 digits." };
    }
    return { valid: true };
}

function validateNIC(nic, required = false) {
    if (!nic || !nic.trim()) {
        if (required) return { valid: false, message: "NIC is required." };
        return { valid: true };
    }
    const cleanNic = nic.trim();
    const nicRegex = /^\d{16}$/;
    if (!nicRegex.test(cleanNic)) {
        return { valid: false, message: "NIC must be exactly 16 digits (numbers only, no symbols or letters)." };
    }
    return { valid: true };
}

function attachInputRestrictions() {
    // Restrict name fields to letters and spaces only
    const nameFields = document.querySelectorAll('#user-fullname, #reg-fullname, #profile-name, #member-name, #edit-member-name');
    nameFields.forEach(field => {
        field.addEventListener('input', (e) => {
            const prev = e.target.value;
            const cleaned = prev.replace(/[^a-zA-Z\s]/g, '');
            if (prev !== cleaned) {
                e.target.value = cleaned;
            }
        });
    });

    // Restrict phone fields to numbers only, max 16 digits
    const phoneFields = document.querySelectorAll('#user-contact, #reg-contact, #profile-contact, #member-phone, #edit-member-phone');
    phoneFields.forEach(field => {
        field.addEventListener('input', (e) => {
            const prev = e.target.value;
            let cleaned = prev.replace(/\D/g, '');
            if (cleaned.length > 16) {
                cleaned = cleaned.slice(0, 16);
            }
            if (prev !== cleaned) {
                e.target.value = cleaned;
            }
        });
    });

    // Restrict NIC fields to numbers only, max 16 digits
    const nicFields = document.querySelectorAll('#reg-nic');
    nicFields.forEach(field => {
        field.addEventListener('input', (e) => {
            const prev = e.target.value;
            let cleaned = prev.replace(/\D/g, '');
            if (cleaned.length > 16) {
                cleaned = cleaned.slice(0, 16);
            }
            if (prev !== cleaned) {
                e.target.value = cleaned;
            }
        });
    });
}

// --- Theme Management ---
function initTheme() {
    let savedTheme = localStorage.getItem('theme');
    if (!savedTheme) {
        savedTheme = 'dark';
        localStorage.setItem('theme', 'dark');
    }
    
    applyTheme(savedTheme === 'dark');
    
    const toggleBtns = document.querySelectorAll('#theme-toggle, .theme-toggle');
    toggleBtns.forEach(btn => {
        updateThemeToggleIcon(btn);
    });
}

function toggleTheme() {
    const isDark = document.documentElement.classList.contains('dark-theme') || document.body.classList.contains('dark-theme');
    const nextIsDark = !isDark;
    applyTheme(nextIsDark);
    localStorage.setItem('theme', nextIsDark ? 'dark' : 'light');
    document.querySelectorAll('#theme-toggle, .theme-toggle').forEach(b => updateThemeToggleIcon(b));
}
window.toggleTheme = toggleTheme;

function applyTheme(isDark) {
    const targetElements = [document.documentElement, document.body];
    targetElements.forEach(el => {
        if (!el) return;
        if (isDark) {
            el.classList.add('dark-theme');
            el.setAttribute('data-theme', 'dark');
        } else {
            el.classList.remove('dark-theme');
            el.setAttribute('data-theme', 'light');
        }
    });
}

function updateThemeToggleIcon(btn) {
    if (!btn || btn.id === 'notif-bell-btn') return;
    const isDark = document.documentElement.classList.contains('dark-theme') || document.body.classList.contains('dark-theme');
    btn.innerHTML = isDark ? '🌙' : '☀️';
    btn.setAttribute('title', isDark ? 'Switch to Light Theme' : 'Switch to Dark Theme');
    btn.setAttribute('aria-label', isDark ? 'Switch to Light Theme' : 'Switch to Dark Theme');
}

// --- Dual Mode & Local Storage Seeding for Demo Mode ---
function initDemoMode() {
    // Seed localStorage if empty so local fallback always has data
    if (!localStorage.getItem('s360_users')) {
        const defaultUsers = [
            { id: 1, username: "Sameeha",  password: "Sameeha@360",  role: "STUDENT",   fullName: "Sameeha",        email: "sameeha@school360.com",  contact: "+94 77 123 4567", qrCodeToken: "QR-STUDENT-SAMEEHA" },
            { id: 2, username: "Madawala", password: "Sameeha@360",  role: "TEACHER",   fullName: "Madawala",       email: "madawala@school360.com", contact: "+94 77 987 6543", qrCodeToken: "QR-TEACHER-MADAWALA" },
            { id: 3, username: "Aathmika", password: "Aathmika@360", role: "ADMIN",     fullName: "Aathmika Shanaz",email: "aathmika@school360.com", contact: "+94 71 555 1234", qrCodeToken: "QR-ADMIN-AATHMIKA" },
            { id: 4, username: "Fatheen",  password: "Fatheen@360",  role: "SUPPORT",   fullName: "Fatheen",        email: "fatheen@school360.com",  contact: "+94 76 222 3344", qrCodeToken: "QR-SUPPORT-FATHEEN" },
            { id: 5, username: "Kavinde",  password: "Kavinde@360",  role: "STAFF",     fullName: "Kavinde",        email: "kavinde@school360.com",  contact: "+94 72 444 5566", qrCodeToken: "QR-STAFF-KAVINDE" },
            { id: 6, username: "Shakeer",  password: "Shakeer@360",  role: "ENROLLMENT",fullName: "Shakeer",        email: "shakeer@school360.com",  contact: "+94 75 777 8899", qrCodeToken: "QR-ENROLLMENT-SHAKEER" },
            { id: 7, username: "aathu11",  password: "aathu_11",     role: "LIBRARIAN", fullName: "Aathmika",       email: "aathu@school360.com",    contact: "+94 70 000 0000", qrCodeToken: "QR-LIBRARIAN-AATHU11" }
        ];
        localStorage.setItem('s360_users', JSON.stringify(defaultUsers));

        const defaultCourses = [
            { id: 1, name: "BSc (Hons) in Software Engineering", code: "SE-101", description: "Standard program covering software development methodologies." },
            { id: 2, name: "BSc (Hons) in Business Management", code: "BM-202", description: "Provides fundamental business, leadership, and management skills." },
            { id: 3, name: "BSc (Hons) in Cyber Security", code: "CS-303", description: "Explores network defense, cryptography, and hacking." }
        ];
        localStorage.setItem('s360_courses', JSON.stringify(defaultCourses));

        const defaultStudents = [
            { id: 1, userId: 1, admissionNumber: "ADM-360-001", courseId: 1, className: "Year 3", section: "Section A", enrollmentStatus: "APPROVED_STAFF" }
        ];
        localStorage.setItem('s360_students', JSON.stringify(defaultStudents));

        const defaultModules = [
            { id: 1, name: "Advanced Programming in Java", code: "SE-302", courseId: 1, teacherId: 2 },
            { id: 2, name: "Web Applications Architecture", code: "SE-304", courseId: 1, teacherId: 2 },
            { id: 3, name: "Database Design & Management", code: "SE-306", courseId: 1, teacherId: 2 }
        ];
        localStorage.setItem('s360_modules', JSON.stringify(defaultModules));

        const defaultSchedules = [
            { id: 1, moduleId: 1, moduleName: "Advanced Programming in Java", teacherId: 2, dayOfWeek: "Monday", startTime: "08:30", endTime: "10:30", room: "Lab 3 (Level 2)", status: "ACTIVE", statusMessage: "Lecture begins at 08:30 AM" },
            { id: 2, moduleId: 2, moduleName: "Web Applications Architecture", teacherId: 2, dayOfWeek: "Wednesday", startTime: "11:00", endTime: "13:00", room: "Hall C (Ground Floor)", status: "ACTIVE", statusMessage: "Lecture begins at 11:00 AM" }
        ];
        localStorage.setItem('s360_schedules', JSON.stringify(defaultSchedules));

        const defaultAnnouncements = [
            { id: 1, title: "Welcome to School360", content: "Welcome all students and teachers to the School360 Academic portal. Please review your schedules and profile details.", createdBy: "Aathmika Shanaz", role: "ADMIN", createdDate: "2026-07-26 09:00:00" },
            { id: 2, title: "Java Assignment Due", content: "Please make sure to submit your Advanced Java assignment before the deadline on Friday.", createdBy: "Madawala", role: "TEACHER", createdDate: "2026-07-26 10:15:00" }
        ];
        localStorage.setItem('s360_announcements', JSON.stringify(defaultAnnouncements));

        const defaultTickets = [
            { id: 1, studentId: 1, studentName: "Sameeha", title: "Portal Access Slow", description: "I am experiencing slow loading speeds when opening the course materials page.", status: "OPEN", priority: "MEDIUM", escalationStatus: "NONE", createdDate: "2026-07-26 10:45:00" }
        ];
        localStorage.setItem('s360_tickets', JSON.stringify(defaultTickets));

        const defaultReplies = [
            { id: 1, ticketId: 1, senderName: "Sameeha", senderRole: "STUDENT", message: "Hi, the dashboard sometimes takes more than 5 seconds to show the timetable details. Please assist.", attachmentName: null, attachmentData: null, createdDate: "2026-07-26 10:46:00" }
        ];
        localStorage.setItem('s360_ticket_replies', JSON.stringify(defaultReplies));

        const defaultMaintenanceReports = [];
        localStorage.setItem('s360_maintenance_reports', JSON.stringify(defaultMaintenanceReports));

        const defaultAttendance = [
            { id: 1, studentId: 1, studentName: "Sameeha", scheduleId: 1, status: "PRESENT", date: "2026-07-26" }
        ];
        localStorage.setItem('s360_attendance', JSON.stringify(defaultAttendance));

        const defaultEnrollmentRequests = [
            { id: 1, studentId: 1, courseId: 1, studentName: "Sameeha", courseName: "BSc (Hons) in Software Engineering", eoStatus: "APPROVED", staffStatus: "APPROVED", remarks: "Credentials verified", admissionNumber: "ADM-360-001", className: "Year 3", section: "Section A" }
        ];
        localStorage.setItem('s360_enrollment_requests', JSON.stringify(defaultEnrollmentRequests));

        const defaultLogs = [
            { id: 1, message: "System initialized in Demo Mode.", severity: "INFO", timestamp: "2026-07-26 05:00:00" }
        ];
        localStorage.setItem('s360_logs', JSON.stringify(defaultLogs));
    }

    // ── Migration: rename old _school360 usernames to short names ──
    const usernameMap = {
        'Sameeha_school360':  'Sameeha',
        'Madawala_school360': 'Madawala',
        'Aathmika_school360': 'Aathmika',
        'Fatheen_school360':  'Fatheen',
        'Kavinde_school360':  'Kavinde',
        'Shakeer_school360':  'Shakeer',
        'Nadeesha_school360': 'Nadeesha',
    };
    // ── Migration: update fullNames & deduplicate users ──
    const fullNameByUsernameMap = {
        'Sameeha':  'Sameeha',
        'Madawala': 'Madawala',
        'Aathmika': 'Aathmika Shanaz',
        'Fatheen':  'Fatheen',
        'Kavinde':  'Kavinde',
        'Shakeer':  'Shakeer',
        'aathu11':  'Aathmika',
    };
    let existingUsers = JSON.parse(localStorage.getItem('s360_users') || '[]');
    let usersMigrated = false;
    const seenUsernames = new Set();
    const uniqueUsers = [];

    existingUsers.forEach(u => {
        if (usernameMap[u.username]) { u.username = usernameMap[u.username]; usersMigrated = true; }
        
        if (fullNameByUsernameMap[u.username] && u.fullName !== fullNameByUsernameMap[u.username]) {
            u.fullName = fullNameByUsernameMap[u.username];
            usersMigrated = true;
        } else if (!fullNameByUsernameMap[u.username] && u.fullName === 'Sameeha' && u.username !== 'Sameeha') {
            u.fullName = u.username.charAt(0).toUpperCase() + u.username.slice(1);
            usersMigrated = true;
        }

        if (!seenUsernames.has(u.username)) {
            seenUsernames.add(u.username);
            uniqueUsers.push(u);
        } else {
            usersMigrated = true;
        }
    });
    existingUsers = uniqueUsers;

    // Migration: ensure librarian account 'aathu11' exists in demo mode
    if (!existingUsers.some(u => u.username === 'aathu11')) {
        existingUsers.push({ id: existingUsers.length ? Math.max(...existingUsers.map(u => u.id)) + 1 : 1, username: "aathu11", password: "aathu_11", role: "LIBRARIAN", fullName: "Aathmika", email: "aathu@school360.com", contact: "+94 70 000 0000", qrCodeToken: "QR-LIBRARIAN-AATHU11" });
        usersMigrated = true;
    }
    if (usersMigrated) { localStorage.setItem('s360_users', JSON.stringify(existingUsers)); }
    // Migration: also rename current session user if it had old username
    const sessionUser = JSON.parse(localStorage.getItem('s360_current_user') || 'null');
    if (sessionUser && usernameMap[sessionUser.username]) {
        sessionUser.username = usernameMap[sessionUser.username];
        localStorage.setItem('s360_current_user', JSON.stringify(sessionUser));
    }
}

// --- API Wrapper Functions ---
async function apiCall(endpoint, method = 'GET', body = null) {
    if (CONFIG.isDemoMode) {
        return handleDemoRequest(endpoint, method, body);
    }
    
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    if (body) {
        options.body = JSON.stringify(body);
    }

    const res = await fetch(`${CONFIG.apiBase}${endpoint}`, options);
    const responseText = await res.text();
    if (!res.ok) {
        throw new Error(responseText || `API Call failed (${res.status})`);
    }
    // Controllers return JSON, plain text, or an empty body after a mutation.
    // Never replay a server request against a separate demo database.
    if (!responseText.trim()) return null;
    if ((res.headers.get('content-type') || '').includes('json')) {
        return JSON.parse(responseText);
    }
    return responseText;
}

// --- Session & Authentication Controls ---
const DASHBOARD_ROUTES = Object.freeze({
    STUDENT: 'student.html', TEACHER: 'teacher.html', ADMIN: 'admin.html',
    SUPPORT: 'support.html', STAFF: 'staff.html', ENROLLMENT: 'enrollment.html',
    OFFICER: 'enrollment.html', LIBRARIAN: 'librarian.html'
});

function readSessionUser() {
    try {
        const user = JSON.parse(localStorage.getItem('s360_current_user'));
        const role = typeof user?.role === 'string' ? user.role.toUpperCase() : '';
        if (user && user.id && Object.prototype.hasOwnProperty.call(DASHBOARD_ROUTES, role)) {
            return { ...user, role: role === 'OFFICER' ? 'ENROLLMENT' : role };
        }
    } catch (_) { /* Expired or malformed browser session. */ }
    localStorage.removeItem('s360_current_user');
    return null;
}

function checkAuth() {
    currentUser = readSessionUser();
    const path = window.location.pathname;
    const isLoginPath = path.endsWith('index.html') || path.endsWith('/') || path === '';
    const isContactPath = path.endsWith('contact.html');
    const isPublicPath = isLoginPath || isContactPath;
    
    if (currentUser) {
        const page = path.split('/').pop();
        const isDashboard = Object.values(DASHBOARD_ROUTES).includes(page);
        if (isDashboard && page !== DASHBOARD_ROUTES[currentUser.role]) {
            redirectToDashboard(currentUser.role);
        }
    } else {
        if (!isPublicPath) {
            window.location.href = 'index.html';
        }
    }
}

function redirectToDashboard(role) {
    const normalizedRole = typeof role === 'string' ? role.toUpperCase() : '';
    window.location.href = Object.prototype.hasOwnProperty.call(DASHBOARD_ROUTES, normalizedRole)
        ? DASHBOARD_ROUTES[normalizedRole] : 'index.html';
}

function logout() {
    currentUser = null;
    localStorage.removeItem('s360_current_user');
    window.location.href = 'index.html';
}

// Connect existing page controls without changing their markup or presentation.
function initPageNavigation() {
    const page = window.location.pathname.split('/').pop();
    const publicPage = !page || page === 'index.html' || page === 'contact.html';
    if (publicPage) {
        document.addEventListener('click', event => {
            const link = event.target.closest('a');
            if (!link) return;
            const handler = link.getAttribute('onclick') || '';
            const portalLink = link.id === 'nav-portal-btn' ||
                handler.includes('openLoginModal') || link.textContent.trim() === 'Portal Login';
            if (!portalLink) return;
            event.preventDefault();
            event.stopImmediatePropagation();
            const user = readSessionUser();
            if (user) redirectToDashboard(user.role);
            else if (typeof openLoginModal === 'function') openLoginModal();
            else window.location.href = 'index.html?login=true';
        }, true);
        const params = new URLSearchParams(window.location.search);
        if (params.get('scanId') === 'true' && typeof openQRScanner === 'function') openQRScanner();
        else if (params.get('login') === 'true' && typeof openLoginModal === 'function') openLoginModal();
        return;
    }
    if (!currentUser || DASHBOARD_ROUTES[currentUser.role] !== page) return;
    const functionName = typeof window.showPanel === 'function' ? 'showPanel' : 'switchTab';
    const original = window[functionName];
    if (typeof original !== 'function') return;
    const prefix = functionName === 'showPanel' ? 'panel-' : 'tab-';
    const defaultSection = document.querySelector('.sidebar-item.active')?.id.replace(/^menu-/, '');
    let restoring = false;
    window[functionName] = function(section, ...args) {
        if (!document.getElementById(prefix + section)) return;
        const result = original.call(this, section, ...args);
        if (!restoring && window.location.hash !== '#' + section) {
            window.history.pushState(null, '', '#' + section);
        }
        return result;
    };
    // Existing href="#" controls must not overwrite the section URL after onclick.
    document.addEventListener('click', event => {
        if (event.target.closest('.sidebar-item a')) event.preventDefault();
    });
    const restoreSection = () => {
        const section = window.location.hash.slice(1) || defaultSection;
        restoring = true;
        try { window[functionName](section); } finally { restoring = false; }
    };
    window.addEventListener('popstate', restoreSection);
    if (window.location.hash) restoreSection();
}

// Reconcile sessions when another portal tab signs out or switches accounts.
window.addEventListener('storage', event => {
    if (event.key === 's360_current_user' || event.key === null) window.location.reload();
});
window.addEventListener('pageshow', event => {
    if (event.persisted) window.location.reload();
});

// --- SVG-Based QR Code Generator (Inline & Safe) ---
function getQRWebUrl(token) {
    if (!token) return window.location.href;
    if (typeof token === 'string' && (token.startsWith('http://') || token.startsWith('https://'))) {
        return token;
    }
    let origin = window.location.origin;
    if (!origin || origin === 'null' || origin.startsWith('file')) {
        origin = window.location.protocol && window.location.protocol.startsWith('http')
            ? (window.location.protocol + '//' + window.location.host)
            : 'http://localhost:8080';
    }
    let pathname = window.location.pathname || '/';
    let basePath = pathname.substring(0, pathname.lastIndexOf('/') + 1);
    if (!basePath.endsWith('/')) basePath += '/';
    return `${origin}${basePath}index.html?qrToken=${encodeURIComponent(token)}`;
}

function escapeJsToken(str) {
    return (str || '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function generateQRCodeSVG(text, size = 150) {
    // Ensure payload is an absolute web URL so mobile phone cameras automatically open the site
    const qrTargetUrl = getQRWebUrl(text);
    const pxSize = Math.max(size * 2, 200);
    const qrImgUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${pxSize}x${pxSize}&margin=1&data=${encodeURIComponent(qrTargetUrl)}`;

    return `<img src="${qrImgUrl}" alt="QR Code" width="${size}" height="${size}" style="display:block;margin:0 auto;background:#ffffff;padding:4px;border-radius:6px;box-shadow:0 2px 6px rgba(0,0,0,0.15);" crossorigin="anonymous" onerror="this.onerror=null;this.outerHTML=generateOfflineVectorQRSVG('${escapeJsToken(qrTargetUrl)}', ${size});" />`;
}

function generateOfflineVectorQRSVG(text, size = 150) {
    const matrix = buildQRMatrix(text);
    const n = matrix.length;
    const cellSize = (size / n);
    let svg = `<svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}" xmlns="http://www.w3.org/2000/svg" style="background:#ffffff;padding:4px;border-radius:6px;">`;
    svg += `<rect width="${size}" height="${size}" fill="#ffffff" />`;
    for (let r = 0; r < n; r++) {
        for (let c = 0; c < n; c++) {
            if (matrix[r][c]) {
                svg += `<rect x="${(c * cellSize).toFixed(2)}" y="${(r * cellSize).toFixed(2)}" width="${(cellSize + 0.05).toFixed(2)}" height="${(cellSize + 0.05).toFixed(2)}" fill="#000000" />`;
            }
        }
    }
    svg += `</svg>`;
    return svg;
}

function buildQRMatrix(text) {
    const N = 29; // Version 3 QR Code (29x29 matrix)
    const m = Array.from({ length: N }, () => Array(N).fill(false));
    const reserved = Array.from({ length: N }, () => Array(N).fill(false));

    function mark(r, c, val) {
        if (r >= 0 && r < N && c >= 0 && c < N) {
            reserved[r][c] = true;
            m[r][c] = val;
        }
    }

    // 1. Finder patterns
    const drawFinder = (row, col) => {
        for (let r = -1; r <= 7; r++) {
            for (let c = -1; c <= 7; c++) {
                const mr = row + r, mc = col + c;
                if (mr >= 0 && mr < N && mc >= 0 && mc < N) {
                    reserved[mr][mc] = true;
                    if (r >= 0 && r < 7 && c >= 0 && c < 7) {
                        const isBorder = (r === 0 || r === 6 || c === 0 || c === 6);
                        const isCenter = (r >= 2 && r <= 4 && c >= 2 && c <= 4);
                        m[mr][mc] = isBorder || isCenter;
                    } else {
                        m[mr][mc] = false;
                    }
                }
            }
        }
    };
    drawFinder(0, 0);
    drawFinder(0, N - 7);
    drawFinder(N - 7, 0);

    // 2. Alignment pattern at (20, 20)
    for (let r = -2; r <= 2; r++) {
        for (let c = -2; c <= 2; c++) {
            mark(20 + r, 20 + c, Math.abs(r) === 2 || Math.abs(c) === 2 || (r === 0 && c === 0));
        }
    }

    // 3. Timing patterns
    for (let i = 8; i < N - 8; i++) {
        mark(6, i, i % 2 === 0);
        mark(i, 6, i % 2 === 0);
    }
    mark(N - 8, 8, true);

    // 4. Reserve format areas
    for (let i = 0; i <= 8; i++) {
        if (i < N) { reserved[8][i] = true; reserved[i][8] = true; }
        if (N - 1 - i >= 0) { reserved[8][N - 1 - i] = true; reserved[N - 1 - i][8] = true; }
    }

    // 5. Data payload bits encoding (Byte Mode)
    const bits = [0, 1, 0, 0]; // Mode indicator 0100
    const len = text.length;
    for (let b = 7; b >= 0; b--) bits.push((len >> b) & 1);
    for (let i = 0; i < text.length; i++) {
        const code = text.charCodeAt(i);
        for (let b = 7; b >= 0; b--) bits.push((code >> b) & 1);
    }
    // Pad bits
    const padPattern = [1,1,1,0,1,1,0,0, 0,0,0,1,0,0,0,1];
    let p = 0;
    while (bits.length < 350) {
        bits.push(padPattern[p % padPattern.length]);
        p++;
    }

    // Place bits in matrix (zig-zag right to left)
    let bitIdx = 0;
    let dir = -1;
    for (let col = N - 1; col > 0; col -= 2) {
        if (col === 6) col--;
        const rowRange = dir === -1 ? [...Array(N).keys()].reverse() : [...Array(N).keys()];
        for (const r of rowRange) {
            for (const c of [col, col - 1]) {
                if (!reserved[r][c]) {
                    const bit = bitIdx < bits.length ? bits[bitIdx++] === 1 : false;
                    const mask = (r + c) % 2 === 0;
                    m[r][c] = bit ^ mask;
                }
            }
        }
        dir = -dir;
    }

    // Format Info bits (ECC L, Mask 0: 101010000010010)
    const fmt = [1,0,1,0,1,0,0,0,0,0,1,0,0,1,0];
    let f = 0;
    for (let c = 0; c <= 8; c++) { if (c !== 6) m[8][c] = fmt[f++] === 1; }
    for (let r = 7; r >= 0; r--) { if (r !== 6) m[r][8] = fmt[f++] === 1; }
    f = 0;
    for (let r = N - 1; r >= N - 7; r--) { m[r][8] = fmt[f++] === 1; }
    for (let c = N - 8; c < N; c++) { m[8][c] = fmt[f++] === 1; }

    return m;
}

// --- CSV/Excel Export Utility ---
function exportTableToCSV(tableId, filename = 'export.csv') {
    const table = document.getElementById(tableId);
    if (!table) return;
    
    let csv = [];
    const rows = table.querySelectorAll('tr');
    
    for (let i = 0; i < rows.length; i++) {
        let row = [];
        const cols = rows[i].querySelectorAll('td, th');
        
        for (let j = 0; j < cols.length; j++) {
            // Clean text contents
            let text = cols[j].innerText.replace(/"/g, '""');
            row.push(`"${text}"`);
        }
        csv.push(row.join(','));
    }
    
    const csvContent = "data:text/csv;charset=utf-8," + csv.join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast("File Export", "Roster successfully exported to CSV/Excel", "success");
}

// --- Real-time Notifications Engine ---
function initNotifications() {
    const notifyContainer = document.createElement('div');
    notifyContainer.className = 'notification-container';
    notifyContainer.id = 'toast-container';
    document.body.appendChild(notifyContainer);
}

function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    // For creation success messages, omit sub-line and display 'Created successfully'
    if ((message && message.toLowerCase().includes('created successfully')) || 
        (title && title.toLowerCase().includes('created')) ||
        (type === 'success' && message && message.toLowerCase().includes('created'))) {
        title = "Created successfully";
        message = "";
    }
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'warning') icon = '⚠️';
    if (type === 'danger') icon = '🚨';
    
    const messageHtml = (message && message.trim()) ? `<p style="margin-top:2px;margin-bottom:0;">${message}</p>` : '';
    
    toast.innerHTML = `
        <div style="font-size: 1.2rem; display: flex; align-items: center;">${icon}</div>
        <div class="toast-content" style="display:flex; flex-direction:column; justify-content:center;">
            <h5 style="margin:0; font-size:0.9rem; font-weight:700;">${title}</h5>
            ${messageHtml}
        </div>
    `;
    
    container.appendChild(toast);
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse forwards';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// --- Smart AI Assist Chat Box on Dashboards & All Pages ---
let localTrainedKnowledge = {
    "what is school360": "School360 is a unified academic SaaS protocol for digital campus management. It handles admissions, digital QR ID badges, automated class timetables, support tickets, library catalogs, and real-time attendance.",
    "how to login": "You can log in using your registered username & password, or click 'Scan ID' in the navigation bar to scan your Student Digital ID QR Code.",
    "how to enroll": "Navigate to 'Course Enrollment' on your dashboard, pick your course, and submit an application for Enrollment Officer sign-off.",
    "library rules": "Books can be borrowed for 14 days with your QR ID card. Contact Librarian Aathmika for renewals."
};

function initAIChat() {
    if (document.getElementById('ai-launcher')) return;
    
    const launcher = document.createElement('div');
    launcher.className = 'ai-chat-launcher';
    launcher.id = 'ai-launcher';
    launcher.innerHTML = '🤖<div class="ai-launcher-badge" title="AI Online & Learning"></div>';
    launcher.title = 'Ask School360 Intelligent AI';
    document.body.appendChild(launcher);
    
    const chatWindow = document.createElement('div');
    chatWindow.className = 'ai-chat-window';
    chatWindow.id = 'ai-chat-window';
    
    const role = currentUser ? currentUser.role.toUpperCase() : 'GUEST';
    
    chatWindow.innerHTML = `
        <div class="ai-chat-header">
            <div class="title-wrap">
                <h3>🤖 School360 AI Assistant</h3>
                <div class="subtitle"><span style="color:#6b3294;">●</span> Live DB Connected & Learned</div>
            </div>
            <div class="ai-chat-header-actions">
                <button class="ai-header-btn" onclick="toggleAITrainPanel()" title="Train AI with new Q&A data">🎓 Train</button>
                <button class="ai-header-btn" onclick="clearAIChat()" title="Clear chat history">🗑️</button>
                <span style="cursor:pointer;font-size:1.1rem;margin-left:0.2rem;" onclick="toggleAIChat()">✕</span>
            </div>
        </div>
        
        <!-- Prompt Quick Suggestion Pills -->
        <div class="ai-chat-pills" id="ai-chat-pills">
            <div class="ai-pill" onclick="sendAIMessage('📊 How many students are registered?')">📊 Student Count</div>
            <div class="ai-pill" onclick="sendAIMessage('📚 Show all available courses')">📚 Courses List</div>
            <div class="ai-pill" onclick="sendAIMessage('🗓️ Show class timetable schedule')">🗓️ Class Schedule</div>
            <div class="ai-pill" onclick="sendAIMessage('📢 Latest announcements')">📢 Campus News</div>
            <div class="ai-pill" onclick="sendAIMessage('🎫 Support tickets status')">🎫 Support Status</div>
            <div class="ai-pill" onclick="sendAIMessage('📖 Library catalog & rules')">📖 Library Info</div>
        </div>

        <!-- Inline Training Panel -->
        <div class="ai-train-panel" id="ai-train-panel">
            <h4>🎓 Train AI with New Data</h4>
            <input type="text" id="ai-train-q" class="form-input" placeholder="Question (e.g. Pass mark?)" style="font-size:0.78rem;padding:0.4rem;" />
            <input type="text" id="ai-train-a" class="form-input" placeholder="Answer (e.g. 50% for all modules)" style="font-size:0.78rem;padding:0.4rem;" />
            <button class="btn btn-primary" style="font-size:0.78rem;padding:0.35rem;" onclick="submitAITrainingData()">Train Model Now</button>
        </div>
        
        <div class="ai-chat-messages" id="ai-chat-messages">
            <div class="ai-bubble ai">
                👋 Hello! I am your <strong>School360 Intelligent AI Assistant</strong>.<br>
                I am connected to the live database and trained on system data. Ask me anything about student counts, courses, schedules, support tickets, library material, or general knowledge!
            </div>
        </div>
        
        <div class="ai-chat-input-bar">
            <input type="text" id="ai-input" class="form-input" placeholder="Ask any question..." style="flex:1;font-size:0.85rem;" />
            <button class="btn btn-primary" style="padding:0.4rem 0.9rem;" onclick="sendAIMessage()">Send</button>
        </div>
    `;
    document.body.appendChild(chatWindow);
    
    launcher.addEventListener('click', toggleAIChat);
    
    document.getElementById('ai-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendAIMessage();
    });
}

function toggleAIChat() {
    const win = document.getElementById('ai-chat-window');
    win.classList.toggle('active');
}

function toggleAITrainPanel() {
    const panel = document.getElementById('ai-train-panel');
    panel.classList.toggle('active');
}

function clearAIChat() {
    const msgsContainer = document.getElementById('ai-chat-messages');
    msgsContainer.innerHTML = `
        <div class="ai-bubble ai">
            Chat cleared! I am ready to answer any questions or learn new data.
        </div>
    `;
}

function submitAITrainingData() {
    const qInput = document.getElementById('ai-train-q');
    const aInput = document.getElementById('ai-train-a');
    const q = qInput.value.trim();
    const a = aInput.value.trim();
    
    if (!q || !a) {
        showNotification("Please provide both a Question and an Answer to train the AI.", "warning");
        return;
    }
    
    // Save to local memory
    localTrainedKnowledge[q.toLowerCase()] = a;
    
    // Attempt backend API training sync
    if (CONFIG.apiBase) {
        fetch(`${CONFIG.apiBase}/ai/train`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ question: q, answer: a })
        }).catch(err => console.log("Offline train fallback active:", err));
    }
    
    qInput.value = '';
    aInput.value = '';
    toggleAITrainPanel();
    
    const msgsContainer = document.getElementById('ai-chat-messages');
    const aiBubble = document.createElement('div');
    aiBubble.className = 'ai-bubble ai';
    aiBubble.innerHTML = `🧠 <strong>[Training Complete]</strong><br>I have learned the response for: <em>"${q}"</em>! Ask me about it anytime.`;
    msgsContainer.appendChild(aiBubble);
    msgsContainer.scrollTop = msgsContainer.scrollHeight;
    
    showNotification("AI Chatbot trained successfully with new dataset!", "success");
}

function sendAIMessage(customText) {
    const input = document.getElementById('ai-input');
    const msg = customText || input.value.trim();
    if (!msg) return;
    
    const msgsContainer = document.getElementById('ai-chat-messages');
    
    // Append User bubble
    const userBubble = document.createElement('div');
    userBubble.className = 'ai-bubble user';
    userBubble.innerText = msg;
    msgsContainer.appendChild(userBubble);
    
    if (!customText) input.value = '';
    msgsContainer.scrollTop = msgsContainer.scrollHeight;
    
    // Append Typing Indicator
    const typingIndicator = document.createElement('div');
    typingIndicator.className = 'ai-typing-indicator';
    typingIndicator.id = 'ai-typing-indicator';
    typingIndicator.innerHTML = '<div class="ai-typing-dot"></div><div class="ai-typing-dot"></div><div class="ai-typing-dot"></div>';
    msgsContainer.appendChild(typingIndicator);
    msgsContainer.scrollTop = msgsContainer.scrollHeight;

    const role = currentUser ? currentUser.role.toUpperCase() : 'GUEST';
    const username = currentUser ? currentUser.username : 'GUEST';
    
    // Try Real Backend AI API First
    if (CONFIG.apiBase) {
        fetch(`${CONFIG.apiBase}/ai/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                message: msg,
                userRole: role,
                username: username,
                pageContext: window.location.pathname
            })
        })
        .then(res => res.json())
        .then(data => {
            removeTypingIndicator();
            appendAIResponseBubble(data.response || generateAIResponse(msg));
        })
        .catch(err => {
            console.log("Using intelligent client-side fallback engine:", err);
            setTimeout(() => {
                removeTypingIndicator();
                appendAIResponseBubble(generateAIResponse(msg));
            }, 600);
        });
    } else {
        setTimeout(() => {
            removeTypingIndicator();
            appendAIResponseBubble(generateAIResponse(msg));
        }, 600);
    }
}

function removeTypingIndicator() {
    const indicator = document.getElementById('ai-typing-indicator');
    if (indicator) indicator.remove();
}

function appendAIResponseBubble(responseText) {
    const msgsContainer = document.getElementById('ai-chat-messages');
    const aiBubble = document.createElement('div');
    aiBubble.className = 'ai-bubble ai';
    aiBubble.innerHTML = formatAIMarkdown(responseText);
    msgsContainer.appendChild(aiBubble);
    msgsContainer.scrollTop = msgsContainer.scrollHeight;
}

function formatAIMarkdown(text) {
    if (!text) return '';
    let html = text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
    
    // Bold
    html = html.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
    // Code
    html = html.replace(/`(.*?)`/g, "<code>$1</code>");
    // Line breaks
    html = html.replace(/\n/g, "<br>");
    return html;
}

function generateAIResponse(query) {
    const lowerQuery = query.toLowerCase().trim();
    const role = currentUser ? currentUser.role.toUpperCase() : 'GUEST';

    // 1. Check Trained Memory Map
    for (let k in localTrainedKnowledge) {
        if (lowerQuery.includes(k) || k.includes(lowerQuery)) {
            return `🧠 **[Trained Knowledge Match]**\n${localTrainedKnowledge[k]}`;
        }
    }
    
    // 2. Math Calculations Engine
    const mathMatch = lowerQuery.match(/(\d+(\.\d+)?)\s*([\+\-\*/%])\s*(\d+(\.\d+)?)/);
    if (mathMatch) {
        const n1 = parseFloat(mathMatch[1]);
        const op = mathMatch[3];
        const n2 = parseFloat(mathMatch[4]);
        let r = 0;
        if (op === '+') r = n1 + n2;
        if (op === '-') r = n1 - n2;
        if (op === '*') r = n1 * n2;
        if (op === '/') r = n2 !== 0 ? n1 / n2 : 'Error: Division by zero';
        if (op === '%') r = n1 % n2;
        return `🧮 **AI Math Engine Calculation:**\n\`${n1} ${op} ${n2} = ${r}\``;
    }

    // 3. Dynamic Local Storage Queries (Demo / Fallback Mode)
    const getStorage = (key) => {
        try { return JSON.parse(localStorage.getItem(key)) || []; } catch(e) { return []; }
    };

    if (lowerQuery.includes('student count') || lowerQuery.includes('how many student') || lowerQuery.includes('total student')) {
        const students = getStorage('students');
        const count = students.length > 0 ? students.length : 12;
        return `📊 **Live Database Insight:** There are currently **${count} active students** registered in the School360 system.`;
    }

    if (lowerQuery.includes('course') || lowerQuery.includes('subject')) {
        const courses = getStorage('courses');
        if (courses.length > 0) {
            let res = `📚 **Live Academic Courses Directory (${courses.length} Total):**\n\n`;
            courses.forEach(c => {
                res += `• **${c.courseCode || c.code}**: ${c.courseName || c.name} (${c.credits || 3} Credits)\n`;
            });
            return res;
        }
        return `📚 **Live Academic Courses Directory:**\n• **CS101**: Introduction to Computer Science (4 Credits)\n• **ENG201**: Advanced Academic Writing (3 Credits)\n• **MATH301**: Linear Algebra & Differential Equations (4 Credits)\n• **PHYS102**: Applied University Physics (4 Credits)`;
    }

    if (lowerQuery.includes('announcement') || lowerQuery.includes('news') || lowerQuery.includes('notice')) {
        const news = getStorage('announcements');
        if (news.length > 0) {
            let res = `📢 **Latest Campus Announcements:**\n\n`;
            news.slice(0, 3).forEach(n => {
                res += `📌 **${n.title}**\n${n.content}\n\n`;
            });
            return res.trim();
        }
        return `📢 **Latest Campus Announcements:**\n📌 **Mid-Semester Examination Schedule Published**\nExams start on the 15th of next month. Review hall allocations in your Student Portal.\n\n📌 **Library Digital Resource Portal Live**\nStudents can access IEEE and ScienceDirect publications using Digital QR credentials.`;
    }

    if (lowerQuery.includes('schedule') || lowerQuery.includes('timetable') || lowerQuery.includes('class time')) {
        const schedules = getStorage('schedules');
        if (schedules.length > 0) {
            let res = `🗓️ **Live Class Schedule (${schedules.length} Sessions):**\n\n`;
            schedules.forEach(s => {
                res += `• **${s.moduleName || s.module}** (${s.hallNumber || s.hall})\n  🕒 ${s.startTime} - ${s.endTime} | Teacher: ${s.teacherName || 'Faculty'}\n`;
            });
            return res;
        }
        return `🗓️ **Live Class Schedule:**\n• **Computer Science 101** (Lab Hall 3B)\n  🕒 09:00 AM - 11:00 AM | Teacher: Prof. Madawala\n• **Linear Algebra** (Lecture Hall 1A)\n  🕒 01:30 PM - 03:30 PM | Teacher: Dr. Sameeha`;
    }

    if (lowerQuery.includes('ticket') || lowerQuery.includes('support status') || lowerQuery.includes('helpdesk')) {
        const tickets = getStorage('tickets');
        const count = tickets.length > 0 ? tickets.length : 5;
        return `🎫 **Live Helpdesk Stats:**\n• Total Tickets: **${count}**\n• Status: Active Queue Monitoring\n\nYou can submit new support tickets or attach screenshots from the **Support Center** page.`;
    }

    if (lowerQuery.includes('librarian') || lowerQuery.includes('library') || lowerQuery.includes('book')) {
        return `📖 **Central Library Service:**\n• Librarian: **Aathmika** (\`aathu11\`)\n• Location: Academic Block B, Level 2\n• Hours: Mon-Fri (08:00 AM - 05:00 PM)\n• Checkout: Scan your Digital Student QR ID badge at the counter.`;
    }

    if (lowerQuery.includes('hello') || lowerQuery.includes('hi ') || lowerQuery.startsWith('hi')) {
        return `👋 Hello! How can I assist you in your role as a **${role}** on School360 today?`;
    }

    if (lowerQuery.includes('who are you') || lowerQuery.includes('what can you do')) {
        return `🤖 **About Me:** I am the real-time AI Assistant for School360. I query live system data (students, courses, timetables, tickets, announcements) and learn from custom Q&A training datasets!`;
    }

    if (lowerQuery.includes('login') || lowerQuery.includes('qr')) {
        return "You can log in either by entering your registered username & password or by clicking 'Scan ID' to present your encrypted Digital Student QR code badge.";
    }

    // Role-specific Fallback
    if (role === 'STUDENT') {
        if (lowerQuery.includes('enroll')) return "To enroll, go to 'Course Enrollment', pick your course, and submit. Status goes to 'Pending' for Enrollment Officer clearance.";
        if (lowerQuery.includes('grade')) return "Grades and transcript summaries are published in your Academic Profile section after faculty sign-off.";
    }

    if (role === 'ADMIN') {
        if (lowerQuery.includes('user')) return "Admin account managers can edit profiles, create roles, or disable accounts from the User Manager table.";
    }

    return `🤖 **AI Assistance:**\nI have analyzed your query regarding *"${query}"*.\n\nYou can ask about **"student count"**, **"available courses"**, **"class schedule"**, **"announcements"**, or **"support status"** for real-time system data, or train me with new answers using the 🎓 **Train** button!`;
}

function handleDemoRequest(endpoint, method, body) {
    // Ensure demo localStorage is seeded
    if (typeof initDemoMode === 'function') {
        initDemoMode();
    }

    // Helper to get collections safely
    const get = (key) => {
        try {
            const val = JSON.parse(localStorage.getItem(key));
            return Array.isArray(val) ? val : [];
        } catch (e) {
            return [];
        }
    };
    const set = (key, val) => localStorage.setItem(key, JSON.stringify(val));

    // Simple router simulation
    if (endpoint === '/auth/directory' && method === 'GET') {
        let users = get('s360_users');
        if (!users || users.length === 0) {
            if (typeof initDemoMode === 'function') initDemoMode();
            users = get('s360_users');
        }
        return users
            .filter(user => user && user.qrCodeToken)
            .map(user => ({
                id: user.id,
                fullName: user.fullName || user.username,
                role: user.role,
                qrCodeToken: user.qrCodeToken
            }));
    }

    if (endpoint === '/auth/login' && method === 'POST') {
        const users = get('s360_users');
        const reqUsername = (body.username || '').trim().toLowerCase();
        const reqPassword = (body.password || '').trim();
        const match = users.find(u => u && u.username && u.username.toLowerCase() === reqUsername && u.password === reqPassword);
        if (match) return match;
        throw new Error("Invalid username or password.");
    }
    
    if (endpoint === '/auth/register' && method === 'POST') {
        const users = get('s360_users');
        if (users.some(u => u.username === body.username)) {
            throw new Error("This username is already taken. Please choose another.");
        }
        if (users.some(u => u.email === body.email)) {
            throw new Error("An account with this email already exists.");
        }
        const newUser = {
            id: users.length ? Math.max(...users.map(u => u.id)) + 1 : 1,
            username: body.username,
            password: body.password,
            role: "STUDENT",
            fullName: body.fullName,
            email: body.email,
            contact: body.contact,
            qrCodeToken: "QR-STUDENT-" + body.username.toUpperCase()
        };
        users.push(newUser);
        set('s360_users', users);

        const students = get('s360_students');
        students.push({
            id: students.length ? Math.max(...students.map(s => s.id)) + 1 : 1,
            userId: newUser.id,
            admissionNumber: "",
            courseId: null,
            className: "",
            section: "",
            enrollmentStatus: null
        });
        set('s360_students', students);

        const logs = get('s360_logs');
        logs.push({ id: logs.length + 1, message: `New student self-registered: ${newUser.fullName} (${newUser.username}).`, severity: "INFO", timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19) });
        set('s360_logs', logs);

        return newUser;
    }

    if (endpoint === '/auth/qr-login' && method === 'POST') {
        const users = get('s360_users');
        const tokenInput = (body.qrToken || '').trim().toLowerCase();
        const match = users.find(u => {
            if (!u) return false;
            const token = (u.qrCodeToken || '').toLowerCase();
            const uname = (u.username || '').toLowerCase();
            const role = (u.role || '').toLowerCase();
            return token === tokenInput ||
                   tokenInput.includes(token) ||
                   token.includes(tokenInput) ||
                   tokenInput.includes(uname) ||
                   (role.length > 3 && tokenInput.includes(role));
        });
        if (match) return match;
        throw new Error("Invalid or unrecognized QR Login Code.");
    }

    // Student profile endpoints
    if (endpoint.startsWith('/students/profile/') && endpoint.endsWith('/edit') && method === 'POST') {
        const parts = endpoint.split('/');
        const userId = parseInt(parts[3]);
        const users = get('s360_users');
        const userIdx = users.findIndex(u => u.id === userId);
        if (userIdx !== -1) {
            if (body.fullName) users[userIdx].fullName = body.fullName;
            if (body.email) users[userIdx].email = body.email;
            if (body.contact) users[userIdx].contact = body.contact;
            set('s360_users', users);
            
            const students = get('s360_students');
            const student = students.find(s => s.userId === userId);
            student.user = users[userIdx];
            return student;
        }
        throw new Error("Student profile not found.");
    }

    if (endpoint.startsWith('/students/profile/') && method === 'GET') {
        const parts = endpoint.split('/');
        const userId = parseInt(parts[3]);
        const students = get('s360_students');
        const users = get('s360_users');
        const student = students.find(s => s.userId === userId);
        if (student) {
            student.user = users.find(u => u.id === userId);
            return student;
        }
        throw new Error("Student profile not found.");
    }

    if (endpoint === '/students/courses' && method === 'GET') {
        return get('s360_courses');
    }

    if (endpoint === '/students/enroll' && method === 'POST') {
        const students = get('s360_students');
        const courses = get('s360_courses');
        const enrollments = get('s360_enrollment_requests');
        
        const student = students.find(s => s.id === body.studentId);
        const course = courses.find(c => c.id === body.courseId);
        
        const req = {
            id: enrollments.length + 1,
            studentId: student.id,
            courseId: course.id,
            studentName: student.user ? student.user.fullName : "Sameeha Perera",
            courseName: course.name,
            eoStatus: "PENDING",
            staffStatus: "PENDING",
            remarks: "",
            admissionNumber: "",
            className: "",
            section: ""
        };
        
        student.courseId = course.id;
        student.enrollmentStatus = "PENDING_EO";
        
        enrollments.push(req);
        set('s360_enrollment_requests', enrollments);
        set('s360_students', students);
        return req;
    }

    if (endpoint.endsWith('/modules') && endpoint.startsWith('/students/') && method === 'GET') {
        const studentId = parseInt(endpoint.split('/')[2]);
        const student = get('s360_students').find(s => s.id === studentId);
        if (student && student.enrollmentStatus === 'APPROVED_STAFF') {
            return get('s360_modules').filter(m => m.courseId === student.courseId);
        }
        return [];
    }

    if (endpoint.endsWith('/timetable') && endpoint.startsWith('/students/') && method === 'GET') {
        const studentId = parseInt(endpoint.split('/')[2]);
        const student = get('s360_students').find(s => s.id === studentId);
        if (student && student.enrollmentStatus === 'APPROVED_STAFF') {
            const modules = get('s360_modules').filter(m => m.courseId === student.courseId);
            const mIds = modules.map(m => m.id);
            return get('s360_schedules').filter(s => mIds.includes(s.moduleId));
        }
        return [];
    }

    if (endpoint === '/students/announcements' && method === 'GET') {
        return get('s360_announcements');
    }

    if (endpoint === '/students/tickets' && method === 'POST') {
        const tickets = get('s360_tickets') || [];
        const students = get('s360_students') || [];
        const users = get('s360_users') || [];
        const studId = parseInt(body.studentId);
        const stud = students.find(s => s.id === studId);
        const user = stud ? users.find(u => u.id === stud.userId) : null;
        
        const tick = {
            id: tickets.length + 1,
            studentId: studId || (stud ? stud.id : 1),
            studentName: user ? user.fullName : (stud ? stud.name : "Student"),
            title: body.title,
            description: body.description,
            status: "OPEN",
            priority: "LOW",
            escalationStatus: "NONE",
            createdDate: new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        tickets.push(tick);
        set('s360_tickets', tickets);
        return tick;
    }

    if (endpoint.startsWith('/students/') && endpoint.endsWith('/tickets') && method === 'GET') {
        const studId = parseInt(endpoint.split('/')[2]);
        return get('s360_tickets').filter(t => t.studentId === studId);
    }

    if (endpoint.startsWith('/students/tickets/') && endpoint.endsWith('/replies') && method === 'GET') {
        const tId = parseInt(endpoint.split('/')[3]);
        return get('s360_ticket_replies').filter(r => r.ticketId === tId);
    }

    if (endpoint.startsWith('/students/tickets/') && endpoint.endsWith('/replies') && method === 'POST') {
        const tId = parseInt(endpoint.split('/')[3]);
        const replies = get('s360_ticket_replies');
        const reply = {
            id: replies.length + 1,
            ticketId: tId,
            senderName: body.senderName,
            senderRole: "STUDENT",
            message: body.message,
            attachmentName: body.attachmentName,
            attachmentData: body.attachmentData,
            createdDate: new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        replies.push(reply);
        set('s360_ticket_replies', replies);
        
        // Mark ticket open
        const tickets = get('s360_tickets');
        const tick = tickets.find(t => t.id === tId);
        if (tick && (tick.status === 'CLOSED' || tick.status === 'RESOLVED')) {
            tick.status = 'OPEN';
            set('s360_tickets', tickets);
        }
        return reply;
    }

    if (endpoint.startsWith('/students/enrollment-request/') && method === 'DELETE') {
        const studentId = parseInt(endpoint.split('/')[3]);
        const students = get('s360_students');
        const stud = students.find(s => s.id === studentId);
        if (stud && stud.enrollmentStatus === 'PENDING_EO') {
            stud.enrollmentStatus = null;
            stud.courseId = null;
            set('s360_students', students);
            
            const reqs = get('s360_enrollment_requests');
            const filtered = reqs.filter(r => !(r.studentId === studentId && r.eoStatus === 'PENDING'));
            set('s360_enrollment_requests', filtered);
            return "Enrollment request cancelled successfully.";
        }
        throw new Error("Unable to cancel enrollment request.");
    }

    // Teacher Dashboard mocks
    if (endpoint.startsWith('/teachers/') && endpoint.endsWith('/modules') && method === 'GET') {
        const tId = parseInt(endpoint.split('/')[2]);
        return get('s360_modules').filter(m => m.teacherId === tId);
    }

    if (endpoint === '/teachers/modules' && method === 'POST') {
        const modules = get('s360_modules');
        body.id = modules.length + 1;
        modules.push(body);
        set('s360_modules', modules);
        return body;
    }

    if (endpoint.startsWith('/teachers/modules/') && method === 'PUT') {
        const mId = parseInt(endpoint.split('/')[3]);
        const modules = get('s360_modules');
        const idx = modules.findIndex(m => m.id === mId);
        if (idx !== -1) {
            modules[idx].name = body.name;
            modules[idx].code = body.code;
            modules[idx].courseId = body.courseId;
            set('s360_modules', modules);
            return modules[idx];
        }
        throw new Error("Module not found.");
    }

    if (endpoint.startsWith('/teachers/modules/') && endpoint.endsWith('/materials') && method === 'POST') {
        const mId = parseInt(endpoint.split('/')[3]);
        const modules = get('s360_modules');
        const idx = modules.findIndex(m => m.id === mId);
        if (idx === -1) throw new Error("Module not found.");
        if (!modules[idx].materials) modules[idx].materials = [];
        const material = {
            id: modules[idx].materials.length ? Math.max(...modules[idx].materials.map(x => x.id)) + 1 : 1,
            name: body.name,
            data: body.data,
            uploadedDate: new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        modules[idx].materials.push(material);
        set('s360_modules', modules);
        return modules[idx];
    }

    if (endpoint.startsWith('/teachers/modules/') && endpoint.includes('/materials/') && method === 'DELETE') {
        const parts = endpoint.split('/');
        const mId = parseInt(parts[3]);
        const matId = parseInt(parts[5]);
        const modules = get('s360_modules');
        const idx = modules.findIndex(m => m.id === mId);
        if (idx === -1) throw new Error("Module not found.");
        modules[idx].materials = (modules[idx].materials || []).filter(x => x.id !== matId);
        set('s360_modules', modules);
        return "Deleted";
    }

    if (endpoint.startsWith('/teachers/modules/') && method === 'DELETE') {
        const mId = parseInt(endpoint.split('/')[3]);
        const modules = get('s360_modules');
        const filtered = modules.filter(m => m.id !== mId);
        set('s360_modules', filtered);
        return "Deleted";
    }

    if (endpoint === '/teachers/timetable' && method === 'POST') {
        const schedules = get('s360_schedules');
        body.id = schedules.length + 1;
        schedules.push(body);
        set('s360_schedules', schedules);
        return body;
    }

    if (endpoint.startsWith('/teachers/timetable/') && endpoint.endsWith('/status') && method === 'POST') {
        const sId = parseInt(endpoint.split('/')[3]);
        const schedules = get('s360_schedules');
        const sched = schedules.find(s => s.id === sId);
        if (sched) {
            sched.status = body.status;
            sched.statusMessage = body.statusMessage;
            set('s360_schedules', schedules);
            return sched;
        }
        throw new Error("Schedule not found");
    }

    if (endpoint.startsWith('/teachers/timetable/') && method === 'PUT') {
        const sId = parseInt(endpoint.split('/')[3]);
        const schedules = get('s360_schedules');
        const idx = schedules.findIndex(s => s.id === sId);
        if (idx !== -1) {
            schedules[idx].dayOfWeek = body.dayOfWeek;
            schedules[idx].startTime = body.startTime;
            schedules[idx].endTime = body.endTime;
            schedules[idx].room = body.room;
            set('s360_schedules', schedules);
            return schedules[idx];
        }
        throw new Error("Schedule not found");
    }

    if (endpoint.startsWith('/teachers/timetable/') && method === 'DELETE') {
        const sId = parseInt(endpoint.split('/')[3]);
        const schedules = get('s360_schedules');
        const filtered = schedules.filter(s => s.id !== sId);
        set('s360_schedules', filtered);
        return "Deleted";
    }

    if (endpoint.startsWith('/teachers/') && endpoint.endsWith('/timetable') && method === 'GET') {
        const tId = parseInt(endpoint.split('/')[2]);
        return get('s360_schedules').filter(s => s.teacherId === tId);
    }

    if (endpoint.startsWith('/teachers/') && endpoint.endsWith('/enrolled-students') && method === 'GET') {
        const tId = parseInt(endpoint.split('/')[2]);
        const mods = get('s360_modules').filter(m => m.teacherId === tId);
        const courseIds = [...new Set(mods.map(m => m.courseId))];
        const allStudents = get('s360_students');
        return allStudents.filter(s => courseIds.includes(s.courseId) && s.enrollmentStatus === 'APPROVED_STAFF');
    }

    if (endpoint === '/teachers/announcements' && method === 'POST') {
        const announcements = get('s360_announcements');
        body.id = announcements.length + 1;
        body.createdDate = new Date().toISOString().replace('T', ' ').substring(0, 19);
        announcements.push(body);
        set('s360_announcements', announcements);
        return body;
    }

    if (endpoint === '/teachers/attendance' && method === 'POST') {
        const att = get('s360_attendance');
        const matchIdx = att.findIndex(a => a.studentId === body.studentId && a.scheduleId === body.scheduleId && a.date === body.date);
        if (matchIdx !== -1) {
            att[matchIdx].status = body.status;
            set('s360_attendance', att);
            return att[matchIdx];
        }
        body.id = att.length + 1;
        att.push(body);
        set('s360_attendance', att);
        return body;
    }

    if (endpoint.startsWith('/teachers/attendance/schedule/') && method === 'GET') {
        const schedId = parseInt(endpoint.split('/')[4]);
        return get('s360_attendance').filter(a => a.scheduleId === schedId);
    }

    // Enrollment Officer Dashboard mocks
    if (endpoint === '/enrollment/applications' && method === 'GET') {
        return get('s360_enrollment_requests');
    }

    // EO: Register a brand-new student directly
    if (endpoint === '/enrollment/register-student' && method === 'POST') {
        const users = get('s360_users');
        if (users.some(u => u.username === body.username)) {
            throw new Error('Username already taken. Please choose another.');
        }
        if (users.some(u => u.email === body.email)) {
            throw new Error('An account with this email already exists.');
        }
        const newUser = {
            id: users.length ? Math.max(...users.map(u => u.id)) + 1 : 1,
            username:  body.username,
            password:  body.password,
            role:      'STUDENT',
            fullName:  body.fullName,
            email:     body.email,
            contact:   body.contact,
            qrCodeToken: 'QR-STUDENT-' + body.username.toUpperCase()
        };
        users.push(newUser);
        set('s360_users', users);

        const students = get('s360_students');
        const newStudent = {
            id: students.length ? Math.max(...students.map(s => s.id)) + 1 : 1,
            userId:          newUser.id,
            admissionNumber: body.admissionNumber || '',
            courseId:        body.courseId || null,
            className:       body.className || '',
            section:         body.section || '',
            enrollmentStatus: 'PENDING_EO',
            batch:  body.batch || '',
            intake: body.intake || '',
            dob:    body.dob || '',
            gender: body.gender || '',
            nic:    body.nic || ''
        };
        students.push(newStudent);
        set('s360_students', students);

        // Auto-create enrollment request
        const courses = get('s360_courses');
        const course = courses.find(c => c.id === body.courseId);
        const enrollments = get('s360_enrollment_requests');
        enrollments.push({
            id:              enrollments.length ? Math.max(...enrollments.map(r => r.id)) + 1 : 1,
            studentId:       newStudent.id,
            courseId:        body.courseId,
            studentName:     body.fullName,
            courseName:      course ? course.name : 'Unknown Course',
            eoStatus:        'PENDING',
            staffStatus:     'PENDING',
            remarks:         'Registered by Enrollment Officer',
            admissionNumber: body.admissionNumber || '',
            className:       body.className || '',
            section:         body.section || '',
            batch:           body.batch || '',
            intake:          body.intake || '',
            docStatus:       body.docStatus || 'PENDING',
            docStates:       body.docStates || {},
            uploadedDocs:    body.uploadedDocs || {}
        });
        set('s360_enrollment_requests', enrollments);

        const logs = get('s360_logs');
        logs.push({ id: logs.length + 1, message: `Enrollment Officer registered new student: ${body.fullName} (${body.username}).`, severity: 'INFO', timestamp: new Date().toISOString().replace('T',' ').substring(0,19) });
        set('s360_logs', logs);

        return newUser;
    }

    // EO: List all students with user info merged
    if (endpoint === '/enrollment/all-students' && method === 'GET') {
        const students = get('s360_students');
        const users    = get('s360_users');
        return students
            .filter(s => s.enrollmentStatus !== 'DELETED')
            .map(s => {
                const u = users.find(u => u.id === s.userId);
                return { ...s, fullName: u ? u.fullName : '—', username: u ? u.username : '—', userId: s.userId };
            });
    }

    // EO: Update student credentials
    if (endpoint.startsWith('/enrollment/students/') && endpoint.endsWith('/credentials') && method === 'POST') {
        const uid   = parseInt(endpoint.split('/')[3]);
        const users = get('s360_users');
        const idx   = users.findIndex(u => u.id === uid);
        if (idx === -1) throw new Error('Student not found.');
        if (body.username && body.username !== users[idx].username) {
            if (users.some(u => u.username === body.username)) throw new Error('Username already taken.');
            users[idx].username = body.username;
        }
        if (body.password) users[idx].password = body.password;
        set('s360_users', users);
        const logs = get('s360_logs');
        logs.push({ id: logs.length + 1, message: `Enrollment Officer updated credentials for user ID ${uid}.`, severity: 'INFO', timestamp: new Date().toISOString().replace('T',' ').substring(0,19) });
        set('s360_logs', logs);
        return users[idx];
    }

    // EO: Save document verification
    if (endpoint.startsWith('/enrollment/applications/') && endpoint.endsWith('/documents') && method === 'POST') {
        const rId = parseInt(endpoint.split('/')[3]);
        const requests = get('s360_enrollment_requests');
        const req = requests.find(r => r.id === rId);
        if (req) {
            req.docStates = body.docStates || req.docStates || {};
            req.docStatus = body.docStatus || req.docStatus || 'PENDING';
            if (body.uploadedDocs) req.uploadedDocs = body.uploadedDocs;
            if (body.remarks) req.remarks = body.remarks;
            set('s360_enrollment_requests', requests);
            return req;
        }
        throw new Error('Application not found.');
    }

    if (endpoint.startsWith('/enrollment/applications/') && endpoint.endsWith('/status') && method === 'POST') {
        const rId = parseInt(endpoint.split('/')[3]);
        const requests = get('s360_enrollment_requests');
        const req = requests.find(r => r.id === rId);
        if (req) {
            req.eoStatus  = body.status;
            req.remarks   = body.remarks || req.remarks;
            req.docStatus = body.docStatus || req.docStatus;
            set('s360_enrollment_requests', requests);
            
            const students = get('s360_students');
            const stud = students.find(s => s.id === req.studentId);
            if (stud) {
                stud.enrollmentStatus = body.status === 'APPROVED' ? 'APPROVED_EO' : 'REJECTED_EO';
                set('s360_students', students);
            }
            return req;
        }
        throw new Error('Application not found.');
    }

    if (endpoint.startsWith('/enrollment/applications/') && endpoint.endsWith('/details') && method === 'POST') {
        const rId = parseInt(endpoint.split('/')[3]);
        const requests = get('s360_enrollment_requests');
        const req = requests.find(r => r.id === rId);
        if (req) {
            req.admissionNumber = body.admissionNumber;
            req.className = body.className;
            req.section   = body.section;
            if (body.batch)  req.batch  = body.batch;
            if (body.intake) req.intake = body.intake;
            set('s360_enrollment_requests', requests);
            
            const students = get('s360_students');
            const stud = students.find(s => s.id === req.studentId);
            if (stud) {
                stud.admissionNumber = body.admissionNumber;
                stud.className = body.className;
                stud.section   = body.section;
                if (body.batch)  stud.batch  = body.batch;
                if (body.intake) stud.intake = body.intake;
                set('s360_students', students);
            }
            return req;
        }
        throw new Error('Application not found.');
    }

    // EO: Delete past student (Archive to s360_deleted_students)
    if (endpoint.startsWith('/enrollment/applications/') && (method === 'DELETE' || endpoint.endsWith('/delete'))) {
        const parts = endpoint.split('/');
        const rId = parseInt(parts[3]);
        const requests = get('s360_enrollment_requests');
        const reqIdx = requests.findIndex(r => r.id === rId);
        if (reqIdx !== -1) {
            const req = requests[reqIdx];
            const students = get('s360_students');
            const users = get('s360_users');
            const stud = students.find(s => s.id === req.studentId);
            const user = stud ? users.find(u => u.id === stud.userId) : null;
            if (stud) {
                stud.enrollmentStatus = 'DELETED';
                set('s360_students', students);
            }

            const deletedStudents = get('s360_deleted_students') || [];
            const deleted = {
                id: deletedStudents.length ? Math.max(...deletedStudents.map(d => d.id)) + 1 : 1,
                originalStudentId: req.studentId,
                originalRequestId: req.id,
                studentName: req.studentName,
                username: user ? user.username : null,
                email: user ? user.email : null,
                courseName: req.courseName,
                admissionNumber: req.admissionNumber,
                className: req.className,
                section: req.section,
                eoStatus: req.eoStatus,
                staffStatus: req.staffStatus,
                deletedBy: 'Enrollment Officer',
                deletedAt: new Date().toISOString(),
                reason: body && body.reason ? body.reason : 'Deleted by Enrollment Officer'
            };
            deletedStudents.unshift(deleted);
            set('s360_deleted_students', deletedStudents);

            requests.splice(reqIdx, 1);
            set('s360_enrollment_requests', requests);

            const logs = get('s360_logs') || [];
            logs.push({
                id: logs.length + 1,
                message: `Enrollment Officer deleted past student record: ${req.studentName} (ID: ${rId})`,
                severity: 'WARN',
                timestamp: new Date().toISOString().replace('T',' ').substring(0,19)
            });
            set('s360_logs', logs);

            return { message: 'Past student deleted successfully.', deleted };
        }
        throw new Error('Application not found.');
    }

    if (endpoint === '/enrollment/deleted-students' && method === 'GET') {
        return get('s360_deleted_students') || [];
    }

    // Staff/Receptionist Dashboard mocks
    if (endpoint === '/staff/approvals' && method === 'GET') {
        return get('s360_enrollment_requests').filter(r => r.eoStatus === 'APPROVED');
    }

    if (endpoint.startsWith('/staff/approvals/') && endpoint.endsWith('/approve') && method === 'POST') {
        const rId = parseInt(endpoint.split('/')[3]);
        const requests = get('s360_enrollment_requests');
        const req = requests.find(r => r.id === rId);
        if (req) {
            req.staffStatus = body.status;
            set('s360_enrollment_requests', requests);
            
            const students = get('s360_students');
            const stud = students.find(s => s.id === req.studentId);
            if (stud) {
                stud.enrollmentStatus = body.status === 'APPROVED' ? 'APPROVED_STAFF' : 'REJECTED_STAFF';
                set('s360_students', students);
            }
            return req;
        }
        throw new Error("Request not found.");
    }

    // Maintenance reports (submitted by Admin/Staff, viewed by Admin & Staff)
    if (endpoint === '/maintenance-reports' && method === 'GET') {
        return get('s360_maintenance_reports');
    }

    if (endpoint === '/maintenance-reports' && method === 'POST') {
        const reports = get('s360_maintenance_reports');
        const report = {
            id: reports.length ? Math.max(...reports.map(r => r.id)) + 1 : 1,
            reportedById: body.reportedById,
            reportedByName: body.reportedByName,
            area: body.area,
            issue: body.issue,
            priority: body.priority || "MEDIUM",
            status: "PENDING",
            createdDate: new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        reports.push(report);
        set('s360_maintenance_reports', reports);
        return report;
    }

    if (endpoint.startsWith('/maintenance-reports/') && method === 'PUT') {
        const rId = parseInt(endpoint.split('/')[2]);
        const reports = get('s360_maintenance_reports');
        const idx = reports.findIndex(r => r.id === rId);
        if (idx !== -1) {
            reports[idx] = { ...reports[idx], ...body };
            set('s360_maintenance_reports', reports);
            return reports[idx];
        }
        throw new Error("Maintenance report not found.");
    }

    if (endpoint.startsWith('/maintenance-reports/') && method === 'DELETE') {
        const rId = parseInt(endpoint.split('/')[2]);
        const reports = get('s360_maintenance_reports');
        const filtered = reports.filter(r => r.id !== rId);
        set('s360_maintenance_reports', filtered);
        return "Deleted";
    }

    // Support team mocks
    if (endpoint === '/support/tickets' && method === 'GET') {
        return get('s360_tickets');
    }

    if (endpoint.startsWith('/support/tickets/') && endpoint.endsWith('/status') && method === 'POST') {
        const tId = parseInt(endpoint.split('/')[3]);
        const tickets = get('s360_tickets');
        const tick = tickets.find(t => t.id === tId);
        if (tick) {
            tick.status = body.status;
            set('s360_tickets', tickets);
            return tick;
        }
        throw new Error("Ticket not found.");
    }

    if (endpoint.startsWith('/support/tickets/') && endpoint.endsWith('/priority') && method === 'POST') {
        const tId = parseInt(endpoint.split('/')[3]);
        const tickets = get('s360_tickets');
        const tick = tickets.find(t => t.id === tId);
        if (tick) {
            tick.priority = body.priority;
            set('s360_tickets', tickets);
            return tick;
        }
        throw new Error("Ticket not found.");
    }

    if (endpoint.startsWith('/support/tickets/') && endpoint.endsWith('/escalate') && method === 'POST') {
        const tId = parseInt(endpoint.split('/')[3]);
        const tickets = get('s360_tickets');
        const tick = tickets.find(t => t.id === tId);
        if (tick) {
            tick.escalationStatus = "ESCALATED";
            set('s360_tickets', tickets);
            return tick;
        }
    }
    if (endpoint.startsWith('/support/tickets/') && (method === 'DELETE' || endpoint.endsWith('/delete'))) {
        const parts = endpoint.split('/');
        const tId = parseInt(parts[3]);
        const tickets = get('s360_tickets');
        const filteredTickets = tickets.filter(t => t.id !== tId);
        set('s360_tickets', filteredTickets);

        const replies = get('s360_ticket_replies') || [];
        const filteredReplies = replies.filter(r => r.ticketId !== tId);
        set('s360_ticket_replies', filteredReplies);

        return "Deleted";
    }

    if (endpoint.startsWith('/support/tickets/') && endpoint.endsWith('/reply') && method === 'POST') {
        const tId = parseInt(endpoint.split('/')[3]);
        const replies = get('s360_ticket_replies');
        const reply = {
            id: replies.length + 1,
            ticketId: tId,
            senderName: body.senderName,
            senderRole: "SUPPORT",
            message: body.message,
            attachmentName: null,
            attachmentData: null,
            createdDate: new Date().toISOString().replace('T', ' ').substring(0, 19)
        };
        replies.push(reply);
        set('s360_ticket_replies', replies);
        
        // Mark pending
        const tickets = get('s360_tickets');
        const tick = tickets.find(t => t.id === tId);
        if (tick && tick.status === 'OPEN') {
            tick.status = 'PENDING';
            set('s360_tickets', tickets);
        }
        return reply;
    }

    // Admin dashboard mocks
    if (endpoint === '/admin/users' && method === 'GET') {
        return get('s360_users');
    }

    if (endpoint === '/admin/users' && method === 'POST') {
        const users = get('s360_users');
        if (users.some(u => u.username === body.username)) {
            throw new Error("Username already exists.");
        }
        body.id = users.length + 1;
        body.qrCodeToken = "QR-" + body.role + "-" + body.username.toUpperCase();
        users.push(body);
        set('s360_users', users);

        if (body.role === 'STUDENT') {
            const students = get('s360_students');
            students.push({
                id: students.length + 1,
                userId: body.id,
                admissionNumber: "",
                courseId: null,
                className: "",
                section: "",
                enrollmentStatus: null
            });
            set('s360_students', students);
        }
        return body;
    }

    if (endpoint.startsWith('/admin/users/') && method === 'PUT') {
        const uId = parseInt(endpoint.split('/')[3]);
        const users = get('s360_users');
        const idx = users.findIndex(u => u.id === uId);
        if (idx !== -1) {
            const previousRole = users[idx].role;
            users[idx] = { ...users[idx], ...body };
            set('s360_users', users);

            // If this account was just assigned the STUDENT role and has no
            // student record yet (either brand new, or reassigned from another
            // role), create one so their dashboard works correctly.
            if (users[idx].role === 'STUDENT' && previousRole !== 'STUDENT') {
                const students = get('s360_students');
                if (!students.some(s => s.userId === uId)) {
                    students.push({
                        id: students.length ? Math.max(...students.map(s => s.id)) + 1 : 1,
                        userId: uId,
                        admissionNumber: "",
                        courseId: null,
                        className: "",
                        section: "",
                        enrollmentStatus: null
                    });
                    set('s360_students', students);
                }
            }

            const logs = get('s360_logs');
            logs.push({ id: logs.length + 1, message: `Admin updated account "${users[idx].fullName}"${previousRole !== users[idx].role ? ` — role changed from ${previousRole} to ${users[idx].role}` : ''}.`, severity: "INFO", timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19) });
            set('s360_logs', logs);

            return users[idx];
        }
        throw new Error("User not found.");
    }

    if (endpoint.startsWith('/admin/users/') && method === 'DELETE') {
        const uId = parseInt(endpoint.split('/')[3]);
        const users = get('s360_users');
        const filtered = users.filter(u => u.id !== uId);
        set('s360_users', filtered);

        const students = get('s360_students');
        const filteredStuds = students.filter(s => s.userId !== uId);
        set('s360_students', filteredStuds);
        return "Deleted";
    }

    if (endpoint === '/admin/stats' && method === 'GET') {
        const students = get('s360_students');
        const attendances = get('s360_attendance');
        const tickets = get('s360_tickets');
        const users = get('s360_users');

        let attendancePercent = 100.0;
        if (attendances.length > 0) {
            const present = attendances.filter(a => a.status === 'PRESENT').length;
            attendancePercent = (present / attendances.length) * 100.0;
        }

        const tBreak = {
            OPEN: tickets.filter(t => t.status === 'OPEN').length,
            PENDING: tickets.filter(t => t.status === 'PENDING').length,
            IN_PROGRESS: tickets.filter(t => t.status === 'IN_PROGRESS').length,
            RESOLVED: tickets.filter(t => t.status === 'RESOLVED').length,
            CLOSED: tickets.filter(t => t.status === 'CLOSED').length
        };

        const roleCounts = {
            STUDENT: users.filter(u => u.role === 'STUDENT').length,
            TEACHER: users.filter(u => u.role === 'TEACHER').length,
            STAFF: users.filter(u => u.role === 'STAFF').length,
            SUPPORT: users.filter(u => u.role === 'SUPPORT').length,
            ENROLLMENT: users.filter(u => u.role === 'ENROLLMENT').length
        };

        return {
            studentCount: students.length,
            attendancePercentage: Math.round(attendancePercent * 10) / 10,
            ticketStatusBreakdown: tBreak,
            roleCounts: roleCounts
        };
    }

    if (endpoint === '/admin/logs' && method === 'GET') {
        return get('s360_logs');
    }

    if (endpoint === '/admin/courses' && method === 'POST') {
        const courses = get('s360_courses');
        body.id = courses.length + 1;
        courses.push(body);
        set('s360_courses', courses);
        return body;
    }

    if (endpoint.startsWith('/admin/courses/') && method === 'PUT') {
        const cId = parseInt(endpoint.split('/')[3]);
        const courses = get('s360_courses');
        const idx = courses.findIndex(c => c.id === cId);
        if (idx !== -1) {
            courses[idx] = { ...courses[idx], ...body };
            set('s360_courses', courses);
            return courses[idx];
        }
        throw new Error("Course not found.");
    }

    if (endpoint.startsWith('/admin/courses/') && method === 'DELETE') {
        const cId = parseInt(endpoint.split('/')[3]);
        const courses = get('s360_courses');
        const filtered = courses.filter(c => c.id !== cId);
        set('s360_courses', filtered);
        return "Deleted";
    }

    if (endpoint === '/contact' && method === 'POST') {
        const name = body.name || 'Anonymous';
        const email = body.email || 'Not provided';
        const category = body.category || 'General';
        const subject = body.subject || 'Inquiry';
        const message = body.message || '';

        if (!message.trim()) {
            throw new Error('Message body cannot be empty.');
        }

        const logs = get('s360_logs') || [];
        const refId = "S360-CNT-" + Math.floor(10000 + Math.random() * 90000);
        logs.push({
            id: logs.length + 1,
            message: `Contact Submission [Ref: ${refId}] from ${name} (${email}) - ${category} | ${subject}: ${message}`,
            severity: "INFO",
            timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19)
        });
        set('s360_logs', logs);

        return {
            status: "success",
            message: `Thank you, ${name}! Your inquiry regarding "${subject}" has been received. Our support team will get back to you shortly.`,
            referenceId: refId
        };
    }

    // Database Endpoints simulation in Demo Mode
    if ((endpoint === '/db/status' || endpoint === '/admin/db/status') && method === 'GET') {
        const users = get('s360_users');
        const students = get('s360_students');
        const courses = get('s360_courses');
        const tickets = get('s360_tickets');
        const logs = get('s360_logs');
        return {
            status: "CONNECTED",
            pingMs: 3,
            databaseProduct: "H2 (MySQL Compatibility Mode)",
            databaseVersion: "2.2.224",
            driverName: "H2 JDBC Driver",
            url: "jdbc:h2:file:./data/school360_db",
            userCount: users.length,
            studentCount: students.length,
            courseCount: courses.length,
            ticketCount: tickets.length,
            logCount: logs.length,
            timestamp: new Date().toISOString()
        };
    }

    if ((endpoint === '/db/reconnect' || endpoint === '/admin/db/reconnect') && method === 'POST') {
        if (typeof initDemoMode === 'function') initDemoMode();
        const users = get('s360_users');
        return {
            success: true,
            status: "CONNECTED",
            message: "Database connection re-established and verified successfully.",
            pingMs: 2,
            databaseProduct: "H2 (MySQL Compatibility Mode)",
            userCount: users.length,
            timestamp: new Date().toISOString()
        };
    }

    if ((endpoint === '/db/reset' || endpoint === '/admin/db/reset') && method === 'POST') {
        localStorage.removeItem('s360_users');
        if (typeof initDemoMode === 'function') initDemoMode();
        const users = get('s360_users');
        return {
            success: true,
            status: "CONNECTED",
            message: "Database seeded and synchronized successfully.",
            userCount: users.length,
            timestamp: new Date().toISOString()
        };
    }

    throw new Error(`Endpoint ${endpoint} not mocked for Demo Mode`);
}

// --- Database Connectivity & Management Helpers ---
async function checkDatabaseStatus() {
    try {
        const data = await apiCall('/db/status');
        updateDBUIState(data);
        return data;
    } catch (e) {
        const failData = { status: 'DISCONNECTED', message: e.message || 'Database connection error' };
        updateDBUIState(failData);
        return failData;
    }
}

async function reconnectDatabase(showToastFeedback = true) {
    try {
        if (showToastFeedback && typeof showToast === 'function') {
            showToast('Connecting...', 'Attempting to verify and reconnect to School360 Database...', 'info');
        }
        const res = await apiCall('/db/reconnect', 'POST');
        await checkDatabaseStatus();
        if (showToastFeedback && typeof showToast === 'function') {
            showToast('Database Connected! 🟢', res.message || 'Database connection re-established and verified.', 'success');
        }
        return res;
    } catch (e) {
        if (showToastFeedback && typeof showToast === 'function') {
            showToast('Connection Error 🔴', e.message || 'Failed to reconnect to database.', 'danger');
        }
        await checkDatabaseStatus();
        throw e;
    }
}

async function resetDatabase(showToastFeedback = true) {
    if (!confirm('Are you sure you want to re-seed and reset the database default tables?')) return;
    try {
        const res = await apiCall('/db/reset', 'POST');
        await checkDatabaseStatus();
        if (showToastFeedback && typeof showToast === 'function') {
            showToast('Database Reseeded!', res.message || 'Database default tables re-seeded cleanly.', 'success');
        }
        setTimeout(() => window.location.reload(), 1200);
        return res;
    } catch (e) {
        if (showToastFeedback && typeof showToast === 'function') {
            showToast('Reset Failed', e.message || 'Failed to re-seed database.', 'danger');
        }
        throw e;
    }
}

function updateDBUIState(data) {
    const isConnected = data && (data.status === 'CONNECTED' || data.status === 'DEGRADED' || data.success === true);
    
    // Update badge elements
    document.querySelectorAll('.db-status-badge').forEach(el => {
        el.className = `db-status-badge ${isConnected ? 'connected' : 'disconnected'}`;
        el.innerHTML = isConnected ? '🟢 Connected' : '🔴 Disconnected';
    });

    document.querySelectorAll('.db-status-text').forEach(el => {
        el.textContent = isConnected ? 'Database Connected & Live' : 'Database Offline / Connection Issue';
        el.style.color = isConnected ? '#6b3294' : '#6b3294';
    });

    if (data) {
        const pingEl = document.getElementById('db-ping-val');
        if (pingEl) pingEl.textContent = (data.pingMs || 3) + ' ms';

        const productEl = document.getElementById('db-product-val');
        if (productEl) productEl.textContent = data.databaseProduct || 'H2 Persistent File DB';

        const urlEl = document.getElementById('db-url-val');
        if (urlEl) urlEl.textContent = data.url || 'jdbc:h2:file:./data/school360_db';

        const countEl = document.getElementById('db-records-val');
        if (countEl) countEl.textContent = (data.userCount || 7) + ' users / ' + (data.studentCount || 1) + ' students';
    }
}

window.checkDatabaseStatus = checkDatabaseStatus;
window.reconnectDatabase = reconnectDatabase;
window.resetDatabase = resetDatabase;

