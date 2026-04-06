import React, { useState, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { supabase } from "../../../lib/supabase.js"
import "./Dashboard.css"

// ─── Constantes ────────────────────────────────────────────────────────────
const MONTHS_ES = ["Enero","Febrero","Marzo","Abril","Mayo","Junio",
                   "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"]
const DAYS_ES   = ["L","M","X","J","V","S","D"]
const DAYS_FULL = ["Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"]

const EVENT_COLORS = {
  reunion:    "#3b82f6",
  reserva:    "#10b981",
  incidencia: "#f59e0b",
  nota:       "#a78bfa",
}

const ROLE_LABELS = {
  vecino:         "Vecino",
  presidente:     "Presidente",
  administrador:  "Administrador",
  ayuntamiento:   "Ayuntamiento",
}

const ESTADO_COLORS = {
  abierta:    "#f59e0b",
  "en curso": "#3b82f6",
  resuelta:   "#10b981",
}

const CURRENT_USER_FALLBACK = { name: "Usuario", role: "vecino", community: "" }

const ACCESOS = [
  { label: "Incidencias",  icon: "fa-triangle-exclamation", to: "/incidencias", color: "#f59e0b" },
  { label: "Reservas",     icon: "fa-calendar-check",       to: "/reservas",    color: "#10b981" },
  { label: "Documentos",   icon: "fa-folder-open",          to: "/documentos",  color: "#3b82f6" },
  { label: "Chat",         icon: "fa-comments",             to: "/chat",        color: "#a78bfa" },
]


function weatherInfo(code) {
  if (code === 0)                      return { desc: "Despejado",   icon: "fa-sun" }
  if ([1, 2].includes(code))           return { desc: "Poco nuboso", icon: "fa-cloud-sun" }
  if (code === 3)                      return { desc: "Nublado",     icon: "fa-cloud" }
  if ([45, 48].includes(code))         return { desc: "Niebla",      icon: "fa-smog" }
  if ([51, 53, 55].includes(code))     return { desc: "Llovizna",    icon: "fa-cloud-drizzle" }
  if ([61, 63, 65].includes(code))     return { desc: "Lluvia",      icon: "fa-cloud-rain" }
  if ([71, 73, 75, 77].includes(code)) return { desc: "Nieve",       icon: "fa-snowflake" }
  if ([80, 81, 82].includes(code))     return { desc: "Chubascos",   icon: "fa-cloud-showers-heavy" }
  if ([95, 96, 99].includes(code))     return { desc: "Tormenta",    icon: "fa-bolt" }
  return { desc: "Variable", icon: "fa-cloud" }
}

//Calendario
function getDays(year, month) {
  const first     = new Date(year, month, 1).getDay()
  const offset    = first === 0 ? 6 : first - 1
  const total     = new Date(year, month + 1, 0).getDate()
  const prevTotal = new Date(year, month, 0).getDate()
  const cells     = []
  for (let i = offset - 1; i >= 0; i--) cells.push({ d: prevTotal - i, other: true })
  for (let i = 1; i <= total; i++)       cells.push({ d: i, other: false })
  while (cells.length % 7 !== 0)         cells.push({ d: cells.length - total - offset + 1, other: true })
  return cells
}

//Tiempo
function WeatherCard() {
  const [weather, setWeather] = useState(null)
  const [status,  setStatus]  = useState("loading")

  useEffect(() => {
    if (!navigator.geolocation) { setStatus("error"); return }
    navigator.geolocation.getCurrentPosition(
      async ({ coords }) => {
        try {
          const { latitude: lat, longitude: lon } = coords
          const [geoRes, wtRes] = await Promise.all([
            fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`,
              { headers: { "Accept-Language": "es" } }),
            fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}` +
              `&current=temperature_2m,weathercode,windspeed_10m,relative_humidity_2m&timezone=auto`)
          ])
          const geoData = await geoRes.json()
          const wtData  = await wtRes.json()
          const cur     = wtData.current
          setWeather({
            temp:     Math.round(cur.temperature_2m),
            code:     cur.weathercode,
            wind:     Math.round(cur.windspeed_10m),
            humidity: cur.relative_humidity_2m,
            city: geoData.address?.neighbourhood
               || geoData.address?.suburb
               || geoData.address?.village
               || geoData.address?.town
               || geoData.address?.city
               || geoData.address?.municipality
               || geoData.address?.county
               || "Tu ubicación",
          })
          setStatus("ok")
        } catch { setStatus("error") }
      },
      () => setStatus("error")
    )
  }, [])

  if (status === "loading") return (
    <div className="weather-card weather-loading">
      <i className="fa-solid fa-spinner fa-spin"></i>
      <span>Obteniendo ubicación…</span>
    </div>
  )
  if (status === "error") return (
    <div className="weather-card weather-error">
      <i className="fa-solid fa-location-slash"></i>
      <span>Activa los permisos de ubicación para ver el tiempo.</span>
    </div>
  )

  const { desc, icon } = weatherInfo(weather.code)
  return (
    <div className="weather-card">
      <div className="weather-top">
        <div className="weather-left">
          <p className="weather-city">
            <i className="fa-solid fa-location-dot"></i> {weather.city}
          </p>
          <p className="weather-temp">{weather.temp}°C</p>
          <p className="weather-desc">{desc}</p>
        </div>
        <i className={`fa-solid ${icon} weather-icon`}></i>
      </div>
      <div className="weather-stats">
        <div className="weather-stat">
          <i className="fa-solid fa-wind"></i><span>{weather.wind} km/h</span>
        </div>
        <div className="weather-stat">
          <i className="fa-solid fa-droplet"></i><span>{weather.humidity}%</span>
        </div>
      </div>
    </div>
  )
}

