import React, { useState } from "react";
import "./incidencias.css";

const incidenciasData = [
  { id: 1, titulo: "Fuga de agua en escalera B", descripcion: "Se ha detectado una fuga en el segundo rellano de la escalera B. Se ha avisado al fontanero.", estado: "en_proceso", prioridad: "alta", fecha: "19/03/2026", autor: "Vecino 3ºB" },
  { id: 2, titulo: "Ascensor fuera de servicio", descripcion: "El ascensor lleva dos días sin funcionar. Se solicita reparación urgente.", estado: "pendiente", prioridad: "alta", fecha: "18/03/2026", autor: "Vecino 1ºA" },
  { id: 3, titulo: "Iluminación del garaje", descripcion: "Varias luces del garaje están fundidas en la zona de plazas 20-30.", estado: "pendiente", prioridad: "media", fecha: "15/03/2026", autor: "Vecino 2ºC" },
  { id: 4, titulo: "Puerta de entrada forzada", descripcion: "La cerradura de la puerta principal presenta daños. Ya ha sido reparada por el cerrajero.", estado: "resuelta", prioridad: "alta", fecha: "10/03/2026", autor: "Administración" },
  { id: 5, titulo: "Ruidos nocturnos en 4ºD", descripcion: "Se reportan ruidos excesivos durante la madrugada reiteradamente.", estado: "en_proceso", prioridad: "baja", fecha: "14/03/2026", autor: "Vecino 4ºE" },
];

const estadoConfig = {
  pendiente:  { label: "Pendiente",  clase: "estado-pendiente" },
  en_proceso: { label: "En proceso", clase: "estado-proceso" },
  resuelta:   { label: "Resuelta",   clase: "estado-resuelta" },
};
const prioridadConfig = {
  alta:  { label: "Alta",  clase: "prioridad-alta" },
  media: { label: "Media", clase: "prioridad-media" },
  baja:  { label: "Baja",  clase: "prioridad-baja" },
};

export default function Incidencias() {
  const [filtro, setFiltro] = useState("todas");

  const incidenciasFiltradas =
    filtro === "todas"
      ? incidenciasData
      : incidenciasData.filter((i) => i.estado === filtro);

  return (
    <div className="incidencias-page">

      {/* ── BLOQUE SUPERIOR ── */}
      <div className="incidencias-top">

        <h2 className="mb-2 fw-bold incidencias-titulo">Incidencias</h2>
        <p className="incidencias-subtitulo mb-4">Consulta y seguimiento de incidencias de la comunidad</p>

        <div className="filtros-bar mb-3 d-flex gap-2 flex-wrap justify-content-center">
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

        <p className="incidencias-count mb-2">
          {incidenciasFiltradas.length} incidencia{incidenciasFiltradas.length !== 1 ? "s" : ""}
        </p>
      </div>

      {/* ── TARJETAS ── */}
      <div className="incidencias-scroll">
        <div className="row w-100 justify-content-center g-4 px-3 px-md-5 pb-5">
          {incidenciasFiltradas.map((inc) => (
            <div key={inc.id} className="col-12 col-md-8 col-lg-6">
              <div className="card shadow-sm incidencia-card">
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-start mb-2 flex-wrap gap-2">
                    <h5 className="card-title fw-bold mb-0">{inc.titulo}</h5>
                    <div className="d-flex gap-2">
                      <span className={`inc-badge ${prioridadConfig[inc.prioridad].clase}`}>
                        {prioridadConfig[inc.prioridad].label}
                      </span>
                      <span className={`inc-badge ${estadoConfig[inc.estado].clase}`}>
                        {estadoConfig[inc.estado].label}
                      </span>
                    </div>
                  </div>
                  <p className="card-text">{inc.descripcion}</p>
                  <div className="inc-footer d-flex justify-content-between align-items-center mt-3">
                    <span className="inc-autor">
                      <span className="inc-autor-icono">👤</span> {inc.autor}
                    </span>
                    <span className="inc-fecha">{inc.fecha}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

    </div>
  );
}