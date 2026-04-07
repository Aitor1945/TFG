import React from "react";
import "./documentos.css";
import driveIcon from "../../../assets/drive.png"

export default function Documentos() {

  const DRIVE_URL = "https://drive.google.com/drive/folders/1rw1bsjmlof-3x8X9KFj5SZ6fpFMUU-Tt?usp=sharing";

  return (
    <div className="documentos-page">

      <h2 className="documentos-titulo">Documentos</h2>
      <p className="documentos-subtitulo">
        Accede a los documentos compartidos de la comunidad
      </p>

      <div className="drive-container">
        
        <div className="drive-card">
          
          <div className="drive-icon">
  <img src={driveIcon} alt="Google Drive" className="drive-img" />
</div>

          <h3>Carpeta compartida</h3>
          
          <p>
            Aquí encontrarás actas, normativas y documentos importantes de la comunidad.
          </p>

          <a
            href={DRIVE_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="drive-btn"
          >
            <i className="fa-solid fa-folder-open me-2"></i>
            Abrir carpeta
          </a>

        </div>

      </div>

    </div>
  );
}