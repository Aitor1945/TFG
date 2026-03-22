import React, { useState, useRef } from "react";
import "./documentos.css";

export default function Documentos() {
  const [archivos, setArchivos] = useState([]);
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef(null);

  const handleFileChange = (e) => {
    const nuevos = Array.from(e.target.files);
    agregarArchivos(nuevos);
    e.target.value = "";
  };

  const agregarArchivos = (nuevos) => {
    const solosPDF = nuevos.filter((f) => f.type === "application/pdf");
    if (solosPDF.length === 0) return;

    const conMeta = solosPDF.map((f) => ({
      id: Date.now() + Math.random(),
      file: f,
      nombre: f.name,
      tamaño: (f.size / 1024).toFixed(1),
      fecha: new Date().toLocaleDateString("es-ES"),
    }));

    setArchivos((prev) => [...prev, ...conMeta]);
  };

  const eliminar = (id) => {
    setArchivos((prev) => prev.filter((a) => a.id !== id));
  };

  const onDragOver = (e) => { e.preventDefault(); setDragging(true); };
  const onDragLeave = () => setDragging(false);
  const onDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    const dropped = Array.from(e.dataTransfer.files);
    agregarArchivos(dropped);
  };

  return (
    <div className="documentos-page">

      <div className="marquee-container">
        <div className="marquee-content">
          Gestión documental: sube, consulta y descarga los documentos de tu comunidad en formato PDF • Gestión documental: sube, consulta y descarga los documentos de tu comunidad en formato PDF •
        </div>
      </div>

      <h2 className="mb-2 fw-bold documentos-titulo">Documentos</h2>
      <p className="documentos-subtitulo mb-5">Sube y gestiona los documentos PDF de la comunidad</p>

      <div className="upload-area-wrapper mb-5">
        <div
          className={`upload-zone ${dragging ? "dragging" : ""}`}
          onClick={() => inputRef.current.click()}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onDrop={onDrop}
        >
          <input
            ref={inputRef}
            type="file"
            accept="application/pdf"
            multiple
            onChange={handleFileChange}
            style={{ display: "none" }}
          />
          <div className="upload-icon">
            <i className="fa-solid fa-file-pdf"></i>
          </div>
          <p className="upload-text-main">
            {dragging ? "Suelta aquí tu PDF" : "Haz clic o arrastra un PDF aquí"}
          </p>
          <p className="upload-text-sub">Solo se aceptan archivos en formato PDF</p>
          <button
            className="upload-btn"
            type="button"
            onClick={(e) => { e.stopPropagation(); inputRef.current.click(); }}
          >
            <i className="fa-solid fa-upload me-2"></i>
            Seleccionar PDF
          </button>
        </div>
      </div>

      {archivos.length > 0 && (
        <div className="archivos-lista w-100">
          <p className="archivos-count mb-3">
            {archivos.length} documento{archivos.length !== 1 ? "s" : ""} subido{archivos.length !== 1 ? "s" : ""}
          </p>
          <div className="row justify-content-center g-3 w-100 m-0">
            {archivos.map((a) => (
              <div key={a.id} className="col-12 col-md-8 col-lg-6 col-xl-5">
                <div className="archivo-card d-flex align-items-center gap-3 p-3">
                  <div className="pdf-icon-wrapper">
                    <i className="fa-solid fa-file-pdf"></i>
                  </div>
                  <div className="archivo-info flex-grow-1 overflow-hidden">
                    <p className="archivo-nombre text-truncate mb-0">{a.nombre}</p>
                    <p className="archivo-meta mb-0">{a.tamaño} KB · {a.fecha}</p>
                  </div>
                  <div className="archivo-acciones d-flex gap-2 flex-shrink-0">
                    <a
                      href={URL.createObjectURL(a.file)}
                      download={a.nombre}
                      className="accion-btn accion-descargar"
                      title="Descargar"
                    >
                      <i className="fa-solid fa-download"></i>
                    </a>
                    <button
                      className="accion-btn accion-eliminar"
                      title="Eliminar"
                      onClick={() => eliminar(a.id)}
                    >
                      <i className="fa-solid fa-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {archivos.length === 0 && (
        <div className="empty-state text-center mt-2">
          <i className="fa-regular fa-folder-open empty-icon"></i>
          <p className="empty-text mt-3">No hay documentos subidos todavía</p>
        </div>
      )}

    </div>
  );
}