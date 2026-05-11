import React from "react"
import { Outlet, useLocation } from "react-router-dom"
import SideBar from "./SideBar"
import ChatbotAyuda from "../ChatbotAyuda/ChatbotAyuda"

export default function AppLayout() {
  const location = useLocation()

  // No mostramos el chatbot en /chat para no molestar en móvil
  const mostrarChatbot = location.pathname !== "/chat"

  return (
    <div className="app">
      <SideBar />
      <main className="main">
        <Outlet />
      </main>

      {mostrarChatbot && <ChatbotAyuda />}
    </div>
  )
}