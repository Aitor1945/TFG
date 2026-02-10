import React from "react"
import { Routes, Route, BrowserRouter, Navigate } from "react-router-dom"

import AppLayout from "../components/layouts/AppLayout"


// Páginas
import Login from "../components/pages/Login"
import Dashboard from "../components/pages/Dashboard"
import Muro from "../components/pages/Muro"
import Incidencias from "../components/pages/Incidencias"
import Reservas from "../components/pages/Reservas"
import Documentos from "../components/pages/Documentos"
import Chat from "../components/pages/Chat"
import Ajustes from "../components/pages/Ajustes"
import MiPerfil from "../components/pages/MiPerfil"

export default function Rutas() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Login SIN layout */}
        <Route path="/login" element={<Login />} />

        {/* Todo lo demás CON layout */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/muro" element={<Muro />} />
          <Route path="/incidencias" element={<Incidencias />} />
          <Route path="/reservas" element={<Reservas />} />
          <Route path="/documentos" element={<Documentos />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/ajustes" element={<Ajustes />} />
          <Route path="/miperfil" element={<MiPerfil />} />
        </Route>

        {/* 404 */}
        <Route path="*" element={<h1>404 - Página no encontrada</h1>} />
      </Routes>
    </BrowserRouter>
  )
}
