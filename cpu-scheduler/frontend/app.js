const API = "http://localhost:8080/api/scheduler/simulate";
const rows = document.querySelector("#rows"), palette = ["#5261d5","#0f9c91","#e39336","#8055bd","#d45d75","#337bb5","#738d3b"];
let id = 1;
function add(p={}) { const tr=document.createElement("tr"), pid=p.processId??id++;
  for(const [key,value] of [["processId",pid],["arrivalTime",p.arrivalTime??0],["burstTime",p.burstTime??5],["priority",p.priority??1]]) { const td=document.createElement("td"), input=document.createElement("input"); input.type="number";input.dataset.key=key;input.value=value;if(key!=="priority")input.min=key==="burstTime"?1:0;td.append(input);tr.append(td); }
  const td=document.createElement("td"),btn=document.createElement("button");btn.textContent="×";btn.onclick=()=>tr.remove();td.append(btn);tr.append(td);rows.append(tr);
}
function read(){return [...rows.rows].map(r=>Object.fromEntries([...r.querySelectorAll("input")].map(i=>[i.dataset.key,Number(i.value)])));}
document.querySelector("#add").onclick=()=>add();
document.querySelector("#algorithm").onchange=e=>document.querySelector("#qwrap").classList.toggle("hidden",e.target.value!=="ROUND_ROBIN");
document.querySelector("#run").onclick=async()=>{const error=document.querySelector("#error");error.textContent="";const processes=read(),algorithm=document.querySelector("#algorithm").value;
 if(!processes.length){error.textContent="Add at least one process.";return;}if(processes.some(p=>!Object.values(p).every(Number.isInteger)||p.arrivalTime<0||p.burstTime<1)){error.textContent="Use whole numbers; arrival must be 0 or greater and burst must be at least 1.";return;}
 try{const res=await fetch(API,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({algorithm,quantum:Number(document.querySelector("#quantum").value),processes})});const data=await res.json();if(!res.ok)throw Error(data.message||"Request failed");render(data);}catch(e){error.textContent=e instanceof TypeError?"Backend unavailable. Start Spring Boot and retry.":e.message;}};
function render(d){document.querySelector("#results").classList.remove("hidden");document.querySelector("#metrics").innerHTML=`<div class="metric"><span>Average waiting</span><strong>${d.averageWaitingTime.toFixed(2)}</strong></div><div class="metric"><span>Average turnaround</span><strong>${d.averageTurnaroundTime.toFixed(2)}</strong></div><div class="metric"><span>Total elapsed time</span><strong>${d.totalTime}</strong></div>`;
 const timeline=document.querySelector("#timeline");timeline.replaceChildren();for(const s of d.timeline){const b=document.createElement("div");b.className="slice";b.style.setProperty("--d",s.endTime-s.startTime);b.style.setProperty("--c",palette[(Math.abs(s.processId)-1)%palette.length]);b.innerHTML=`P${s.processId}<small>${s.startTime}–${s.endTime}</small>`;timeline.append(b);}
 document.querySelector("#labels").innerHTML=`<span>${d.timeline[0]?.startTime??0}</span><span>${d.totalTime}</span>`;document.querySelector("#stats").innerHTML=d.processes.map(p=>`<tr><td>P${p.processId}</td><td>${p.arrivalTime}</td><td>${p.burstTime}</td><td>${p.priority}</td><td>${p.completionTime}</td><td>${p.turnaroundTime}</td><td>${p.waitingTime}</td></tr>`).join("");}
add({processId:1,arrivalTime:0,burstTime:5,priority:2});add({processId:2,arrivalTime:1,burstTime:3,priority:1});add({processId:3,arrivalTime:2,burstTime:4,priority:3});id=4;
