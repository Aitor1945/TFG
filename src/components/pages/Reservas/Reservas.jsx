import React, { useState } from "react";
import "./reservas.css";

const ZONAS = [
  { id: "salon",    nombre: "Salón Comunal",  icono: "fa-solid fa-people-roof",              capacidad: 30, color: "#3b82f6", horario: [9,  23] },
  { id: "piscina",  nombre: "Piscina",        icono: "fa-solid fa-water-ladder",             capacidad: 20, color: "#06b6d4", horario: [10, 21] },
  { id: "bbq",      nombre: "Zona Barbacoa",  icono: "fa-solid fa-fire-burner",              capacidad: 15, color: "#f59e0b", horario: [12, 22] },
  { id: "gimnasio", nombre: "Gimnasio",       icono: "fa-solid fa-dumbbell",                 capacidad: 8,  color: "#10b981", horario: [7,  22] },
  { id: "estudio",  nombre: "Sala de Estudio",icono: "fa-solid fa-book-open",                capacidad: 10, color: "#a78bfa", horario: [8,  22] },
  { id: "padel",    nombre: "Pista de Pádel", icono: "fa-solid fa-table-tennis-paddle-ball", capacidad: 4,  color: "#ef4444", horario: [8,  23] },
];

const MESES = ["Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];
const DIAS  = ["Lun","Mar","Mié","Jue","Vie","Sáb","Dom"];

const hoy    = new Date();
const toStr  = d => d.toISOString().split("T")[0];
const padStr = (y, m, d) => `${y}-${String(m+1).padStart(2,"0")}-${String(d).padStart(2,"0")}`;

const MOCK = [
  { id: 1, zonaId: "salon",   fecha: padStr(hoy.getFullYear(), hoy.getMonth(), hoy.getDate()+1), hi: 17, hf: 20, autor: "Vecino 2ºA" },
  { id: 2, zonaId: "piscina", fecha: toStr(hoy),                                                 hi: 11, hf: 13, autor: "Vecino 3ºC" },
  { id: 3, zonaId: "bbq",     fecha: padStr(hoy.getFullYear(), hoy.getMonth(), hoy.getDate()+2), hi: 13, hf: 17, autor: "Vecino 1ºB" },
];

function celdas(year, month) {
  const offset = (new Date(year, month, 1).getDay() + 6) % 7;
  const total  = new Date(year, month + 1, 0).getDate();
  return [...Array(offset).fill(null), ...Array.from({ length: total }, (_, i) => i + 1)];
}

export default function Reservas() {
  const [year,  setYear]    = useState(hoy.getFullYear());
  const [month, setMonth]   = useState(hoy.getMonth());
  const [fecha, setFecha]   = useState(toStr(hoy));
  const [zona,  setZona]    = useState(null);
  const [reservas, setReservas] = useState(MOCK);
  const [modal, setModal]   = useState(false);
  const [form,  setForm]    = useState({ autor: "", hi: "", hf: "" });
  const [error, setError]   = useState("");
  const [exito, setExito]   = useState(false);
  const [tab,   setTab]     = useState("zonas");

  const diaR  = reservas.filter(r => r.fecha === fecha);
  const zonaR = zona ? diaR.filter(r => r.zonaId === zona.id) : [];

  function prevMonth() { month === 0  ? (setMonth(11), setYear(y => y-1)) : setMonth(m => m-1); }
  function nextMonth() { month === 11 ? (setMonth(0),  setYear(y => y+1)) : setMonth(m => m+1); }

  function confirmar() {
    const hi = parseInt(form.hi), hf = parseInt(form.hf);
    if (!form.autor.trim())        return setError("Indica tu nombre o piso.");
    if (isNaN(hi) || isNaN(hf))    return setError("Selecciona hora de inicio y fin.");
    if (hf <= hi)                  return setError("La hora de fin debe ser posterior.");
    if (hi < zona.horario[0] || hf > zona.horario[1]) return setError(`Horario: ${zona.horario[0]}:00–${zona.horario[1]}:00`);
    if (zonaR.some(r => hi < r.hf && hf > r.hi))      return setError("Tramo ocupado. Elige otro horario.");
    setReservas(p => [...p, { id: Date.now(), zonaId: zona.id, fecha, hi, hf, autor: form.autor.trim() }]);
    setExito(true);
    setTimeout(() => { setExito(false); setModal(false); setForm({ autor:"", hi:"", hf:"" }); setError(""); }, 1800);
  }

  function horaOpts(min, max) {
    return Array.from({ length: max - min + 1 }, (_, i) => (
      <option key={i} value={min+i}>{String(min+i).padStart(2,"0")}:00</option>
    ));
  }

  return (
    <div className="reservas-page">

      <h2 className="res-titulo fw-bold mb-1">Reservas</h2>
      <p className="res-sub mb-4">Gestiona el uso de las zonas comunes de tu comunidad</p>

      <div className="tabs-movil">
        {["zonas","calendario","detalle"].map(t => (
          <button key={t} className={`tab-btn${tab===t?" activo":""}`} onClick={() => setTab(t)}>
            {t.charAt(0).toUpperCase()+t.slice(1)}
          </button>
        ))}
      </div>

      <div className="res-grid">

        {/* ── Col 1: Zonas ── */}
        <div className={tab!=="zonas"?"oculto-movil":undefined}>
          <p className="col-label">Zonas disponibles</p>
          {ZONAS.map(z => {
            const total = z.horario[1] - z.horario[0];
            const ocup  = diaR.filter(r => r.zonaId === z.id);
            return (
              <div
                key={z.id}
                className={`zona-card${zona?.id===z.id?" activa":""}`}
                style={zona?.id===z.id ? { borderColor: z.color, boxShadow: `0 0 0 2px ${z.color}33` } : {}}
                onClick={() => { setZona(z); setTab("detalle"); }}
              >
                <div className="zona-icono" style={{ background: z.color+"22", color: z.color }}>
                  <i className={z.icono}/>
                </div>
                <div className="zona-body">
                  <p className="zona-nombre">{z.nombre}</p>
                  <div className="zona-meta">
                    <span><i className="fa-solid fa-users"/> {z.capacidad}</span>
                    <span><i className="fa-regular fa-clock"/> {z.horario[0]}:00–{z.horario[1]}:00</span>
                  </div>
                  <div className="timeline">
                    {ocup.map((r,i) => (
                      <div key={i} className="tl-bloque" style={{
                        left:  `${((r.hi-z.horario[0])/total)*100}%`,
                        width: `${((r.hf-r.hi)/total)*100}%`,
                        background: z.color,
                      }}/>
                    ))}
                  </div>
                  <p className="zona-estado">{ocup.length>0 ? `${ocup.length} reserva${ocup.length>1?"s":""} hoy` : "Libre hoy"}</p>
                </div>
              </div>
            );
          })}
        </div>

        {/* ── Col 2: Calendario ── */}
        <div className={tab!=="calendario"?"oculto-movil":undefined}>
          <p className="col-label">Selecciona una fecha</p>
          <div className="cal-card">
            <div className="cal-nav">
              <button className="cal-btn" onClick={prevMonth}>&#8249;</button>
              <span className="cal-mes">{MESES[month]} {year}</span>
              <button className="cal-btn" onClick={nextMonth}>&#8250;</button>
            </div>
            <div className="cal-grid">
              {DIAS.map(d => <div key={d} className="dia-nombre">{d}</div>)}
              {celdas(year, month).map((d, i) => {
                if (!d) return <div key={`v${i}`}/>;
                const ds = padStr(year, month, d);
                return (
                  <div
                    key={d}
                    className={`dia-celda${ds===fecha?" sel":ds===toStr(hoy)?" hoy":""}${reservas.some(r=>r.fecha===ds)?" dot":""}`}
                    onClick={() => { setFecha(ds); setTab("detalle"); }}
                  >
                    {d}
                  </div>
                );
              })}
            </div>
            <div className="cal-leyenda">
              <span><span className="l-dot ld-hoy"/>Hoy</span>
              <span><span className="l-dot ld-sel"/>Sel.</span>
              <span><span className="l-dot ld-res"/>Reservas</span>
            </div>
          </div>

          <div className="resumen">
            <p className="col-label"><i className="fa-regular fa-calendar-check"/> Reservas del día</p>
            {diaR.length === 0
              ? <p className="resumen-vacio">Sin reservas para este día</p>
              : diaR.map(r => {
                  const z = ZONAS.find(z => z.id===r.zonaId);
                  return (
                    <div key={r.id} className="resumen-item">
                      <div className="resumen-barra" style={{background: z?.color}}/>
                      <i className={z?.icono} style={{color: z?.color}}/>
                      <div>
                        <p className="resumen-zona">{z?.nombre}</p>
                        <p className="resumen-hora">{r.hi}:00–{r.hf}:00 · {r.autor}</p>
                      </div>
                    </div>
                  );
                })
            }
          </div>
        </div>

        {/* ── Col 3: Detalle ── */}
        <div className={tab!=="detalle"?"oculto-movil":undefined}>
          {!zona ? (
            <div className="detalle-vacio">
              <i className="fa-regular fa-hand-pointer"/>
              <p>Selecciona una zona para ver disponibilidad y reservar</p>
            </div>
          ) : (
            <>
              <div className="detalle-header" style={{borderColor: zona.color}}>
                <div className="detalle-icono" style={{background: zona.color+"22", color: zona.color}}>
                  <i className={zona.icono}/>
                </div>
                <div>
                  <p className="detalle-nombre">{zona.nombre}</p>
                  <div className="detalle-chips">
                    <span className="chip"><i className="fa-solid fa-users"/> {zona.capacidad} personas</span>
                    <span className="chip"><i className="fa-regular fa-clock"/> {zona.horario[0]}:00–{zona.horario[1]}:00</span>
                  </div>
                </div>
              </div>

              <p className="detalle-fecha"><i className="fa-regular fa-calendar"/> {fecha}</p>

              <div className="detalle-reservas">
                <p className="col-label">Reservas existentes</p>
                {zonaR.length === 0
                  ? <p className="libre"><i className="fa-solid fa-circle-check" style={{color:"#10b981"}}/> Zona libre todo el día</p>
                  : zonaR.map(r => (
                    <div key={r.id} className="reserva-item">
                      <i className="fa-regular fa-clock" style={{color: zona.color}}/>
                      <span className="r-hora">{r.hi}:00–{r.hf}:00</span>
                      <span className="r-autor">{r.autor}</span>
                      <span className="badge-ok">confirmada</span>
                      <button className="btn-x" onClick={() => setReservas(p => p.filter(x => x.id!==r.id))}>
                        <i className="fa-solid fa-xmark"/>
                      </button>
                    </div>
                  ))
                }
              </div>

              <button className="btn-reservar" style={{background: zona.color}} onClick={() => { setModal(true); setError(""); }}>
                <i className="fa-solid fa-plus"/> Nueva reserva
              </button>
            </>
          )}
        </div>

      </div>

      {/* ── Modal ── */}
      {modal && (
        <div className="modal-overlay" onClick={() => setModal(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            {exito ? (
              <div className="modal-exito">
                <i className="fa-solid fa-circle-check exito-icon"/>
                <p className="exito-titulo">¡Reserva confirmada!</p>
                <p className="exito-sub">{zona.nombre} · {fecha}</p>
              </div>
            ) : (
              <>
                <div className="modal-top">
                  <div className="modal-icono" style={{background: zona.color+"22", color: zona.color}}>
                    <i className={zona.icono}/>
                  </div>
                  <div>
                    <p className="modal-zona">{zona.nombre}</p>
                    <p className="modal-fecha-txt">{fecha}</p>
                  </div>
                  <button className="btn-cerrar" onClick={() => setModal(false)}><i className="fa-solid fa-xmark"/></button>
                </div>
                <div className="modal-body">
                  <div className="modal-field">
                    <label>Tu nombre / piso</label>
                    <input className="modal-input" placeholder="Ej: Vecino 3ºA" value={form.autor} onChange={e => setForm(f => ({...f, autor: e.target.value}))}/>
                  </div>
                  <div className="modal-row">
                    <div className="modal-field">
                      <label>Inicio</label>
                      <select className="modal-input" value={form.hi} onChange={e => setForm(f => ({...f, hi: e.target.value}))}>
                        <option value="">--</option>{horaOpts(zona.horario[0], zona.horario[1]-1)}
                      </select>
                    </div>
                    <div className="modal-field">
                      <label>Fin</label>
                      <select className="modal-input" value={form.hf} onChange={e => setForm(f => ({...f, hf: e.target.value}))}>
                        <option value="">--</option>{horaOpts(zona.horario[0]+1, zona.horario[1])}
                      </select>
                    </div>
                  </div>
                  {error && <div className="modal-error"><i className="fa-solid fa-triangle-exclamation"/> {error}</div>}
                </div>
                <div className="modal-footer">
                  <button className="btn-modal-cancel" onClick={() => setModal(false)}>Cancelar</button>
                  <button className="btn-modal-ok" style={{background: zona.color}} onClick={confirmar}>
                    <i className="fa-solid fa-check"/> Confirmar
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

    </div>
  );
}