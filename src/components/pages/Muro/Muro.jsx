import React, { useEffect, useState } from "react";
import { supabase } from "../../../lib/supabase";
import "./tablon.css";

const Tablon = () => {
  const [anuncios, setAnuncios] = useState([]);
  const [titulo, setTitulo] = useState("");
  const [contenido, setContenido] = useState("");
  const [rol, setRol] = useState("");
  const [comunidadId, setComunidadId] = useState(null);

  const rolesPermitidos = ["presidente", "admin", "ayuntamiento"];

  // 🔹 Obtener rol y comunidad del usuario
  const getUserProfile = async () => {
    const { data: userData, error: userError } = await supabase.auth.getUser();
    if (!userData?.user) return console.error(userError);

    const { data: profile, error } = await supabase
      .from("profiles")
      .select("role, comunidad_id")
      .eq("id", userData.user.id)
      .single();

    if (!error && profile) {
      setRol(profile.role);
      setComunidadId(profile.comunidad_id);
    } else {
      console.error("Error al obtener perfil:", error);
    }
  };

  // 🔹 Obtener anuncios y unir con nombres de autores
  const fetchAnuncios = async () => {
    const { data: anunciosData, error: errorAnuncios } = await supabase
      .from("muro_publicaciones")
      .select("*")
      .order("created_at", { ascending: false });

    if (errorAnuncios) return console.error("Error al obtener anuncios:", errorAnuncios);
    if (!anunciosData) return;

    // Traer perfiles de los autores
    const autorIds = anunciosData.map(a => a.autor_id);
    const { data: perfiles } = await supabase
      .from("profiles")
      .select("id, role, full_name")
      .in("id", autorIds);

    const anunciosConNombre = anunciosData.map(a => ({
      ...a,
      autor_nombre: perfiles.find(p => p.id === a.autor_id)?.full_name || "Desconocido"
    }));

    setAnuncios(anunciosConNombre);
  };

  // 🔹 Crear anuncio
 const crearAnuncio = async () => {
  const { data: userData } = await supabase.auth.getUser();
  if (!userData?.user) return alert("Usuario no autenticado");
  if (!comunidadId) return alert("No se pudo obtener tu comunidad");

  // ✅ Validar campos obligatorios
  if (!titulo.trim() || !contenido.trim()) {
    return
  }

  const { error } = await supabase.from("muro_publicaciones").insert([
    {
      titulo,
      contenido,
      autor_id: userData.user.id,
      tipo: "anuncio",
      comunidad_id: comunidadId,
    },
  ]);

  if (!error) {
    setTitulo("");
    setContenido("");
    fetchAnuncios();
  } else {
    console.error("Error al crear anuncio:", error);
  }
};

  useEffect(() => {
    fetchAnuncios();
    getUserProfile();
  }, []);

  return (
    <div className="container-fluid tablon-container">
      <h1 className="mb-2 fw-bold ">Muro Comunitario 📌</h1>
      <h2 className=" fw-bold"> Novedades y anuncios de la comunidad</h2>

      {/* FORMULARIO SOLO PARA ROLES PERMITIDOS */}
      {rol && rolesPermitidos.includes(rol) && (
        <div className="tablon-form">
          <h5>Nuevo mensaje</h5>

          <input
            type="text"
            placeholder="Título"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            required
          />

          <textarea
            placeholder="Escribe el mensaje..."
            value={contenido}
            onChange={(e) => setContenido(e.target.value)}
            required
          />

         <button 
          onClick={crearAnuncio} 
          disabled={!titulo.trim() || !contenido.trim()}
          style={{
          opacity: !titulo.trim() || !contenido.trim() ? 0.6 : 1,
          cursor: !titulo.trim() || !contenido.trim() ? "not-allowed" : "pointer",
  }}
>
  Publicar
</button>
        </div>
      )}

      {/* LISTADO DE ANUNCIOS */}
      <div className="tablon-grid">
        {anuncios.map((anuncio) => (
          <div key={anuncio.id} className="tablon-card">
            <h5>{anuncio.titulo}</h5>
            <p>{anuncio.contenido}</p>
            <div className="tablon-card-header">
              <span className="tablon-autor">👤 {anuncio.autor_nombre}</span>
              <span className="tablon-fecha">{new Date(anuncio.created_at).toLocaleDateString()}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Tablon;