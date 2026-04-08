import React, { useEffect, useState } from "react";
import "./incidencias.css";
import { supabase } from "../../../lib/supabase";

const configuracionPrioridad = {
  alta:  { etiqueta: "Alta",  clase: "prioridad-alta"  },
  media: { etiqueta: "Media", clase: "prioridad-media" },
  baja:  { etiqueta: "Baja",  clase: "prioridad-baja"  },
};

const configuracionEstado = {
  pendiente:  { etiqueta: "Pendiente",  clase: "estado-pendiente" },
  en_proceso: { etiqueta: "En proceso", clase: "estado-proceso"   },
  resuelta:   { etiqueta: "Resuelta",   clase: "estado-resuelta"  },
};



const ROLES_GESTION = ["ayuntamiento", "presidente", "admin"];

export default function Incidencias() {
  const [avisos, setAvisos]           = useState([]);
  const [filtroActual, setFiltroActual] = useState("todas");
  const [rolUsuario, setRolUsuario]   = useState("");
  const [comunidadId, setComunidadId] = useState(null);
  const [idUsuario, setIdUsuario]     = useState(null);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [enviando, setEnviando]       = useState(false);

  const [campos, setCampos] = useState({
    titulo:      "",
    descripcion: "",
    prioridad:   "media",
  });

  // Cargamos el perfil del usuario
  const cargarPerfil = async () => {
    const { data: datosAuth } = await supabase.auth.getUser();
    if (!datosAuth?.user) return;
    setIdUsuario(datosAuth.user.id);

    const { data: perfil } = await supabase
      .from("profiles")
      .select("role, comunidad_id, username")
      .eq("id", datosAuth.user.id)
      .single();

    if (perfil) {
      setRolUsuario(perfil.role);
      setComunidadId(perfil.comunidad_id);
    }
  };

  //Cargamos las incidencias
  const cargarAvisos = async () => {
    const { data, error } = await supabase
      .from("incidencias")
      .select("*")
      .order("created_at", { ascending: false });

    if (error) return console.error("Error al cargar incidencias:", error);

    const idsAutores = data.map((i) => i.autor_id).filter(Boolean);
    const { data: perfiles } = await supabase
      .from("profiles")
      .select("id, username, full_name")
      .in("id", idsAutores);

    setAvisos(
      data.map((i) => ({
        ...i,
        nombre_autor:
          perfiles?.find((p) => p.id === i.autor_id)?.full_name || "Vecino",
        fecha_formateada: new Date(i.created_at).toLocaleDateString("es-ES", {
          day: "2-digit", month: "2-digit", year: "numeric",
        }),
      }))
    );
  };

  // Panel para crear incidencias
  const enviarIncidencia = async () => {
    if (!campos.titulo.trim() || !campos.descripcion.trim()) return;
    if (!comunidadId) return;

    setEnviando(true);
    const { error } = await supabase.from("incidencias").insert([{
      titulo:       campos.titulo,
      descripcion:  campos.descripcion,
      prioridad:    campos.prioridad,
      estado:       "pendiente",
      autor_id:     idUsuario,
      comunidad_id: comunidadId,
    }]);
    setEnviando(false);

    if (error) {
      console.error("Error al crear incidencia:", error);
      return alert("No se pudo crear la incidencia.");
    }

    setCampos({ titulo: "", descripcion: "", prioridad: "media" });
    setMostrarFormulario(false);
    cargarAvisos();
  };

  // Cambiar estado de incidencia
  const actualizarEstado = async (id, nuevoEstado) => {
    const { error } = await supabase
      .from("incidencias")
      .update({ estado: nuevoEstado })
      .eq("id", id);

    if (error) return console.error("Error al actualizar estado:", error);
    cargarAvisos();
  };

  // Eliminar incidencia
  const borrarAviso = async (id) => {
    const { error } = await supabase
      .from("incidencias")
      .delete()
      .eq("id", id);

    if (error) return console.error("Error al eliminar incidencia:", error);
    cargarAvisos();
  };

  useEffect(() => {
    cargarPerfil();
    cargarAvisos();
  }, []);

  const avisosFiltrados =
    filtroActual === "todas"
      ? avisos
      : avisos.filter((i) => i.estado === filtroActual);

  const puedeGestionar = ROLES_GESTION.includes(rolUsuario);

  return (
    <div className="pagina-incidencias">

      <div className="cabecera-seccion">
        <h1 className="titulo-pagina">Incidencias</h1>
        <h2 className="subtitulo-pagina">
          Consulta y seguimiento de incidencias de la comunidad
        </h2>
      </div>

      <div className="fila-controles">
        <div className="barra-filtros">
          {["todas", "pendiente", "en_proceso", "resuelta"].map((f) => (
            <button
              key={f}
              className={`boton-filtro ${filtroActual === f ? "seleccionado" : ""}`}
              onClick={() => setFiltroActual(f)}
            >
              {f === "todas" ? "Todas" : configuracionEstado[f]?.etiqueta}
            </button>
          ))}
        </div>

        {idUsuario && (
          <button
            className="boton-nueva-incidencia"
            onClick={() => setMostrarFormulario((v) => !v)}
          >
            {mostrarFormulario ? "Cancelar" : "+ Nueva incidencia"}
          </button>
        )}
      </div>

      {/* Formulario crear incidencia*/}
      {mostrarFormulario && (
        <div className="formulario-nueva">
          <h5>Nueva incidencia</h5>
          <input
            className="campo-titulo"
            type="text"
            placeholder="Título…"
            value={campos.titulo}
            onChange={(e) => setCampos({ ...campos, titulo: e.target.value })}
          />
          <textarea
            className="campo-descripcion"
            placeholder="Descripción del problema…"
            value={campos.descripcion}
            onChange={(e) => setCampos({ ...campos, descripcion: e.target.value })}
          />
          <select
            className="campo-prioridad"
            value={campos.prioridad}
            onChange={(e) => setCampos({ ...campos, prioridad: e.target.value })}
          >
            <option value="alta">Prioridad: Alta</option>
            <option value="media">Prioridad: Media</option>
            <option value="baja">Prioridad: Baja</option>
          </select>
          <button
            className="boton-enviar"
            onClick={enviarIncidencia}
            disabled={enviando || !campos.titulo.trim() || !campos.descripcion.trim()}
          >
            {enviando ? "Enviando…" : "Enviar incidencia"}
          </button>
        </div>
      )}

      {/* Contador de incidencias*/}
      <p className="texto-contador">
        {avisosFiltrados.length} incidencia
        {avisosFiltrados.length !== 1 ? "s" : ""}
      </p>

      {/* Lista de todas las incidencias*/}
      <div className="lista-avisos">
        {avisosFiltrados.map((incidencia) => (
          <div key={incidencia.id} className="aviso-comunidad">

            <div className="encabezado-aviso">
              <h5>{incidencia.titulo}</h5>
              <div className="grupo-etiquetas">
                <span className={`etiqueta-estado ${configuracionPrioridad[incidencia.prioridad]?.clase}`}>
                  {configuracionPrioridad[incidencia.prioridad]?.etiqueta}
                </span>
                <span className={`etiqueta-estado ${configuracionEstado[incidencia.estado]?.clase}`}>
                  {configuracionEstado[incidencia.estado]?.etiqueta}
                </span>
              </div>
            </div>

            <p>{incidencia.descripcion}</p>

            {puedeGestionar && (
              <div className="zona-acciones">
                {incidencia.estado === "pendiente" && (
                  <button
                    className="boton-gestion gestion-proceso"
                    onClick={() => actualizarEstado(incidencia.id, "en_proceso")}
                  >
                    Marcar en proceso
                  </button>
                )}
                {incidencia.estado !== "resuelta" && (
                  <button
                    className="boton-gestion gestion-resuelta"
                    onClick={() => actualizarEstado(inc.id, "resuelta")}
                  >
                    Marcar como resuelta
                  </button>
                )}
                <button
                  className="boton-gestion gestion-eliminar"
                  onClick={() => borrarAviso(incidencia.id)}
                >
                  Eliminar
                </button>
              </div>
            )}

            <div className="pie-aviso">
              <span>👤 {incidencia.nombre_autor}</span>
              <span>{incidencia.fecha_formateada}</span>
            </div>

          </div>
        ))}

        {avisosFiltrados.length === 0 && (
          <p className="aviso-vacio">No hay incidencias en esta categoría.</p>
        )}
      </div>

    </div>
  );
}