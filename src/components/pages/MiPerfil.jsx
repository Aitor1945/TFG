import React, { useState } from "react"
import "./MiPerfil.css"

/* ── Mock del usuario logueado ─────────────────────────────────────────── */
const USUARIO = {
  nombre: "Jose",
  apellidos: "Luis Torrente",
  iniciales: "JT",
  rol: "Administrador de Comunidad",
  email: "j.luis@barriored.es",
  telefono: "+34 612 345 678",
  comunidad: "Residencial de Madrid",
  piso: "3ºB – Bloque 2",
  miembro_desde: "enero 2022",
  ultima_conexion: "Hace 5 minutos",
  ciudad: "Madrid, España",
}

const STATS = [
  { valor: 24,  sufijo: "",  label: "Incidencias gestionadas", clase: "accent"  },
  { valor: 8,   sufijo: "",  label: "Reservas este mes",       clase: ""        },
  { valor: 3,   sufijo: "",  label: "Documentos subidos",      clase: "success" },
  { valor: 97,  sufijo: "%", label: "Satisfacción vecinal",    clase: "warning" },
]

const ACTIVIDAD = [
  {
    tipo: "act-reserva",
    icono: "fa-regular fa-calendar-check",
    titulo: "Reserva confirmada",
    desc: "Sala comunitaria · Sábado 15:00h",
    tiempo: "hace 2h",
  },
  {
    tipo: "act-incid",
    icono: "fa-solid fa-triangle-exclamation",
    titulo: "Incidencia resuelta",
    desc: "Avería ascensor Bloque 1",
    tiempo: "hace 1d",
  },
  {
    tipo: "act-doc",
    icono: "fa-solid fa-file-arrow-up",
    titulo: "Documento publicado",
    desc: "Acta Reunión Marzo 2025",
    tiempo: "hace 3d",
  },
  {
    tipo: "act-chat",
    icono: "fa-regular fa-comments",
    titulo: "Mensaje destacado",
    desc: "Recordatorio pago cuota trimestral",
    tiempo: "hace 5d",
  },
]

const PERMISOS = [
  { label: "Panel de control",  activo: true,  icono: "fa-solid fa-chart-line"           },
  { label: "Gestión incidencias", activo: true,  icono: "fa-solid fa-triangle-exclamation"},
  { label: "Subir documentos",  activo: true,  icono: "fa-solid fa-folder-open"          },
  { label: "Gestión de pagos",  activo: false, icono: "fa-solid fa-credit-card"           },
  { label: "Crear reservas",    activo: true,  icono: "fa-regular fa-calendar-check"      },
  { label: "Admin usuarios",    activo: false, icono: "fa-solid fa-users-gear"            },
]

const PROGRESO = [
  { label: "Perfil completado",      valor: 85 },
  { label: "Actividad este mes",     valor: 72 },
  { label: "Interacción vecinal",    valor: 60 },
]

