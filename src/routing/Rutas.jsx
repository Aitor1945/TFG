import React, { useEffect, useState } from "react";
import {
  Routes,
  Route,
  BrowserRouter,
  Navigate,
  Outlet,
} from "react-router-dom";
import { supabase } from "../lib/supabase";

import AppLayout from "../components/layouts/AppLayout";

// Páginas
import Login from "../components/pages/Login/Login";
import Dashboard from "../components/pages/Dashboard/Dashboard";
import Muro from "../components/pages/Muro/Muro";
import Incidencias from "../components/pages/Incidencias/Incidencias";
import Reservas from "../components/pages/Reservas";
import Documentos from "../components/pages/Documentos/Documentos";
import Chat from "../components/pages/Chat/Chat";
import Ajustes from "../components/pages/Ajustes/Ajustes";
import MiPerfil from "../components/pages/MiPerfil/MiPerfil";
import ResetPassword from "../components/pages/ResetPassword/ResetPassword";

//Proteccion de rutas
const Private = () => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session);
      setLoading(false);
    });
  }, []);

  if (loading) return null;

  return session ? <AppLayout /> : <Navigate to="/login" replace />;
};

//rutas publicas, impide volver a login si estas logeado
const PublicRoute = () => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      setSession(data.session);
      setLoading(false);
    });
  }, []);

  if (loading) return null;

  return session ? <Navigate to="/dashboard" replace /> : <Outlet />;
};

export default function Rutas() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Públicas */}
        <Route element={<PublicRoute />}>
          <Route path="/login" element={<Login />} />
          <Route path="/reset-password" element={<ResetPassword />} />
        </Route>

        {/* Protegidas */}
        <Route element={<Private />}>
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
        <Route
          path="*"
          element={
            <div className="d-flex flex-column justify-content-center align-items-center vh-100 vw-100 text-center">
              <h1 className="display-1 fw-bold text-white">404</h1>

              <h4 className="text-white mb-3">Página no encontrada</h4>

              <p className="text-white-50 mb-4">
                La ruta que estás buscando no existe o ha sido movida.
              </p>
            </div>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
