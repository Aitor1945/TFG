import React from "react";
import "./tablon.css";

const anuncios = [
  { id: 1, titulo: "Mantenimiento programado", descripcion: "El servidor estará en mantenimiento el sábado de 10:00 a 12:00.", date:"17/02/2026" },
  { id: 2, titulo: "Nueva funcionalidad", descripcion: "Se ha añadido un chat interno para comunicación rápida entre usuarios." , date:"17/02/2026" },
  { id: 3, titulo: "Recordatorio de seguridad", descripcion: "Recuerda cambiar tu contraseña cada 3 meses para mantener la seguridad." , date:"17/02/2026" },
  { id: 4, titulo: "Software en desarrollo", descripcion: "Recordamos que el software sigue estando en desarrollo." , date:"17/02/2026" },
];

const Tablon = () => {
  return (
<div className="container-fluid min-vh-100 d-flex flex-column align-items-center justify-content-start pt-5 tablon-container">
  <h2 className="mb-4 fw-bold text-dark">Muro Comunitario 🧱</h2>
  <div className="row w-100 justify-content-center g-4 px-3 px-md-5">
    {anuncios.map(anuncio => (
      <div key={anuncio.id} className="col-12 col-md-8 col-lg-6">
        <div className="card shadow-sm tablon-card">
          <div className="card-body">
            <h5 className="card-title fw-bold">{anuncio.titulo}</h5>
            <p className="card-text text-muted">{anuncio.descripcion}</p>
            <p className="card-date">{anuncio.date}</p>
          </div>
        </div>
      </div>
    ))}
  </div>
</div>
  );
};

export default Tablon;
