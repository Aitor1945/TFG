import React from "react"
import { NavLink } from "react-router-dom"

export default function SideBar() {
  return (
    <nav className="sidebar">
      <div className="brand-wrapper">
        <div className="brand">
          <i className="fa-solid fa-city"></i>
          <span>BarrioRed</span>
        </div>

        <button
          type="button"
          className="sidebar-toggle-btn"
          aria-label="Colapsar menú"
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

        <li className="radial-wrapper">
          <div className="nav-link radial-trigger" style={{ cursor: "pointer" }}>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
              <i className="fa-solid fa-share-nodes"></i>
              <span>Redes</span>
            </div>
          </div>

          <div className="social-nodes">
            <a href="#" target="_blank" rel="noreferrer" className="social-btn">
              <i className="fa-brands fa-instagram"></i>
            </a>
            <a href="#" target="_blank" rel="noreferrer" className="social-btn">
              <i className="fa-brands fa-whatsapp"></i>
            </a>
            <a href="#" target="_blank" rel="noreferrer" className="social-btn">
              <i className="fa-brands fa-linkedin-in"></i>
            </a>
            <a href="#" target="_blank" rel="noreferrer" className="social-btn">
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

      <div className="theme-switch-wrapper">
        <label className="theme-switch" htmlFor="checkbox-theme">
          <input type="checkbox" id="checkbox-theme" />
          <div className="slider"></div>
        </label>
        <span className="mode-label">Modo Claro</span>
      </div>
    </nav>
  )
}