export default function Dashboard() {
  const navigate = useNavigate()
  const now      = new Date()

  const [year,        setYear]        = useState(now.getFullYear())
  const [month,       setMonth]       = useState(now.getMonth())
  const [selected,    setSel]         = useState(now.getDate())
 const [events, setEvents] = useState({})
  const [showSummary, setShowSummary] = useState(false)
  const [showAdd,     setShowAdd]     = useState(false)
  const [form,        setForm]        = useState({ type: "nota", title: "", time: "" })

  const [currentUser, setCurrentUser] = useState(CURRENT_USER_FALLBACK)
  const [incidencias, setIncidencias] = useState([])
  const [loadingData, setLoadingData] = useState(true)

  useEffect(() => {
    async function fetchData() {
      setLoadingData(true)
      try {
        // 1. Usuario autenticado
        const { data: { user } } = await supabase.auth.getUser()
        if (user) {
          // 2. Perfil 
          const { data: profile } = await supabase
            .from("profiles")
            .select("full_name, username, email, role, comunidad_id, comunidades(nombre)")
            .eq("id", user.id)
            .single()

          if (profile) {
            setCurrentUser({
              name:      profile.full_name || profile.username || user.email,
              role:      profile.role      || "vecino",
              community: profile.comunidades?.nombre || "",
            })
          }
        }

        // 3. Últimas 3 incidencias — columnas: titulo, estado
        const { data: incs } = await supabase
          .from("incidencias")
          .select("id, titulo, estado")
          .order("created_at", { ascending: false })
          .limit(3)

        if (incs) setIncidencias(incs)

      } catch (err) {
        console.error("Error cargando datos dashboard:", err)
      } finally {
        setLoadingData(false)
      }
    }
    fetchData()
  }, [])

  const isCurrentMonth = year === now.getFullYear() && month === now.getMonth()
  const cells          = getDays(year, month)
  const selDay         = new Date(year, month, selected)
  const dayEvents      = events[selected] || []
  const totalEvents    = Object.values(events).reduce((acc, a) => acc + a.length, 0)

  function prevMonth() {
    if (month === 0) { setMonth(11); setYear(y => y - 1) } else setMonth(m => m - 1)
    setSel(1)
  }
  function nextMonth() {
    if (month === 11) { setMonth(0); setYear(y => y + 1) } else setMonth(m => m + 1)
    setSel(1)
  }
  function handleSave() {
    if (!form.title.trim()) return
    setEvents(prev => ({
      ...prev,
      [selected]: [...(prev[selected] || []), {
        time:  form.time ? form.time + "h" : "Sin hora",
        title: form.title.trim(),
        type:  form.type,
      }],
    }))
    setForm({ type: "nota", title: "", time: "" })
    setShowAdd(false)
  }

  return (
    <div className="dashboard-page">

      {/* ── Bienvenida con rol ── */}
      <div className="welcome-bar">
        <div className="welcome-left">
          <div className="welcome-avatar">
            {currentUser.name.split(" ").map(n => n[0]).join("").slice(0, 2).toUpperCase()}
          </div>
          <div>
            <p className="welcome-name">Bienvenido, {currentUser.name.split(" ")[0]}</p>
            <p className="welcome-meta">{currentUser.community}</p>
          </div>
        </div>
        <span className="welcome-role">{ROLE_LABELS[currentUser.role] || currentUser.role}</span>
      </div>

      {/* ── Grid principal ── */}
      <div className="dashboard-grid">

        {/* ══ IZQUIERDA: Calendario ══ */}
        <div className="cal-card">

          <div className="big-date-row">
            <div className="big-num">{String(selected).padStart(2, "0")}</div>
            <div className="big-info">
              <div className="big-day">{DAYS_FULL[selDay.getDay()]}</div>
              <div className="big-mon">{MONTHS_ES[month]} {year}</div>
            </div>
            <div className="nav-group">
              <button className="nav-btn" onClick={prevMonth}>&#8249;</button>
              <button className="nav-btn" onClick={nextMonth}>&#8250;</button>
            </div>
          </div>

          <div className="mini-grid">
            {DAYS_ES.map(d => <div key={d} className="day-name">{d}</div>)}
            {cells.map((cell, i) => {
              const isToday = isCurrentMonth && !cell.other && cell.d === now.getDate()
              const isSel   = !cell.other && cell.d === selected
              const hasDot  = !cell.other && events[cell.d]?.length > 0
              let cls = "day-cell"
              if (cell.other)   cls += " day-cell--other"
              if (isSel)        cls += " day-cell--selected"
              else if (isToday) cls += " day-cell--today"
              if (hasDot)       cls += " day-cell--dot"
              return (
                <div key={i} className={cls} onClick={() => !cell.other && setSel(cell.d)}>
                  {cell.d}
                </div>
              )
            })}
          </div>

          <div className="cal-divider" />

          <div className="agenda-section">
            <p className="agenda-header">
              {dayEvents.length > 0 ? `${dayEvents.length} evento${dayEvents.length > 1 ? "s" : ""}` : "Sin eventos"}
            </p>
            {dayEvents.length === 0 && <p className="agenda-empty">No hay nada programado para este día.</p>}
            {dayEvents.map((ev, i) => (
              <div key={i} className="agenda-item">
                <div className="agenda-bar" style={{ background: EVENT_COLORS[ev.type] || EVENT_COLORS.nota }} />
                <div>
                  <div className="agenda-time">{ev.time}</div>
                  <div className="agenda-title">{ev.title}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="cal-divider" />

          <div className="cal-actions">
            <button className="cal-action-btn cal-action-btn--summary" onClick={() => setShowSummary(true)}>
              <i className="fa-solid fa-list-ul"></i> Resumen ({totalEvents})
            </button>
            <button className="cal-action-btn cal-action-btn--add" onClick={() => setShowAdd(true)}>
              <i className="fa-solid fa-plus"></i> Añadir
            </button>
          </div>
        </div>

        {/* ══ DERECHA: columna de widgets ══ */}
        <div className="right-col">

          {/* Tiempo */}
          <WeatherCard />

          {/* Accesos rápidos */}
          <div className="dash-card">
            <div className="dash-card-header">
              <i className="fa-solid fa-bolt"></i>
              <span>Accesos rápidos</span>
            </div>
            <div className="accesos-grid">
              {ACCESOS.map(a => (
                <button key={a.to} className="acceso-btn" onClick={() => navigate(a.to)}
                  style={{ "--acceso-color": a.color }}>
                  <i className={`fa-solid ${a.icon}`}></i>
                  <span>{a.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Últimas incidencias */}
          <div className="dash-card">
            <div className="dash-card-header">
              <i className="fa-solid fa-triangle-exclamation"></i>
              <span>Últimas incidencias</span>
            </div>
            <div className="dash-list">
              {loadingData && (
                <p style={{ fontSize: 12, color: "var(--text-secondary)", opacity: 0.6 }}>
                  Cargando…
                </p>
              )}
              {!loadingData && incidencias.length === 0 && (
                <p style={{ fontSize: 12, color: "var(--text-secondary)", opacity: 0.6 }}>
                  No hay incidencias registradas.
                </p>
              )}
              {incidencias.map(inc => (
                <div key={inc.id} className="dash-list-item">
                  <div className="dash-list-bar"
                    style={{ background: ESTADO_COLORS[inc.estado] || "#a78bfa" }} />
                  {/* ← inc.titulo en vez de inc.title */}
                  <span className="dash-list-title">{inc.titulo}</span>
                  <span className="dash-badge"
                    style={{
                      background: (ESTADO_COLORS[inc.estado] || "#a78bfa") + "22",
                      color:      ESTADO_COLORS[inc.estado] || "#a78bfa",
                    }}>
                    {inc.estado}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Anuncio — sin tabla aún, se mantiene estático */}
          <div className="anuncio-card">
            <div className="anuncio-header">
              <i className="fa-solid fa-bullhorn"></i>
              <span>Anuncio oficial</span>
            </div>
            <p className="anuncio-autor">Ayuntamiento · 12 mar 2026</p>
            <p className="anuncio-titulo">Corte de agua el 17 de marzo</p>
            <p className="anuncio-cuerpo">
              Se informa a todos los vecinos que el próximo lunes 17 de marzo se realizarán
              trabajos de mantenimiento en la red de suministro. El corte afectará de 9:00h a 14:00h.
            </p>
          </div>

        </div>
      </div>

      {/* ── Modal Resumen ── */}
      {showSummary && (
        <div className="modal-overlay" onClick={() => setShowSummary(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <p className="modal-title">Resumen del mes</p>
            <p className="modal-date">{MONTHS_ES[month]} {year} · {totalEvents} eventos en total</p>
            <div className="modal-event-list">
              {totalEvents === 0 && <p className="modal-empty">No hay eventos este mes.</p>}
              {Object.entries(events)
                .sort(([a], [b]) => Number(a) - Number(b))
                .map(([day, evs]) => evs.map((ev, i) => (
                  <div key={`${day}-${i}`} className="modal-event-row">
                    <div className="modal-event-bar" style={{ background: EVENT_COLORS[ev.type] || EVENT_COLORS.nota }} />
                    <div>
                      <div className="modal-event-time">Día {day} · {ev.time}</div>
                      <div className="modal-event-name">{ev.title}</div>
                    </div>
                  </div>
                )))}
            </div>
            <div className="modal-footer">
              <button className="modal-cancel-btn" onClick={() => setShowSummary(false)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal Añadir ── */}
      {showAdd && (
        <div className="modal-overlay" onClick={() => setShowAdd(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <p className="modal-title">Añadir al día {selected}</p>
            <p className="modal-date">{DAYS_FULL[selDay.getDay()]}, {MONTHS_ES[month]} {year}</p>
            <div className="modal-form">
              <div className="modal-type-row">
                {Object.keys(EVENT_COLORS).map(type => (
                  <button key={type}
                    className={`modal-type-btn${form.type === type ? " active" : ""}`}
                    style={form.type === type
                      ? { borderColor: EVENT_COLORS[type], color: EVENT_COLORS[type], background: `${EVENT_COLORS[type]}18` }
                      : {}}
                    onClick={() => setForm(f => ({ ...f, type }))}
                  >
                    {type.charAt(0).toUpperCase() + type.slice(1)}
                  </button>
                ))}
              </div>
              <input className="modal-input" placeholder="Título o descripción…"
                value={form.title}
                onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
                onKeyDown={e => e.key === "Enter" && handleSave()}
              />
              <input className="modal-input" type="time"
                value={form.time}
                onChange={e => setForm(f => ({ ...f, time: e.target.value }))}
              />
            </div>
            <div className="modal-footer">
              <button className="modal-cancel-btn" onClick={() => setShowAdd(false)}>Cancelar</button>
              <button className="modal-save-btn" onClick={handleSave}>Guardar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}