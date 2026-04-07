import React, { useEffect, useState } from "react";
import "./incidencias.css";
import { supabase } from "../../../lib/supabase";

const estadoConfig = {
  pendiente:  { label: "Pendiente",  clase: "estado-pendiente" },
  en_proceso: { label: "En proceso", clase: "estado-proceso" },
  resuelta:   { label: "Resuelta",   clase: "estado-resuelta" },
};

const prioridadConfig = {
  alta:  { label: "Alta",  clase: "prioridad-alta"  },
  media: { label: "Media", clase: "prioridad-media" },
  baja:  { label: "Baja",  clase: "prioridad-baja"  },
};

const ROLES_GESTION = ["ayuntamiento", "presidente", "admin"];

export default function Incidencias() {
  const [incidencias, setIncidencias] = useState([]);
  const [filtro, setFiltro] = useState("todas");
  const [rol, setRol] = useState("");
  const [comunidadId, setComunidadId] = useState(null);
  const [userId, setUserId] = useState(null);
  const [username, setUsername] = useState("");
  const [mostrarForm, setMostrarForm] = useState(false);
  const [cargando, setCargando] = useState(false);

  const [form, setForm] = useState({
    titulo: "",
    descripcion: "",
    prioridad: "media",
  });

  // ── Perfil del usuario
  const getUserProfile = async () => {
    const { data: userData } = await supabase.auth.getUser();
    if (!userData?.user) return;
    setUserId(userData.user.id);

    const { data: profile } = await supabase
      .from("profiles")
      .select("role, comunidad_id, username")
      .eq("id", userData.user.id)
      .single();

    if (profile) {
      setRol(profile.role);
      setComunidadId(profile.comunidad_id);
      setUsername(profile.username || "Vecino");
    }
  };

  // ── Fetch incidencias
  const fetchIncidencias = async () => {
    const { data, error } = await supabase
      .from("incidencias")
      .select("*")
      .order("created_at", { ascending: false });

    if (error) return console.error("Error al cargar incidencias:", error);

    const autorIds = data.map((i) => i.autor_id).filter(Boolean);
    const { data: perfiles } = await supabase
      .from("profiles")
      .select("id, username, full_name")
      .in("id", autorIds);

    setIncidencias(
      data.map((i) => ({
        ...i,
        autor_nombre:
          perfiles?.find((p) => p.id === i.autor_id)?.full_name || "Vecino",
        fecha: new Date(i.created_at).toLocaleDateString("es-ES", {
          day: "2-digit", month: "2-digit", year: "numeric",
        }),
      }))
    );
  };

  // ── Crear incidencia
  const crearIncidencia = async () => {
    if (!form.titulo.trim() || !form.descripcion.trim()) return;
    if (!comunidadId) return;

    setCargando(true);
    const { error } = await supabase.from("incidencias").insert([{
      titulo:       form.titulo,
      descripcion:  form.descripcion,
      prioridad:    form.prioridad,
      estado:       "pendiente",
      autor_id:     userId,
      comunidad_id: comunidadId,
    }]);
    setCargando(false);

    if (error) {
      console.error("Error al crear incidencia:", error);
      return alert("No se pudo crear la incidencia.");
    }

    setForm({ titulo: "", descripcion: "", prioridad: "media" });
    setMostrarForm(false);
    fetchIncidencias();
  };

  // ── Cambiar estado
  const cambiarEstado = async (id, nuevoEstado) => {
    const { error } = await supabase
      .from("incidencias")
      .update({ estado: nuevoEstado })
      .eq("id", id);

    if (error) return console.error("Error al actualizar estado:", error);
    fetchIncidencias();
  };

  // ── Eliminar incidencia
  const eliminarIncidencia = async (id) => {
    const { error } = await supabase
      .from("incidencias")
      .delete()
      .eq("id", id);

    if (error) return console.error("Error al eliminar incidencia:", error);
    fetchIncidencias();
  };

  useEffect(() => {
    getUserProfile();
    fetchIncidencias();
  }, []);

  const incidenciasFiltradas =
    filtro === "todas"
      ? incidencias
      : incidencias.filter((i) => i.estado === filtro);

  const puedeGestionar = ROLES_GESTION.includes(rol);

  return (
    <div className="incidencias-page">

      {/* ── BLOQUE SUPERIOR ── */}
      <div className="incidencias-top">
        <h2 className="incidencias-titulo">Incidencias</h2>
        <p className="incidencias-subtitulo">
          Consulta y seguimiento de incidencias de la comunidad
        </p>

        {/* Botón nueva incidencia */}
        {userId && (
          <button
            className="inc-nueva-btn"
            onClick={() => setMostrarForm((v) => !v)}
          >
            {mostrarForm ? "Cancelar" : "+ Nueva incidencia"}
          </button>
        )}

        {/* Formulario */}
        {mostrarForm && (
          <div className="inc-form">
            <span className="inc-form-label">Nueva incidencia</span>
            <input
              className="inc-input"
              type="text"
              placeholder="Título…"
              value={form.titulo}
              onChange={(e) => setForm({ ...form, titulo: e.target.value })}
            />
            <textarea
              className="inc-input inc-textarea"
              placeholder="Descripción del problema…"
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
            />
            <select
              className="inc-input"
              value={form.prioridad}
              onChange={(e) => setForm({ ...form, prioridad: e.target.value })}
            >
              <option value="alta">Prioridad: Alta</option>
              <option value="media">Prioridad: Media</option>
              <option value="baja">Prioridad: Baja</option>
            </select>
            <button
              className="inc-submit-btn"
              onClick={crearIncidencia}
              disabled={cargando || !form.titulo.trim() || !form.descripcion.trim()}
            >
              {cargando ? "Enviando…" : "Enviar incidencia"}
            </button>
          </div>
        )}

        {/* Filtros */}
        <div className="filtros-bar">
          {["todas", "pendiente", "en_proceso", "resuelta"].map((f) => (
            <button
              key={f}
              className={`filtro-btn ${filtro === f ? "filtro-activo" : ""}`}
              onClick={() => setFiltro(f)}
            >
              {f === "todas" ? "Todas" : estadoConfig[f]?.label}
            </button>
          ))}
        </div>

        <p className="incidencias-count">
          {incidenciasFiltradas.length} incidencia
          {incidenciasFiltradas.length !== 1 ? "s" : ""}
        </p>
      </div>

      {/* ── TARJETAS ── */}
      <div className="incidencias-scroll">
        <div className="inc-columna pb-5">
          {incidenciasFiltradas.map((inc) => (
            <div key={inc.id} className="incidencia-card">
              <div className="card-body">
                <div className="inc-card-head">
                  <h5 className="card-title">{inc.titulo}</h5>
                  <div className="inc-badges">
                    <span className={`inc-badge ${prioridadConfig[inc.prioridad]?.clase}`}>
                      {prioridadConfig[inc.prioridad]?.label}
                    </span>
                    <span className={`inc-badge ${estadoConfig[inc.estado]?.clase}`}>
                      {estadoConfig[inc.estado]?.label}
                    </span>
                  </div>
                </div>

                <p className="card-text">{inc.descripcion}</p>

                {/* Acciones de gestión */}
                {puedeGestionar && (
                  <div className="inc-acciones">
                    {inc.estado === "pendiente" && (
                      <button
                        className="inc-accion-btn proceso"
                        onClick={() => cambiarEstado(inc.id, "en_proceso")}
                      >
                        Marcar en proceso
                      </button>
                    )}
                    {inc.estado !== "resuelta" && (
                      <button
                        className="inc-accion-btn resuelta"
                        onClick={() => cambiarEstado(inc.id, "resuelta")}
                      >
                        Marcar como resuelta
                      </button>
                    )}
                    <button
                      className="inc-accion-btn resuelta"
                      style={{ backgroundColor: "#ef4444", color: "#fff", border: "1px solid #ef4444" }}
                      onClick={() => eliminarIncidencia(inc.id)}
                    >
                      Eliminar
                    </button>
                  </div>
                )}

                <div className="inc-footer">
                  <span className="inc-autor">👤 {inc.autor_nombre}</span>
                  <span className="inc-fecha">{inc.fecha}</span>
                </div>
              </div>
            </div>
          ))}

          {incidenciasFiltradas.length === 0 && (
            <p className="inc-empty">No hay incidencias en esta categoría.</p>
          )}
        </div>
      </div>
    </div>
  );
}