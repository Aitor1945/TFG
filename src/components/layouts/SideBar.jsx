import React, { useState, useEffect } from "react"
import { NavLink } from "react-router-dom"
import "./SideBar.css"

export default function SideBar({ collapsed, setCollapsed }) {
  const [darkMode, setDarkMode] = useState(true)
  const [socialOpen, setSocialOpen] = useState(false)

  useEffect(() => {
    if (darkMode) {
      document.body.classList.remove("light-mode")
    } else {
      document.body.classList.add("light-mode")
    }
  }, [darkMode])

  return (
    <nav className={`sidebar ${collapsed ? "collapsed" : ""}`}>
      <div className="brand-wrapper">
        <div className="brand">
          <i className="fa-solid fa-city"></i>
          <span>BarrioRed</span>
        </div>
        <button
          type="button"
          className="sidebar-toggle-btn"
          aria-label="Colapsar menú"
          onClick={() => setCollapsed(!collapsed)}
        >
          <i className="fa-solid fa-bars"></i>
        </button>
      </div>

      <ul className="nav-list">
        <li>
          <NavLink to="/dashboard" className="nav-link">
            <i className="fa-solid fa-chart-line"></i><span>Dashboard</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/muro" className="nav-link">
            <i className="fa-solid fa-newspaper"></i><span>Muro / Tablón</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/incidencias" className="nav-link">
            <i className="fa-solid fa-triangle-exclamation"></i><span>Incidencias</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/reservas" className="nav-link">
            <i className="fa-regular fa-calendar-check"></i><span>Reservas</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/documentos" className="nav-link">
            <i className="fa-solid fa-folder-open"></i><span>Documentos</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/chat" className="nav-link">
            <i className="fa-regular fa-comments"></i><span>Chat</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/ajustes" className="nav-link">
            <i className="fa-solid fa-gear"></i><span>Ajustes</span>
          </NavLink>
        </li>

        <li className="menu-spacer"></li>

        {/* Redes sociales */}
        <li className={`radial-wrapper ${socialOpen ? "open" : ""}`}>
          <div
            className="nav-link radial-trigger"
            onClick={() => setSocialOpen(!socialOpen)}
          >
            <i className="fa-solid fa-share-nodes"></i>
            <span>Redes</span>
          </div>
          <div className="social-nodes">
            <a href="#" target="_blank" rel="noreferrer" className="social-btn" title="Instagram">
              <i className="fa-brands fa-instagram"></i>
            </a>
            <a href="#" target="_blank" rel="noreferrer" className="social-btn" title="WhatsApp">
              <i className="fa-brands fa-whatsapp"></i>
            </a>
            <a href="#" target="_blank" rel="noreferrer" className="social-btn" title="LinkedIn">
              <i className="fa-brands fa-linkedin-in"></i>
            </a>
            <a href="#" target="_blank" rel="noreferrer" className="social-btn" title="Twitter / X">
              <i className="fa-brands fa-twitter"></i>
            </a>
          </div>
        </li>

        <li>
          <NavLink to="/miperfil" className="nav-link">
            <i className="fa-regular fa-user"></i><span>Mi Perfil</span>
          </NavLink>
        </li>
        <li>
          <NavLink to="/login" className="nav-link" style={{ color: "#ef4444" }}>
            <i className="fa-solid fa-arrow-right-from-bracket"></i><span>Cerrar Sesión</span>
          </NavLink>
        </li>
      </ul>

      {/* Modo claro/oscuro */}
      <div className="theme-switch-wrapper">
        <label className="theme-switch" htmlFor="checkbox-theme">
          <input
            type="checkbox"
            id="checkbox-theme"
            checked={!darkMode}
            onChange={() => setDarkMode(!darkMode)}
          />
          <div className="slider"></div>
        </label>
        <span className="mode-label">{darkMode ? "Modo Claro" : "Modo Oscuro"}</span>
      </div>
    </nav>
  )
}