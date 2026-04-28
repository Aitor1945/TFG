import React from "react"
import { Outlet } from "react-router-dom"
import SideBar from "./SideBar"
import ChatbotAyuda from "../ChatbotAyuda/ChatbotAyuda"   // ← importamos el chatbot

export default function AppLayout() {
  return (
    <div className="app">
      <SideBar />
      <main className="main">
        <Outlet />
      </main>

      {/*
        ChatbotAyuda se renderiza aquí, fuera del <main>,
        para que quede en position: fixed sobre toda la pantalla
        y aparezca en TODAS las páginas protegidas:
        Dashboard, Muro, Incidencias, Documentos, Chat, Ajustes y Mi Perfil.
      */}
      <ChatbotAyuda />
    </div>
  )
}