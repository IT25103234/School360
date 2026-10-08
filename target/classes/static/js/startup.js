(() => {
    'use strict';
    const DURATION = 10000;
    const KEY = 's360_intro_seen_v1';
    // Public home is also reachable by authenticated users. Never alter their routing.
    try {
        if (JSON.parse(localStorage.getItem('s360_current_user') || 'null')) return;
        if (sessionStorage.getItem(KEY)) return;
        sessionStorage.setItem(KEY, '1');
    } catch (_) { /* Storage restrictions must not prevent entry. */ }
    const overlay = document.createElement('section');
    overlay.id = 's360-startup';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');
    overlay.setAttribute('aria-label', 'Welcome to School360. Your school, connected.');
    const icons = [
        ['Attendance','M-12 0l8 8 17-19'],
        ['Examinations','M-10-15h20v30h-20z M-5-5h10 M-5 2h10 M-5 9h6'],
        ['Teachers','M-15-13h30v20h-30z M0 7v8 M-8 15h16 M-8-4h16'],
        ['Timetable','M-14-12h28v26h-28z M-14-4h28 M-7-16v8 M7-16v8 M-7 3h4 M3 3h4 M-7 9h4'],
        ['Library','M0-10Q-8-17-18-12v25Q-8 8 0 15Q8 8 18 13v-25Q8-17 0-10v25'],
        ['Students','M-6-8a6 6 0 1 0 12 0a6 6 0 1 0-12 0 M-14 15v-5q14-17 28 0v5']
    ];
    const windows = Array.from({length:18}, (_,i) => `<rect x="${302+(i%6)*52}" y="${288+Math.floor(i/6)*43}" width="29" height="24" rx="2"/>`).join('');
    overlay.innerHTML = `<svg class="intro-scene" viewBox="90 90 720 560" preserveAspectRatio="xMidYMid meet" aria-hidden="true">
      <defs><linearGradient id="intro-wall" x2="0" y2="1"><stop stop-color="#43215F"/><stop offset="1" stop-color="#241038"/></linearGradient><radialGradient id="intro-halo"><stop stop-color="#43215F" stop-opacity=".07"/><stop offset="1" stop-color="#241038" stop-opacity="0"/></radialGradient></defs>
      <ellipse cx="450" cy="350" rx="350" ry="260" fill="url(#intro-halo)"/>
      <g data-part="particles" fill="#43215F">${Array.from({length:20},(_,i)=>`<circle cx="${90+(i*139)%720}" cy="${80+(i*83)%470}" r="${i%3===0?2:1}"/>`).join('')}</g>
      <g data-part="campus">
        <ellipse cx="450" cy="477" rx="260" ry="18" fill="#241038" opacity=".12"/>
        <path d="M450 423L350 590H550Z" fill="#DAD9DF" opacity=".5"/>
        <g data-part="building"><path d="M252 440V288L450 237L648 288V440Z" fill="url(#intro-wall)"/>
          <path d="M240 289L450 230L660 289" fill="none" stroke="#43215F" stroke-width="7"/>
          <g fill="#DAD9DF">${windows}</g>
          <path d="M411 440V330Q450 305 489 330V440" fill="#241038"/>
          <path data-part="door-left" d="M414 440V333Q432 321 448 324V440Z" fill="#43215F" stroke="#DAD9DF"/>
          <path data-part="door-right" d="M452 440V324Q469 321 486 333V440Z" fill="#43215F" stroke="#DAD9DF"/>
          <path d="M245 441H655 M262 450H638" stroke="#43215F" stroke-width="5"/>
          <rect x="355" y="258" width="190" height="29" rx="4" fill="#FFFFFF"/><text x="450" y="278" fill="#241038" text-anchor="middle" font-size="16" font-weight="800" letter-spacing="2">SCHOOL360</text>
        </g>
        <g data-part="student"><ellipse cx="0" cy="0" rx="18" ry="4" fill="#241038" opacity=".4"/>
          <g data-part="body"><rect x="-20" y="-65" width="19" height="31" rx="7" fill="#DAD9DF"/><path d="M-8-67Q4-74 14-62L18-29H-11Z" fill="#43215F" stroke="#DAD9DF" stroke-width="1.5"/>
          <circle cx="4" cy="-83" r="12" fill="#DAD9DF"/><path d="M-8-83q-4-21 17-15l7 13-14-5Z" fill="#241038"/>
          <path d="M9-61L19-41 30-47" fill="none" stroke="#DAD9DF" stroke-width="6" stroke-linecap="round"/><path d="M22-53l13 3-4 17-13-3Z" fill="#FFFFFF" stroke="#43215F" stroke-width="2"/>
          <path data-part="leg-left" d="M-4-31L-6-14-13-3" fill="none" stroke="#241038" stroke-width="8" stroke-linecap="round"/>
          <path data-part="leg-right" d="M10-31L12-15 18-3" fill="none" stroke="#241038" stroke-width="8" stroke-linecap="round"/></g>
        </g>
      </g>
      <g data-part="network" fill="none" stroke="#43215F" stroke-width="1" opacity="0">${icons.map((_,i)=>`<path data-line="${i}"/>`).join('')}</g>
      <g data-part="orbit">${icons.map(([label,path],i)=>`<g data-icon="${i}"><circle r="32" fill="#43215F" stroke="#DAD9DF" stroke-opacity=".5"/><path d="${path}" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><rect x="-57" y="37" width="114" height="25" rx="12" fill="#241038"/><text y="54" text-anchor="middle" fill="#FFFFFF" font-size="12">${label}</text></g>`).join('')}</g>
      <g data-part="mark" transform="translate(450 260)" opacity="0" color="#241038"><circle r="64" fill="none" stroke="#43215F" stroke-width="2" stroke-dasharray="300 102" transform="rotate(-65)"/><path d="M-34 0L0-20 34 0 0 20Z M-22 8v20q22 16 44 0V8 M34 0v29" fill="none" stroke="#241038" stroke-width="3" stroke-linejoin="round"/><circle cx="56" cy="-31" r="5" fill="#241038"/></g>
    </svg><div class="intro-brand"><h1>SCHOOL360</h1><p>Smart School Management System</p></div><div class="intro-caption">A JOURNEY INTO SMART EDUCATION</div><div class="intro-sweep"></div><button type="button" class="intro-skip">Skip intro</button><div class="intro-progress"></div>`;
    document.body.appendChild(overlay);
    const siblings = [...document.body.children].filter(el => el !== overlay && !['SCRIPT','STYLE'].includes(el.tagName));
    const inertState = siblings.map(el => el.inert);
    siblings.forEach(el => { el.inert = true; });
    const previousFocus = document.activeElement;
    const skip = overlay.querySelector('.intro-skip');
    skip.focus({preventScroll:true});
    const part = name => overlay.querySelector(`[data-part="${name}"]`);
    const clamp = n => Math.max(0,Math.min(1,n));
    const ease = n => { n=clamp(n); return n*n*(3-2*n); };
    let frame, timer, finished = false;
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
    const finish = () => {
        if (finished) return;
        finished = true;
        cancelAnimationFrame(frame);
        clearTimeout(timer);
        document.removeEventListener('visibilitychange', resume);
        siblings.forEach((el,i) => { el.inert = inertState[i]; });
        overlay.remove();
        if (typeof window.openLoginModal === 'function') {
            window.openLoginModal();
            document.querySelector('#login-modal input')?.focus({preventScroll:true});
        } else if (previousFocus?.isConnected) previousFocus.focus({preventScroll:true});
    };
    skip.addEventListener('click', finish);
    overlay.addEventListener('keydown', event => {
        if (event.key === 'Escape') finish();
        if (event.key === 'Tab') { event.preventDefault(); skip.focus(); }
    });
    const start = performance.now();
    function render(t) {
        const s = t / 1000;
        overlay.style.opacity = 1-ease((s-9.2)/.8);
        overlay.querySelector('.intro-progress').style.transform = `scaleX(${clamp(t/DURATION)})`;
        if (reduced.matches) {
            overlay.querySelector('.intro-brand').style.opacity = '1';
            return;
        }
        part('particles').style.opacity = .55*ease(s/2);
        part('particles').setAttribute('transform',`translate(0 ${-s*3})`);
        part('campus').setAttribute('transform',`translate(450 360) scale(${.9+.1*ease(s/2)}) translate(-450 -360)`);
        part('campus').style.opacity = 1-ease((s-6)/1.2);
        const build = ease(s/1.8);
        part('building').setAttribute('transform',`translate(0 ${440*(1-build)}) scale(1 ${Math.max(.001,build)})`);
        const walk = ease((s-2)/2.6);
        part('student').setAttribute('transform',`translate(${170+280*walk} ${485-50*ease((s-4)/.6)}) scale(${1-.55*ease((s-4)/.6)})`);
        part('student').style.opacity = ease((s-1.9)/.3)*(1-ease((s-4.4)/.4));
        part('body').setAttribute('transform',`translate(0 ${Math.sin(s*17)*1.6})`);
        ['left','right'].forEach((side,i) => part(`leg-${side}`).setAttribute('transform',`rotate(${Math.sin(s*17+i*Math.PI)*23} 4 -31)`));
        const door = 1-.93*ease((s-3.1)/.7);
        part('door-left').setAttribute('transform',`translate(414 0) scale(${door} 1) translate(-414 0)`);
        part('door-right').setAttribute('transform',`translate(486 0) scale(${door} 1) translate(-486 0)`);
        const rotation = ease((s-6)/1.7)*Math.PI*2;
        const morph = ease((s-7)/.8);
        const radius = 203*(1-morph)+64*morph;
        icons.forEach((_,i) => {
            const angle = i*Math.PI/3-Math.PI/2+rotation;
            const x = 450+Math.cos(angle)*radius, y = (340-80*ease((s-6)/1.5))+Math.sin(angle)*radius*.78;
            const node = overlay.querySelector(`[data-icon="${i}"]`);
            node.setAttribute('transform',`translate(${x} ${y}) scale(${1-.8*morph})`);
            node.style.opacity = ease((s-4-i*.12)/.65)*(1-morph);
            overlay.querySelector(`[data-line="${i}"]`).setAttribute('d',`M450 365Q${x} 365 ${x} ${y}`);
        });
        part('network').style.opacity = .45*ease((s-4)/1.2)*(1-ease((s-6)/.7));
        part('mark').style.opacity = ease((s-7.1)/.9);
        overlay.querySelector('.intro-brand').style.opacity = ease((s-7)/.8);
        overlay.querySelector('.intro-caption').style.opacity = 1-ease((s-6)/1);
        const sweep = clamp((s-8.2)/.9);
        const light = overlay.querySelector('.intro-sweep');
        light.style.transform = `translateX(${-100+200*sweep}%)`;
        light.style.opacity = Math.sin(sweep*Math.PI)*.12;
    }
    function tick(now) {
        const elapsed = now-start;
        if (elapsed >= DURATION) { finish(); return; }
        render(elapsed);
        frame = requestAnimationFrame(tick);
    }
    function resume() { if (performance.now()-start >= DURATION) finish(); }
    document.addEventListener('visibilitychange', resume);
    // Same deadline as the visual clock; timer also cleans up if frames are throttled.
    timer = setTimeout(finish, DURATION);
    render(0);
    frame = requestAnimationFrame(tick);
})();