/* ── Componente ─────────────────────────────────────────────────────────── */
export default function MiPerfil() {
  const [editando, setEditando] = useState(false)

  return (
    <div className="perfil-page">

      {/* ── HERO ───────────────────────────────────────────────── */}
      <section className="perfil-hero">
        <div className="perfil-hero-bg" />
        <div className="perfil-hero-inner">

          {/* Avatar */}
          <div className="perfil-avatar-wrap">
            <div className="perfil-avatar">{USUARIO.iniciales}</div>
            <span className="perfil-status-dot" title="En línea" />
          </div>

          {/* Info */}
          <div className="perfil-hero-info">
            <h1 className="perfil-nombre">
              <span>{USUARIO.nombre} {USUARIO.apellidos}</span>
            </h1>
            <div className="perfil-role-badge">
              <i className="fa-solid fa-shield-halved" />
              {USUARIO.rol}
            </div>
            <div className="perfil-meta">
              <span className="perfil-meta-item">
                <i className="fa-solid fa-location-dot" />{USUARIO.ciudad}
              </span>
              <span className="perfil-meta-item">
                <i className="fa-regular fa-envelope" />{USUARIO.email}
              </span>
              <span className="perfil-meta-item">
                <i className="fa-solid fa-clock" />Última conexión: {USUARIO.ultima_conexion}
              </span>
            </div>
          </div>

          {/* Acciones */}
          <div className="perfil-hero-actions">
            <button className="btn-perfil-primary" onClick={() => setEditando(!editando)}>
              <i className={`fa-solid ${editando ? "fa-check" : "fa-pen"}`} />
              {editando ? "Guardar cambios" : "Editar perfil"}
            </button>
            <button className="btn-perfil-outline">
              <i className="fa-solid fa-bell" />
              Notificaciones
            </button>
          </div>

        </div>
      </section>

      {/* ── STATS BAR ──────────────────────────────────────────── */}
      <div className="perfil-stats-bar">
        {STATS.map((s, i) => (
          <div className="perfil-stat" key={i}>
            <div className={`perfil-stat-value ${s.clase}`}>
              {s.valor}{s.sufijo}
            </div>
            <div className="perfil-stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {/* ── BODY GRID ──────────────────────────────────────────── */}
      <div className="perfil-body">

        {/* ── Columna izquierda ── */}
        <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>

          {/* Información personal */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Información personal</span>
              <button className="perfil-card-action" onClick={() => setEditando(!editando)}>
                {editando ? "Cancelar" : "Editar"}
              </button>
            </div>
            <div className="perfil-card-body">
              <div className="info-list">
                {[
                  { icono: "fa-regular fa-envelope",        label: "Correo electrónico", valor: USUARIO.email          },
                  { icono: "fa-solid fa-phone",             label: "Teléfono",           valor: USUARIO.telefono       },
                  { icono: "fa-solid fa-building",          label: "Comunidad",          valor: USUARIO.comunidad      },
                  { icono: "fa-solid fa-door-open",         label: "Piso / Unidad",      valor: USUARIO.piso           },
                  { icono: "fa-solid fa-calendar-days",     label: "Miembro desde",      valor: USUARIO.miembro_desde  },
                  { icono: "fa-solid fa-location-dot",      label: "Ciudad",             valor: USUARIO.ciudad         },
                ].map((item, i) => (
                  <div className="info-item" key={i}>
                    <div className="info-icon">
                      <i className={item.icono} />
                    </div>
                    <div>
                      <div className="info-label">{item.label}</div>
                      <div className="info-value">{item.valor}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Mi comunidad */}
          <div className="perfil-card">
            <div className="comunidad-header-img">
              <i className="fa-solid fa-city" />
            </div>
            <div className="comunidad-info">
              <div className="comunidad-nombre">{USUARIO.comunidad}</div>
              <div className="comunidad-desc">
                Comunidad activa · Madrid, España
              </div>
              <div className="comunidad-stats">
                <div className="comunidad-stat">
                  <div className="comunidad-stat-val">142</div>
                  <div className="comunidad-stat-lbl">Vecinos</div>
                </div>
                <div className="comunidad-stat">
                  <div className="comunidad-stat-val">6</div>
                  <div className="comunidad-stat-lbl">Bloques</div>
                </div>
                <div className="comunidad-stat">
                  <div className="comunidad-stat-val">4</div>
                  <div className="comunidad-stat-lbl">Zonas comunes</div>
                </div>
              </div>
            </div>
          </div>

        </div>

        {/* ── Columna derecha ── */}
        <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>

          {/* Actividad reciente */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Actividad reciente</span>
              <button className="perfil-card-action">Ver todo</button>
            </div>
            <div className="perfil-card-body">
              <div className="actividad-list">
                {ACTIVIDAD.map((a, i) => (
                  <div className="actividad-item" key={i}>
                    <div className={`actividad-icon ${a.tipo}`}>
                      <i className={a.icono} />
                    </div>
                    <div className="actividad-content">
                      <div className="actividad-titulo">{a.titulo}</div>
                      <div className="actividad-desc">{a.desc}</div>
                    </div>
                    <div className="actividad-tiempo">{a.tiempo}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Permisos de acceso */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Permisos de acceso</span>
            </div>
            <div className="perfil-card-body">
              <div className="permisos-grid">
                {PERMISOS.map((p, i) => (
                  <div className={`permiso-chip ${p.activo ? "activo" : ""}`} key={i}>
                    <i className={p.icono} />
                    {p.label}
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Progreso del perfil */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Participación</span>
            </div>
            <div className="perfil-card-body">
              {PROGRESO.map((p, i) => (
                <div className="progreso-item" key={i}>
                  <div className="progreso-header">
                    <span className="progreso-label">{p.label}</span>
                    <span className="progreso-valor">{p.valor}%</span>
                  </div>
                  <div className="progreso-bar">
                    <div
                      className="progreso-fill"
                      style={{ width: `${p.valor}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}