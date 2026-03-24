import React, { useState, useEffect } from "react"
import { supabase } from "../../lib/supabase"
import "./MiPerfil.css"

/* ── Helpers ─────────────────────────────────────────────────────────── */
const rolConfig = {
  admin:        { label: "Administrador de Comunidad", icono: "fa-solid fa-shield-halved" },
  presidente:   { label: "Presidente de Comunidad",    icono: "fa-solid fa-star"          },
  ayuntamiento: { label: "Ayuntamiento",               icono: "fa-solid fa-landmark"      },
  conserje:     { label: "Conserje",                   icono: "fa-solid fa-key"           },
  vecino:       { label: "Vecino",                     icono: "fa-solid fa-house-user"    },
}

const permisosConfig = {
  admin: [
    { label: "Panel de control",    activo: true,  icono: "fa-solid fa-chart-line"           },
    { label: "Gestión incidencias", activo: true,  icono: "fa-solid fa-triangle-exclamation" },
    { label: "Subir documentos",    activo: true,  icono: "fa-solid fa-folder-open"          },
    { label: "Gestión de pagos",    activo: true,  icono: "fa-solid fa-credit-card"          },
    { label: "Admin usuarios",      activo: true,  icono: "fa-solid fa-users-gear"           },
  ],
  presidente: [
    { label: "Panel de control",    activo: true,  icono: "fa-solid fa-chart-line"           },
    { label: "Gestión incidencias", activo: true,  icono: "fa-solid fa-triangle-exclamation" },
    { label: "Subir documentos",    activo: true,  icono: "fa-solid fa-folder-open"          },
    { label: "Gestión de pagos",    activo: false, icono: "fa-solid fa-credit-card"          },
    { label: "Admin usuarios",      activo: false, icono: "fa-solid fa-users-gear"           },
  ],
  vecino: [
    { label: "Panel de control",    activo: false, icono: "fa-solid fa-chart-line"           },
    { label: "Gestión incidencias", activo: true,  icono: "fa-solid fa-triangle-exclamation" },
    { label: "Subir documentos",    activo: false, icono: "fa-solid fa-folder-open"          },
    { label: "Gestión de pagos",    activo: false, icono: "fa-solid fa-credit-card"          },
    { label: "Admin usuarios",      activo: false, icono: "fa-solid fa-users-gear"           },
  ],
  conserje: [
    { label: "Panel de control",    activo: true,  icono: "fa-solid fa-chart-line"           },
    { label: "Gestión incidencias", activo: true,  icono: "fa-solid fa-triangle-exclamation" },
    { label: "Subir documentos",    activo: false, icono: "fa-solid fa-folder-open"          },
    { label: "Gestión de pagos",    activo: false, icono: "fa-solid fa-credit-card"          },
    { label: "Admin usuarios",      activo: false, icono: "fa-solid fa-users-gear"           },
  ],
  ayuntamiento: [
    { label: "Panel de control",    activo: true,  icono: "fa-solid fa-chart-line"           },
    { label: "Gestión incidencias", activo: true,  icono: "fa-solid fa-triangle-exclamation" },
    { label: "Subir documentos",    activo: true,  icono: "fa-solid fa-folder-open"          },
    { label: "Gestión de pagos",    activo: false, icono: "fa-solid fa-credit-card"          },
    { label: "Admin usuarios",      activo: false, icono: "fa-solid fa-users-gear"           },
  ],
}

const iconoActividad = {
  reserva:    { clase: "act-reserva", icono: "fa-regular fa-calendar-check"     },
  incidencia: { clase: "act-incid",   icono: "fa-solid fa-triangle-exclamation" },
  documento:  { clase: "act-doc",     icono: "fa-solid fa-file-arrow-up"        },
  chat:       { clase: "act-chat",    icono: "fa-regular fa-comments"           },
}

