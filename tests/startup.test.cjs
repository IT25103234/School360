const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const source = fs.readFileSync(require('node:path').join(__dirname,'../src/main/resources/static/js/startup.js'),'utf8');
function setup({user=null, seen=false, reduced=false}={}) {
    let now=0, frame, timer, opened=0, removed=false;
    const nodes = new Map();
    const node = key => {
        if (!nodes.has(key)) nodes.set(key,{style:{},setAttribute(){},focus(){},addEventListener(){}});
        return nodes.get(key);
    };
    const content={tagName:'MAIN',inert:false};
    const overlay={style:{},setAttribute(){},querySelector:node,addEventListener(){},remove(){removed=true;}};
    const context={localStorage:{getItem:()=>JSON.stringify(user)},sessionStorage:{getItem:()=>seen,setItem(){}},
        document:{body:{children:[content],appendChild(){}},createElement:()=>overlay,activeElement:null,
            addEventListener(){},removeEventListener(){},querySelector:node},
        window:{matchMedia:()=>({matches:reduced}),openLoginModal(){opened++;}},
        performance:{now:()=>now},requestAnimationFrame:fn=>{frame=fn;return 1;},cancelAnimationFrame(){},
        setTimeout:(fn,delay)=>{timer={fn,delay};return 2;},clearTimeout(){}};
    vm.runInNewContext(source,context);
    return {advance(t){now=t;frame?.(now);},get timer(){return timer;},get opened(){return opened;},get removed(){return removed;},content};
}
test('startup reveals existing login and restores page at 10000ms',()=>{
    const app=setup();
    assert.equal(app.timer.delay,10000);
    assert.equal(app.content.inert,true);
    for(const t of [1000,3000,5000,7000,9000,9999]) app.advance(t);
    assert.equal(app.opened,0);
    app.advance(10000);
    assert.equal(app.opened,1);
    assert.equal(app.removed,true);
    assert.equal(app.content.inert,false);
    app.timer.fn();
    assert.equal(app.opened,1);
});
test('signed-in and returning tab entries skip startup',()=>{
    assert.equal(setup({user:{id:1,role:'STUDENT'}}).timer,undefined);
    assert.equal(setup({seen:true}).timer,undefined);
});
test('reduced motion and throttled frames still complete on deadline',()=>{
    const app=setup({reduced:true});
    app.advance(5000);
    app.timer.fn();
    assert.equal(app.removed,true);
    assert.equal(app.opened,1);
});
