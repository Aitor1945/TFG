import React, { useState, useEffect } from "react"
import { supabase } from "../../../lib/supabase"
import "./Ajustes.css"

export default function Ajustes() {
  // ── Fuente ──────────────────────────────────────────────────────────────
  const [fontSize, setFontSize] = useState(() => localStorage.getItem("br-fontsize") || "normal")

  useEffect(() => {
    const map = { pequeño: "14px", normal: "16px", grande: "19px" }
    document.documentElement.style.setProperty("--app-font-size", map[fontSize])
    localStorage.setItem("br-fontsize", fontSize)
  }, [fontSize])

  // ── Notificaciones ───────────────────────────────────────────────────────
  const [notifChat, setNotifChat]   = useState(() => localStorage.getItem("br-notif-chat")  !== "false")
  const [notifMuro, setNotifMuro]   = useState(() => localStorage.getItem("br-notif-muro")  !== "false")
  const [notifEmail, setNotifEmail] = useState(() => localStorage.getItem("br-notif-email") !== "false")

  const toggleNotif = (key, val, setter) => {
    setter(val)
    localStorage.setItem(key, val)
  }

  // ── Cuenta / Cambiar contraseña ──────────────────────────────────────────
  const [passActual,    setPassActual]    = useState("")
  const [passNueva,     setPassNueva]     = useState("")
  const [passConfirmar, setPassConfirmar] = useState("")
  const [showPass,      setShowPass]      = useState({ actual: false, nueva: false, conf: false })
  const [passError,     setPassError]     = useState("")
  const [passOk,        setPassOk]        = useState(false)
  const [passLoading,   setPassLoading]   = useState(false)

  const cambiarPassword = async (e) => {
    e.preventDefault()
    setPassError("")
    setPassOk(false)
    if (passNueva.length < 6)             return setPassError("La contraseña debe tener al menos 6 caracteres.")
    if (passNueva !== passConfirmar)      return setPassError("Las contraseñas no coinciden.")
    setPassLoading(true)
    const { error } = await supabase.auth.updateUser({ password: passNueva })
    setPassLoading(false)
    if (error) return setPassError("Error al actualizar: " + error.message)
    setPassOk(true)
    setPassActual(""); setPassNueva(""); setPassConfirmar("")
    setTimeout(() => setPassOk(false), 3500)
  }

  // ── Perfil editable ──────────────────────────────────────────────────────
  const [perfil,        setPerfil]        = useState({ full_name: "", telefono: "", piso: "" })
  const [perfilOriginal,setPerfilOriginal]= useState({ full_name: "", telefono: "", piso: "" })
  const [perfilLoading, setPerfilLoading] = useState(false)
  const [perfilOk,      setPerfilOk]      = useState(false)
  const [perfilError,   setPerfilError]   = useState("")
  const [userId,        setUserId]        = useState(null)

  useEffect(() => {
    const cargar = async () => {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      setUserId(user.id)
      const { data } = await supabase
        .from("profiles")
        .select("full_name, telefono, piso")
        .eq("id", user.id)
        .single()
      if (data) {
        const vals = { full_name: data.full_name || "", telefono: data.telefono || "", piso: data.piso || "" }
        setPerfil(vals)
        setPerfilOriginal(vals)
      }
    }
    cargar()
  }, [])

  const perfilCambiado = JSON.stringify(perfil) !== JSON.stringify(perfilOriginal)

  const guardarPerfil = async (e) => {
    e.preventDefault()
    setPerfilError("")
    setPerfilOk(false)
    if (!perfil.full_name.trim()) return setPerfilError("El nombre no puede estar vacío.")
    setPerfilLoading(true)
    const { error } = await supabase
      .from("profiles")
      .update({ full_name: perfil.full_name.trim(), telefono: perfil.telefono.trim(), piso: perfil.piso.trim() })
      .eq("id", userId)
    setPerfilLoading(false)
    if (error) return setPerfilError("Error al guardar: " + error.message)
    setPerfilOriginal({ ...perfil })
    setPerfilOk(true)
    setTimeout(() => setPerfilOk(false), 3000)
  }

  // ── Helpers ──────────────────────────────────────────────────────────────
  const Toggle = ({ checked, onChange }) => (
    <button
      type="button"
      className={`aj-toggle${checked ? " aj-toggle--on" : ""}`}
      onClick={() => onChange(!checked)}
      aria-pressed={checked}
    >
      <span className="aj-toggle-knob" />
    </button>
  )

  const EyeBtn = ({ show, onClick }) => (
    <button type="button" className="aj-eye" onClick={onClick} tabIndex={-1}>
      <i className={`fa-regular ${show ? "fa-eye-slash" : "fa-eye"}`} />
    </button>
  )

  return (
    <div className="aj-page">

      {/* ── CABECERA ── */}
      <div className="aj-header">
        <h1 className="aj-title">Ajustes</h1>
        <p className="aj-subtitle">Personaliza tu experiencia en BarrioRed</p>
      </div>

      <div className="aj-grid">

        {/* ══════════════════════════════════════
            BLOQUE 1 — APARIENCIA (solo fuente)
        ══════════════════════════════════════ */}
        <section className="aj-card">
          <div className="aj-card-header">
            <span className="aj-card-icon"><i className="fa-solid fa-paintbrush" /></span>
            <div>
              <p className="aj-card-title">Apariencia</p>
              <p className="aj-card-desc">Ajusta el tamaño del texto en toda la app</p>
            </div>
          </div>

          <div className="aj-field">
            <label className="aj-label">Tamaño de fuente</label>
            <div className="aj-font-group">
              {[
                { val: "pequeño", label: "Pequeño",  sample: "Aa" },
                { val: "normal",  label: "Normal",   sample: "Aa" },
                { val: "grande",  label: "Grande",   sample: "Aa" },
              ].map(opt => (
                <button
                  key={opt.val}
                  type="button"
                  className={`aj-font-btn${fontSize === opt.val ? " aj-font-btn--active" : ""}`}
                  onClick={() => setFontSize(opt.val)}
                >
                  <span className={`aj-font-sample aj-font-sample--${opt.val}`}>{opt.sample}</span>
                  <span className="aj-font-label">{opt.label}</span>
                </button>
              ))}
            </div>
          </div>
        </section>

        {/* ══════════════════════════════════════
            BLOQUE 2 — NOTIFICACIONES
        ══════════════════════════════════════ */}
        <section className="aj-card">
          <div className="aj-card-header">
            <span className="aj-card-icon"><i className="fa-solid fa-bell" /></span>
            <div>
              <p className="aj-card-title">Notificaciones</p>
              <p className="aj-card-desc">Elige qué alertas quieres recibir</p>
            </div>
          </div>

          {[
            {
              key: "br-notif-chat",
              label: "Mensajes de chat",
              desc: "Nuevos mensajes de tus vecinos",
              icon: "fa-regular fa-comments",
              val: notifChat, setter: setNotifChat,
            },
            {
              key: "br-notif-muro",
              label: "Anuncios del muro",
              desc: "Publicaciones nuevas en el tablón",
              icon: "fa-solid fa-thumbtack",
              val: notifMuro, setter: setNotifMuro,
            },
            {
              key: "br-notif-email",
              label: "Notificaciones por email",
              desc: "Resumen de actividad en tu correo",
              icon: "fa-regular fa-envelope",
              val: notifEmail, setter: setNotifEmail,
            },
          ].map(item => (
            <div className="aj-toggle-row" key={item.key}>
              <div className="aj-toggle-icon"><i className={item.icon} /></div>
              <div className="aj-toggle-info">
                <span className="aj-toggle-label">{item.label}</span>
                <span className="aj-toggle-desc">{item.desc}</span>
              </div>
              <Toggle
                checked={item.val}
                onChange={(v) => toggleNotif(item.key, v, item.setter)}
              />
            </div>
          ))}
        </section>

        {/* ══════════════════════════════════════
            BLOQUE 3 — EDITAR PERFIL
        ══════════════════════════════════════ */}
        <section className="aj-card">
          <div className="aj-card-header">
            <span className="aj-card-icon"><i className="fa-regular fa-user" /></span>
            <div>
              <p className="aj-card-title">Datos del perfil</p>
              <p className="aj-card-desc">Actualiza tu nombre, teléfono y piso</p>
            </div>
          </div>

          <form onSubmit={guardarPerfil} noValidate>
            <div className="aj-form-grid">
              <div className="aj-field">
                <label className="aj-label" htmlFor="aj-nombre">Nombre completo</label>
                <input
                  id="aj-nombre"
                  className="aj-input"
                  type="text"
                  placeholder="Tu nombre y apellidos"
                  value={perfil.full_name}
                  onChange={e => setPerfil({ ...perfil, full_name: e.target.value })}
                />
              </div>
              <div className="aj-field">
                <label className="aj-label" htmlFor="aj-tel">Teléfono</label>
                <input
                  id="aj-tel"
                  className="aj-input"
                  type="tel"
                  placeholder="600 000 000"
                  value={perfil.telefono}
                  onChange={e => setPerfil({ ...perfil, telefono: e.target.value })}
                />
              </div>
              <div className="aj-field">
                <label className="aj-label" htmlFor="aj-piso">Piso / Unidad</label>
                <input
                  id="aj-piso"
                  className="aj-input"
                  type="text"
                  placeholder="Ej: 3ºB"
                  value={perfil.piso}
                  onChange={e => setPerfil({ ...perfil, piso: e.target.value })}
                />
              </div>
            </div>

            {perfilError && (
              <div className="aj-alert aj-alert--error">
                <i className="fa-solid fa-triangle-exclamation" />{perfilError}
              </div>
            )}
            {perfilOk && (
              <div className="aj-alert aj-alert--ok">
                <i className="fa-solid fa-circle-check" />Perfil actualizado correctamente.
              </div>
            )}

            <button
              type="submit"
              className="aj-btn aj-btn--primary"
              disabled={perfilLoading || !perfilCambiado}
            >
              {perfilLoading
                ? <><i className="fa-solid fa-spinner fa-spin" /> Guardando…</>
                : <><i className="fa-solid fa-floppy-disk" /> Guardar cambios</>}
            </button>
          </form>
        </section>

        {/* ══════════════════════════════════════
            BLOQUE 4 — CAMBIAR CONTRASEÑA
        ══════════════════════════════════════ */}
        <section className="aj-card">
          <div className="aj-card-header">
            <span className="aj-card-icon"><i className="fa-solid fa-lock" /></span>
            <div>
              <p className="aj-card-title">Cambiar contraseña</p>
              <p className="aj-card-desc">Elige una contraseña segura de al menos 6 caracteres</p>
            </div>
          </div>

          <form onSubmit={cambiarPassword} noValidate>
            <div className="aj-form-grid aj-form-grid--col1">

              <div className="aj-field">
                <label className="aj-label" htmlFor="aj-pnueva">Nueva contraseña</label>
                <div className="aj-input-wrap">
                  <input
                    id="aj-pnueva"
                    className="aj-input"
                    type={showPass.nueva ? "text" : "password"}
                    placeholder="Mínimo 6 caracteres"
                    value={passNueva}
                    onChange={e => setPassNueva(e.target.value)}
                  />
                  <EyeBtn show={showPass.nueva} onClick={() => setShowPass(p => ({ ...p, nueva: !p.nueva }))} />
                </div>
              </div>

              <div className="aj-field">
                <label className="aj-label" htmlFor="aj-pconf">Confirmar contraseña</label>
                <div className="aj-input-wrap">
                  <input
                    id="aj-pconf"
                    className="aj-input"
                    type={showPass.conf ? "text" : "password"}
                    placeholder="Repite la nueva contraseña"
                    value={passConfirmar}
                    onChange={e => setPassConfirmar(e.target.value)}
                  />
                  <EyeBtn show={showPass.conf} onClick={() => setShowPass(p => ({ ...p, conf: !p.conf }))} />
                </div>
              </div>
            </div>

            {passError && (
              <div className="aj-alert aj-alert--error">
                <i className="fa-solid fa-triangle-exclamation" />{passError}
              </div>
            )}
            {passOk && (
              <div className="aj-alert aj-alert--ok">
                <i className="fa-solid fa-circle-check" />Contraseña actualizada correctamente.
              </div>
            )}

            <button
              type="submit"
              className="aj-btn aj-btn--primary"
              disabled={passLoading || !passNueva || !passConfirmar}
            >
              {passLoading
                ? <><i className="fa-solid fa-spinner fa-spin" /> Guardando…</>
                : <><i className="fa-solid fa-key" /> Actualizar contraseña</>}
            </button>
          </form>
        </section>

      </div>
    </div>
  )
}