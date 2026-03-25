import React from "react"
import { Routes, Route, BrowserRouter, Navigate } from "react-router-dom"

import AppLayout from "../components/layouts/AppLayout"


// Páginas
import Login from "../components/pages/Login/Login"
import Dashboard from "../components/pages/Dashboard/Dashboard"
import Muro from "../components/pages/Muro/Muro"
import Incidencias from "../components/pages/Incidencias/Incidencias"
import Reservas from "../components/pages/Reservas"
import Documentos from "../components/pages/Documentos/Documentos"
import Chat from "../components/pages/Chat/Chat"
import Ajustes from "../components/pages/Ajustes/Ajustes"
import MiPerfil from "../components/pages/MiPerfil/MiPerfil"
import ResetPassword from "../components/pages/ResetPassword/ResetPassword"

export default function Rutas() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Login SIN layout */}
        <Route path="/login" element={<Login />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Todo lo demás CON layout */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/login" replace />} />
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
