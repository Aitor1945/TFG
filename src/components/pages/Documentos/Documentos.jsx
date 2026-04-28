import React, { useEffect, useState } from "react";
import "./documentos.css";
import driveIcon from "../../../assets/drive.png";
import { supabase } from "../../../lib/supabase";

export default function Documentos() {
  const [driveUrl, setDriveUrl] = useState(null);

  useEffect(() => {
    const obtenerDrive = async () => {
      // Guardar usuario logeado
      const { data: userData } = await supabase.auth.getUser();

      if (!userData?.user) {
        console.error("No hay usuario logueado");
        return;
      }

      const userId = userData.user.id;

      // Obtener comunidad del usuario
      const { data: profile, error: profileError } = await supabase
        .from("profiles")
        .select("comunidad_id")
        .eq("id", userId)
        .single();

      if (profileError || !profile?.comunidad_id) {
        console.error(profileError);
        return;
      }

      // Obtener drive_url de la comunidad
      const { data: comunidad, error } = await supabase
        .from("comunidades")
        .select("drive_url")
        .eq("id", profile.comunidad_id)
        .single();

      if (error) {
        console.error(error);
      } else {
        setDriveUrl(comunidad.drive_url);
      }
    };

    obtenerDrive();
  }, []);

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
            Aquí encontrarás actas, normativas y documentos importantes de la
            comunidad.
          </p>
          <a
            href={driveUrl}
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
