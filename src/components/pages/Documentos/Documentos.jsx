import React, { useEffect, useState } from "react";
import "./documentos.css";
import driveIcon from "../../../assets/drive.png";
import { supabase } from "../../../lib/supabase";
import Cargando from "../../../components/Cargando/Cargando";

export default function Documentos() {
  const [driveUrl, setDriveUrl] = useState(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    const obtenerDrive = async () => {
      setCargando(true);

      const { data: userData } = await supabase.auth.getUser();
      if (!userData?.user) {
        console.error("No hay usuario logueado");
        setCargando(false);
        return;
      }

      const { data: profile, error: profileError } = await supabase
        .from("profiles")
        .select("comunidad_id")
        .eq("id", userData.user.id)
        .single();

      if (profileError || !profile?.comunidad_id) {
        console.error(profileError);
        setCargando(false);
        return;
      }

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

      setCargando(false);
    };

    obtenerDrive();
  }, []);

  if (cargando) return <Cargando />;

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