const getIniciales = (fullName, username, email) => {
  if (fullName) {
    const p = fullName.trim().split(" ")
    return p.length >= 2 ? (p[0][0] + p[1][0]).toUpperCase() : p[0].slice(0, 2).toUpperCase()
  }
  if (username) return username.slice(0, 2).toUpperCase()
  if (email)    return email.slice(0, 2).toUpperCase()
  return "??"
}

const formatFecha = (ts) => {
  if (!ts) return "—"
  return new Date(ts).toLocaleDateString("es-ES", { month: "long", year: "numeric" })
}

const tiempoRelativo = (ts) => {
  if (!ts) return "—"
  const diff = (Date.now() - new Date(ts).getTime()) / 1000
  if (diff < 60)    return "ahora mismo"
  if (diff < 3600)  return `hace ${Math.floor(diff / 60)}m`
  if (diff < 86400) return `hace ${Math.floor(diff / 3600)}h`
  return `hace ${Math.floor(diff / 86400)}d`
}

/* ── Componente ──────────────────────────────────────────────────────── */
export default function MiPerfil() {
  const [perfil,    setPerfil]    = useState(null)
  const [comunidad, setComunidad] = useState(null)
  const [actividad, setActividad] = useState([])
  const [stats,     setStats]     = useState({ incidencias: 0, documentos: 0 })
  const [email,     setEmail]     = useState(null)
  const [cargando,  setCargando]  = useState(true)
  const [error,     setError]     = useState(null)

  useEffect(() => {
    const cargar = async () => {
      setCargando(true)

      /* ── 1. Auth ── */
      const { data: { user }, error: authError } = await supabase.auth.getUser()
      if (authError || !user) {
        setError("No has iniciado sesión.")
        setCargando(false)
        return
      }
      setEmail(user.email)

      /* ── 2. Perfil (columnas confirmadas) ── */
      const { data: perfilData, error: perfilError } = await supabase
        .from("profiles")
        .select("id, email, role, comunidad_id, created_at, username, full_name, avatar_url, telefono, piso")
        .eq("id", user.id)
        .single()

      if (perfilError) {
        setError("No se pudo cargar el perfil.")
        setCargando(false)
        return
      }
      setPerfil(perfilData)

      /* ── 3. Comunidad (query independiente, no bloquea si falla) ── */
      if (perfilData.comunidad_id) {
        try {
          const { data: comunidadData, error: comErr } = await supabase
            .from("comunidades")
            .select("nombre, tipo, ciudad, num_vecinos, num_bloques, num_zonas_comunes")
            .eq("id", perfilData.comunidad_id)
            .maybeSingle()

          if (comErr) {
            console.warn("Error cargando comunidad:", comErr.message)
          } else {
            console.log("Comunidad:", comunidadData)
            if (comunidadData) setComunidad(comunidadData)
          }
        } catch (e) {
          console.warn("Excepción comunidad:", e)
        }
      }

      /* ── 4. Stats (cada una protegida, si la tabla no existe devuelve 0) ── */
      const ahora = new Date()
      const inicioMes = new Date(ahora.getFullYear(), ahora.getMonth(), 1).toISOString()

      const safeCount = async (query) => {
        try {
          const { count } = await query
          return count ?? 0
        } catch { return 0 }
      }

      const [totalInc, totalDoc] = await Promise.all([
        safeCount(
          supabase.from("incidencias")
            .select("id", { count: "exact", head: true })
            .eq("autor_id", user.id)
        ),
        safeCount(
          supabase.from("documentos")
            .select("id", { count: "exact", head: true })
            .eq("subido_por", user.id)
        ),
      ])
      setStats({ incidencias: totalInc, documentos: totalDoc })

      /* ── 5. Actividad reciente (tabla opcional, no bloquea si no existe) ── */
      try {
        const { data: actData, error: actErr } = await supabase
          .from("actividad_usuario")
          .select("tipo, titulo, descripcion, created_at")
          .eq("user_id", user.id)
          .order("created_at", { ascending: false })
          .limit(4)

        if (!actErr && actData) setActividad(actData)
      } catch (e) {
        console.warn("actividad_usuario no disponible aún:", e)
      }

      setCargando(false)
    }

    cargar()
  }, [])

  /* ── Progreso dinámico ── */
  const calcularProgreso = () => {
    if (!perfil) return []
    const camposRellenos = [perfil.full_name, perfil.telefono, perfil.piso, perfil.username, perfil.avatar_url]
      .filter(Boolean).length
    const perfilPct      = Math.round((camposRellenos / 5) * 100)
    const actividadPct   = Math.min(100, (stats.incidencias  + stats.documentos) * 10)
    const interaccionPct = Math.min(100, stats.incidencias * 15 )
    return [
      { label: "Perfil completado",   valor: perfilPct      },
      { label: "Actividad este mes",  valor: actividadPct   },
      { label: "Interacción vecinal", valor: interaccionPct },
    ]
  }

  /* ── Loading / Error ── */
  if (cargando) return (
    <div className="perfil-page perfil-center">
      <div className="perfil-spinner" />
    </div>
  )
  if (error) return (
    <div className="perfil-page perfil-center">
      <p className="perfil-error">{error}</p>
    </div>
  )

  /* ── Datos derivados ── */
  const rol            = rolConfig[perfil.role] ?? { label: perfil.role, icono: "fa-solid fa-user" }
  const avatarIni      = getIniciales(perfil.full_name, perfil.username, email)
  const nombreMostrado = perfil.full_name || perfil.username || "Sin nombre"
  const emailMostrado  = email || perfil.email || "—"
  const ciudadMostrada = comunidad?.ciudad || "—"
  const permisos       = permisosConfig[perfil.role] ?? permisosConfig.vecino
  const progreso       = calcularProgreso()

  const STATS_LIST = [
    { valor: stats.incidencias, sufijo: "", label: "Incidencias gestionadas", clase: "accent"  },
    { valor: stats.documentos,  sufijo: "", label: "Documentos subidos",      clase: "success" },
  ]

  return (
    <div className="perfil-page">

      {/* HERO */}
      <section className="perfil-hero">
        <div className="perfil-hero-bg" />
        <div className="perfil-hero-inner">

          <div className="perfil-avatar-wrap">
            <div className="perfil-avatar">{avatarIni}</div>
            <span className="perfil-status-dot" title="En línea" />
          </div>

          <div className="perfil-hero-info">
            <h1 className="perfil-nombre"><span>{nombreMostrado}</span></h1>
            <div className="perfil-role-badge">
              <i className={rol.icono} />{rol.label}
            </div>
            <div className="perfil-meta">
              {ciudadMostrada !== "—" && (
                <span className="perfil-meta-item">
                  <i className="fa-solid fa-location-dot" />{ciudadMostrada}
                </span>
              )}
              <span className="perfil-meta-item">
                <i className="fa-regular fa-envelope" />{emailMostrado}
              </span>
              <span className="perfil-meta-item">
                <i className="fa-solid fa-calendar-days" />
                Miembro desde {formatFecha(perfil.created_at)}
              </span>
            </div>
          </div>

        </div>
      </section>

      {/* STATS BAR */}
      <div className="perfil-stats-bar">
        {STATS_LIST.map((s, i) => (
          <div className="perfil-stat" key={i}>
            <div className={`perfil-stat-value ${s.clase}`}>{s.valor}{s.sufijo}</div>
            <div className="perfil-stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {/* BODY GRID */}
      <div className="perfil-body">

        {/* Columna izquierda */}
        <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>

          {/* Información personal */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Información personal</span>
            </div>
            <div className="perfil-card-body">
              <div className="info-list">
                {[
                  { icono: "fa-regular fa-envelope",   label: "Correo electrónico", valor: emailMostrado           },
                  { icono: "fa-solid fa-phone",         label: "Teléfono",           valor: perfil.telefono || "—"  },
                  { icono: "fa-solid fa-building",      label: "Comunidad",          valor: comunidad?.nombre || "—"},
                  { icono: "fa-solid fa-door-open",     label: "Piso / Unidad",      valor: perfil.piso || "—"      },
                  { icono: "fa-solid fa-calendar-days", label: "Miembro desde",      valor: formatFecha(perfil.created_at) },
                  { icono: "fa-solid fa-location-dot",  label: "Ciudad",             valor: ciudadMostrada          },
                ].map((item, i) => (
                  <div className="info-item" key={i}>
                    <div className="info-icon"><i className={item.icono} /></div>
                    <div>
                      <div className="info-label">{item.label}</div>
                      <div className="info-value">{item.valor}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Mi comunidad — solo se muestra si cargó correctamente */}
          {comunidad ? (
            <div className="perfil-card">
              <div className="comunidad-header-img">
                <i className="fa-solid fa-city" />
              </div>
              <div className="comunidad-info">
                <div className="comunidad-nombre">{comunidad.nombre}</div>
                <div className="comunidad-desc">
                  {comunidad.tipo ?? "Comunidad activa"}
                  {ciudadMostrada !== "—" ? ` · ${ciudadMostrada}` : ""}
                </div>
                <div className="comunidad-stats">
                  <div className="comunidad-stat">
                    <div className="comunidad-stat-val">{comunidad.num_vecinos       ?? "—"}</div>
                    <div className="comunidad-stat-lbl">Vecinos</div>
                  </div>
                  <div className="comunidad-stat">
                    <div className="comunidad-stat-val">{comunidad.num_bloques       ?? "—"}</div>
                    <div className="comunidad-stat-lbl">Bloques</div>
                  </div>
                  <div className="comunidad-stat">
                    <div className="comunidad-stat-val">{comunidad.num_zonas_comunes ?? "—"}</div>
                    <div className="comunidad-stat-lbl">Zonas comunes</div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="perfil-card">
              <div className="perfil-card-body">
                <p className="perfil-empty">Sin comunidad asignada.</p>
              </div>
            </div>
          )}

        </div>

        {/* Columna derecha */}
        <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>

          {/* Actividad reciente */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Actividad reciente</span>
              <button className="perfil-card-action">Ver todo</button>
            </div>
            <div className="perfil-card-body">
              {actividad.length === 0
                ? <p className="perfil-empty">Sin actividad reciente.</p>
                : (
                  <div className="actividad-list">
                    {actividad.map((a, i) => {
                      const cfg = iconoActividad[a.tipo] ?? { clase: "act-chat", icono: "fa-regular fa-bell" }
                      return (
                        <div className="actividad-item" key={i}>
                          <div className={`actividad-icon ${cfg.clase}`}>
                            <i className={cfg.icono} />
                          </div>
                          <div className="actividad-content">
                            <div className="actividad-titulo">{a.titulo}</div>
                            <div className="actividad-desc">{a.descripcion}</div>
                          </div>
                          <div className="actividad-tiempo">{tiempoRelativo(a.created_at)}</div>
                        </div>
                      )
                    })}
                  </div>
                )
              }
            </div>
          </div>

          {/* Permisos de acceso */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Permisos de acceso</span>
            </div>
            <div className="perfil-card-body">
              <div className="permisos-grid">
                {permisos.map((p, i) => (
                  <div className={`permiso-chip ${p.activo ? "activo" : ""}`} key={i}>
                    <i className={p.icono} />{p.label}
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Participación */}
          <div className="perfil-card">
            <div className="perfil-card-header">
              <span className="perfil-card-title">Participación</span>
            </div>
            <div className="perfil-card-body">
              {progreso.map((p, i) => (
                <div className="progreso-item" key={i}>
                  <div className="progreso-header">
                    <span className="progreso-label">{p.label}</span>
                    <span className="progreso-valor">{p.valor}%</span>
                  </div>
                  <div className="progreso-bar">
                    <div className="progreso-fill" style={{ width: `${p.valor}%` }} />
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