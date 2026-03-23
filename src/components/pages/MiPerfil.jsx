import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase";
import "./miperfil.css";

const rolConfig = {
  admin:        { label: "Administrador" },
  presidente:   { label: "Presidente"   },
  ayuntamiento: { label: "Ayuntamiento" },
  conserje:     { label: "Conserje"     },
  vecino:       { label: "Vecino"       },
};

const formatFecha = (ts) => {
  if (!ts) return "—";
  return new Date(ts).toLocaleDateString("es-ES", {
    day: "2-digit", month: "long", year: "numeric",
  });
};

const iniciales = (fullName, username, email) => {
  if (fullName) {
    const p = fullName.trim().split(" ");
    return p.length >= 2
      ? (p[0][0] + p[1][0]).toUpperCase()
      : p[0].slice(0, 2).toUpperCase();
  }
  if (username) return username.slice(0, 2).toUpperCase();
  if (email)    return email.slice(0, 2).toUpperCase();
  return "??";
};

export default function MiPerfil() {
  const [perfil, setPerfil]       = useState(null);
  const [comunidad, setComunidad] = useState(null);
  const [email, setEmail]         = useState(null);
  const [cargando, setCargando]   = useState(true);
  const [error, setError]         = useState(null);

  useEffect(() => {
    const cargar = async () => {
      setCargando(true);

      const { data: { user }, error: authError } = await supabase.auth.getUser();
      if (authError || !user) { setError("No has iniciado sesión."); setCargando(false); return; }
      setEmail(user.email);

      const { data: perfilData, error: perfilError } = await supabase
        .from("profiles")
        .select("id, role, comunidad_id, username, full_name, created_at")
        .eq("id", user.id)
        .single();

      if (perfilError) { setError("No se pudo cargar el perfil."); setCargando(false); return; }
      setPerfil(perfilData);

      if (perfilData.comunidad_id) {
        const { data: comunidadData } = await supabase
          .from("comunidades")
          .select("nombre, tipo")
          .eq("id", perfilData.comunidad_id)
          .single();
        if (comunidadData) setComunidad(comunidadData);
      }

      setCargando(false);
    };
    cargar();
  }, []);

  if (cargando) return (
    <div className="perfil-page perfil-center">
      <div className="perfil-spinner" />
    </div>
  );

  if (error) return (
    <div className="perfil-page perfil-center">
      <p className="perfil-error">{error}</p>
    </div>
  );

  const rol        = rolConfig[perfil.role] ?? { label: perfil.role };
  const avatarIni  = iniciales(perfil.full_name, perfil.username, email);
  const nombreMostrado = perfil.full_name || perfil.username || "Sin nombre";

  return (
    <div className="perfil-page">


      <div className="perfil-panel-dch">

        <p className="perfil-dch-eyebrow">Mi cuenta</p>
        
        <h1 className="perfil-dch-titulo">Información<br />del perfil</h1>

        {/* Datos personales */}
        <div className="perfil-seccion">
          <p className="perfil-seccion-titulo">Datos personales</p>
          <div className="perfil-fila">
            <span className="perfil-fila-label">Nombre completo</span>
            <span className="perfil-fila-valor">{perfil.full_name || "—"}</span>
          </div>
          <div className="perfil-fila">
            <span className="perfil-fila-label">Usuario</span>
            <span className="perfil-fila-valor">{perfil.username ? `@${perfil.username}` : "—"}</span>
          </div>
          <div className="perfil-fila">
            <span className="perfil-fila-label">Correo electrónico</span>
            <span className="perfil-fila-valor">{email || "—"}</span>
          </div>
        </div>

        {/* Comunidad */}
        {comunidad && (
          <div className="perfil-seccion">
            <p className="perfil-seccion-titulo">Mi comunidad</p>
            <div className="perfil-fila">
              <span className="perfil-fila-label">Nombre</span>
              <span className="perfil-fila-valor">{comunidad.nombre}</span>
            </div>
            <div className="perfil-fila">
              <span className="perfil-fila-label">Tipo</span>
              <span className="perfil-fila-valor">
                <span className="perfil-chip">{comunidad.tipo}</span>
              </span>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}