import React, { useState, useEffect } from "react"
import { NavLink, useNavigate } from "react-router-dom"
import { supabase } from "../../lib/supabase"
import "./SideBar.css"

export default function SideBar() {
  const [modoOscuro, setModoOscuro] = useState(true)
  const [redesAbiertas, setRedesAbiertas] = useState(false)
  const [sidebarFijado, setSidebarFijado] = useState(false)

  const navigate = useNavigate()

  // Cambiar a tema oscuro / claro
  useEffect(() => {
    if (modoOscuro) {
      document.body.classList.remove("light-mode")
    } else {
      document.body.classList.add("light-mode")
    }
  }, [modoOscuro])

  // Sidebar fijado
  useEffect(() => {
    const app = document.querySelector(".app")
    if (app) {
      app.classList.toggle("sidebar-pinned", sidebarFijado)
    }
  }, [sidebarFijado])

  // Funcion para cerrar sesión
  const cerrarSesion = async () => {
    await supabase.auth.signOut()

    document.body.classList.remove("light-mode")

    setRedesAbiertas(false)
    setSidebarFijado(false)

    navigate("/login")
  }

  return (
    <nav className={`sidebar${sidebarFijado ? " pinned" : ""}`}>

      <div className="brand-wrapper">
        <div className="brand">
          <i className="fa-solid fa-city"></i>
          <span>BarrioRed</span>
        </div>
      </div>

      {/* menu*/}
      <ul className="nav-list">

        <li>
          <NavLink to="/dashboard" className="nav-link">
            <i className="fa-solid fa-chart-line"></i>
            <span>Panel</span>
          </NavLink>
        </li>

        <li>
          <NavLink to="/muro" className="nav-link">
            <i className="fa-solid fa-newspaper"></i>
            <span>Muro</span>
          </NavLink>
        </li>

        <li>
          <NavLink to="/incidencias" className="nav-link">
            <i className="fa-solid fa-triangle-exclamation"></i>
            <span>Incidencias</span>
          </NavLink>
        </li>

        <li>
          <NavLink to="/reservas" className="nav-link">
            <i className="fa-regular fa-calendar-check"></i>
            <span>Reservas</span>
          </NavLink>
        </li>

        <li>
          <NavLink to="/documentos" className="nav-link">
            <i className="fa-solid fa-folder-open"></i>
            <span>Documentos</span>
          </NavLink>
        </li>

        <li>
          <NavLink to="/chat" className="nav-link">
            <i className="fa-regular fa-comments"></i>
            <span>Chat</span>
          </NavLink>
        </li>

        <li>
          <NavLink to="/ajustes" className="nav-link">
            <i className="fa-solid fa-gear"></i>
            <span>Ajustes</span>
          </NavLink>
        </li>

        <li className="menu-spacer"></li>

        {/* redes sociales*/}
        <li className={`radial-wrapper ${redesAbiertas ? "open" : ""}`}>
          <div
            className="nav-link radial-trigger"
            onClick={() => setRedesAbiertas(!redesAbiertas)}
          >
            <i className="fa-solid fa-share-nodes"></i>
            <span>Redes</span>
          </div>

          <div className="social-nodes">
            <a href="#" className="social-btn">
              <i className="fa-brands fa-instagram"></i>
            </a>
            <a href="#" className="social-btn">
              <i className="fa-brands fa-whatsapp"></i>
            </a>
            <a href="#" className="social-btn">
              <i className="fa-brands fa-linkedin-in"></i>
            </a>
            <a href="#" className="social-btn">
              <i className="fa-brands fa-twitter"></i>
            </a>
          </div>
        </li>

        <li>
          <NavLink to="/miperfil" className="nav-link">
            <i className="fa-regular fa-user"></i>
            <span>Mi perfil</span>
          </NavLink>
        </li>

        {/* cerrar sesion */}
        <li>
          <button className="nav-link logout-link" onClick={cerrarSesion}>
            <i className="fa-solid fa-arrow-right-from-bracket"></i>
            <span>Cerrar sesión</span>
          </button>
        </li>
      </ul>

      {/* footer*/}
      <div className="sidebar-footer">

        <label className="theme-switch">
          <input
            type="checkbox"
            checked={!modoOscuro}
            onChange={() => setModoOscuro(!modoOscuro)}
          />
          <div className="slider"></div>
        </label>

        <span className="mode-label">
          {modoOscuro ? "Modo oscuro" : "Modo claro"}
        </span>

        <button
          className={`pin-btn${sidebarFijado ? " pin-btn--active" : ""}`}
          onClick={() => setSidebarFijado(!sidebarFijado)}
          title={sidebarFijado ? "Fijar abierto" : "Dejar fijo"}
        >
          <i className="fa-solid fa-thumbtack"></i>
        </button>
      </div>

    </nav>
  )
}