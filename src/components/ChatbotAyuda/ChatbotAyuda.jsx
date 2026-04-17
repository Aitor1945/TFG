import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "./ChatbotAyuda.css";

// ─────────────────────────────────────────────
// RESPUESTAS adaptadas a las secciones de BarrioRed
// ─────────────────────────────────────────────
const RESPUESTAS = {
  empezar:
    "¡Bienvenido a BarrioRed! 🏘️\n\nEn el menú de la izquierda encontrarás todo:\n\n• 📊 Panel → resumen de tu comunidad\n• 📰 Muro → anuncios y noticias\n• ⚠️ Incidencias → reportar problemas\n• 📁 Documentos → actas y normativas\n• 💬 Chat → hablar con vecinos\n\n¿Sobre cuál quieres saber más?",

  muro:
    "El Muro es el tablón de anuncios de tu comunidad. 📰\n\nAhí el presidente o el ayuntamiento publican noticias importantes.\n\nPara verlo, pulsa 'Muro' en el menú de la izquierda.",

  incidencias:
    "Las incidencias sirven para reportar problemas en tu comunidad (averías, desperfectos, etc.). ⚠️\n\nPara crear una:\n1. Pulsa 'Incidencias' en el menú\n2. Haz clic en '+ Nueva incidencia'\n3. Escribe el título y la descripción\n4. Pulsa 'Enviar'\n\nEl administrador recibirá tu aviso.",

  chat:
    "Con el Chat puedes hablar directamente con tus vecinos. 💬\n\nPara usarlo:\n1. Pulsa 'Chat' en el menú de la izquierda\n2. Elige el vecino con quien quieras hablar\n3. Escribe tu mensaje y pulsa Enviar\n\n¡Es como el WhatsApp pero dentro de BarrioRed!",

  documentos:
    "En Documentos encontrarás actas de reuniones, normativas y otros archivos importantes de tu comunidad. 📁\n\nAl pulsar 'Documentos' en el menú se abrirá la carpeta compartida de Google Drive de tu comunidad.",

  perfil:
    "En 'Mi Perfil' puedes ver y editar tus datos personales. 👤\n\nEn 'Ajustes' puedes:\n• Cambiar tu contraseña\n• Actualizar tu nombre, teléfono y piso\n• Elegir el tamaño de letra\n• Cambiar entre modo claro y oscuro",

  contrasena:
    "Para cambiar tu contraseña:\n\n1. Pulsa 'Ajustes' en el menú de la izquierda\n2. Baja hasta la sección 'Cambiar contraseña'\n3. Escribe la nueva contraseña dos veces\n4. Pulsa 'Actualizar contraseña'\n\nDebe tener al menos 6 caracteres.",

  cerrarSesion:
    "Para cerrar sesión:\n\n👉 En el menú de la izquierda, baja hasta el final y pulsa 'Cerrar sesión'.\n\nSi alguien más usa el ordenador, siempre es buena idea cerrar sesión al terminar.",

  ayuda:
    "Estoy aquí para ayudarte con BarrioRed. 😊\n\nPuedes preguntarme sobre:\n• Cómo usar el Muro o el Chat\n• Cómo reportar una incidencia\n• Dónde están los documentos\n• Cómo cambiar tus datos o contraseña\n\n¿Qué necesitas?",

  saludo:
    "¡Hola! 👋 Soy el asistente de BarrioRed.\n\nEstoy aquí para ayudarte a usar la plataforma. Pulsa una de las opciones de abajo o escríbeme tu pregunta.",

  gracias:
    "¡De nada! Para eso estoy. 😊\n\nSi tienes más dudas, no dudes en preguntarme.",

  noEntiendo:
    "Lo siento, no he entendido bien tu pregunta. 😅\n\nPrueba a pulsar uno de los botones de abajo, o pregúntame cosas como:\n• '¿Cómo pongo una incidencia?'\n• '¿Dónde está el chat?'\n• '¿Cómo cambio mi contraseña?'",
};

// ─────────────────────────────────────────────
// PALABRAS CLAVE para detectar la intención
// ─────────────────────────────────────────────
const PALABRAS_CLAVE = {
  saludo:       ["hola", "buenas", "buenos días", "buenas tardes", "hey", "saludos"],
  empezar:      ["empezar", "empiezo", "inicio", "comenzar", "nuevo", "primera vez", "cómo funciona"],
  muro:         ["muro", "anuncio", "anuncios", "tablón", "noticias", "publicación"],
  incidencias:  ["incidencia", "incidencias", "avería", "problema", "reportar", "desperfecto"],
  chat:         ["chat", "mensaje", "mensajes", "hablar", "vecino", "escribir"],
  documentos:   ["documento", "documentos", "acta", "archivo", "normativa", "drive"],
  perfil:       ["perfil", "datos", "nombre", "teléfono", "piso", "ajustes"],
  contrasena:   ["contraseña", "password", "clave", "cambiar contraseña"],
  cerrarSesion: ["cerrar sesión", "salir", "logout", "desconectar"],
  gracias:      ["gracias", "muchas gracias", "perfecto", "genial", "ok gracias"],
};

// Botones rápidos que se muestran en el chat
const BOTONES_RAPIDOS = [
  { codigo: "empezar",     icono: "🏘️", texto: "¿Cómo empezar?" },
  { codigo: "incidencias", icono: "⚠️", texto: "Incidencias"     },
  { codigo: "muro",        icono: "📰", texto: "El Muro"         },
  { codigo: "chat",        icono: "💬", texto: "El Chat"         },
  { codigo: "contrasena",  icono: "🔑", texto: "Contraseña"      },
  { codigo: "documentos",  icono: "📁", texto: "Documentos"      },
];

// ─────────────────────────────────────────────
// HELPER: hora actual en formato HH:MM
// ─────────────────────────────────────────────
function horaActual() {
  const d = new Date();
  const h = String(d.getHours()).padStart(2, "0");
  const m = String(d.getMinutes()).padStart(2, "0");
  return `${h}:${m}`;
}

// ─────────────────────────────────────────────
// HELPER: busca respuesta por palabras clave
// ─────────────────────────────────────────────
function buscarRespuesta(texto) {
  const lower = texto.toLowerCase();
  for (const categoria in PALABRAS_CLAVE) {
    const palabras = PALABRAS_CLAVE[categoria];
    for (const palabra of palabras) {
      if (lower.includes(palabra)) {
        return RESPUESTAS[categoria];
      }
    }
  }
  return RESPUESTAS.noEntiendo;
}

// ─────────────────────────────────────────────
// COMPONENTE PRINCIPAL
// ─────────────────────────────────────────────
export default function ChatbotAyuda() {
  const navigate = useNavigate();

  // Estado: abierto/cerrado, lista de mensajes, texto escrito, badge visible
  const [abierto,       setAbierto]       = useState(false);
  const [mensajes,      setMensajes]      = useState([]);
  const [texto,         setTexto]         = useState("");
  const [escribiendo,   setEscribiendo]   = useState(false);
  const [badgeVisible,  setBadgeVisible]  = useState(true);
  const [bienvenidaMostrada, setBienvenidaMostrada] = useState(false);

  // Ref al final de la zona de mensajes para hacer scroll automático
  const refFinal = useRef(null);

  // Cuando cambia la lista de mensajes, hacemos scroll al último
  useEffect(() => {
    refFinal.current?.scrollIntoView({ behavior: "smooth" });
  }, [mensajes, escribiendo]);

  // ── Abrir el chat ──────────────────────────
  function abrirChat() {
    setAbierto(true);
    setBadgeVisible(false);

    // Solo mostramos el saludo la primera vez
    if (!bienvenidaMostrada) {
      setBienvenidaMostrada(true);
      setTimeout(() => {
        agregarMensaje(
          "¡Hola! 👋 Soy el asistente de BarrioRed.\n\nEstoy aquí para ayudarte a usar la plataforma. Pulsa una opción o escríbeme tu pregunta.",
          "asistente"
        );
      }, 400);
    }
  }

  // ── Cerrar el chat ─────────────────────────
  function cerrarChat() {
    setAbierto(false);
  }

  // ── Añadir mensaje a la lista ──────────────
  function agregarMensaje(contenido, tipo) {
    const nuevoMensaje = {
      id:       Date.now() + Math.random(), // id único
      contenido,
      tipo,                                 // "asistente" | "usuario"
      hora:     horaActual(),
    };
    setMensajes(prev => [...prev, nuevoMensaje]);
  }

  // ── Responder con delay (simula que piensa) ─
  function responderConDelay(respuesta) {
    setEscribiendo(true);
    setTimeout(() => {
      setEscribiendo(false);
      agregarMensaje(respuesta, "asistente");
    }, 950);
  }

  // ── Pulsar un botón rápido ─────────────────
  function pulsarBoton(codigo) {
    // Texto que verá el usuario como su "mensaje"
    const boton = BOTONES_RAPIDOS.find(b => b.codigo === codigo);
    if (boton) {
      agregarMensaje(`${boton.icono} ${boton.texto}`, "usuario");
    }
    responderConDelay(RESPUESTAS[codigo] || RESPUESTAS.noEntiendo);
  }

  // ── Enviar mensaje de texto libre ──────────
  function enviarMensaje() {
    const textoLimpio = texto.trim();
    if (!textoLimpio) return;

    agregarMensaje(textoLimpio, "usuario");
    setTexto("");

    const respuesta = buscarRespuesta(textoLimpio);
    responderConDelay(respuesta);
  }

  // ── Enter para enviar ──────────────────────
  function manejarTecla(e) {
    if (e.key === "Enter") enviarMensaje();
  }

  // ─────────────────────────────────────────────
  // RENDER
  // ─────────────────────────────────────────────
  return (
    <>
      {/* ── Ventana del chat ── */}
      <div className={`cb-contenedor${abierto ? "" : " cb-oculto"}`}>

        {/* Cabecera azul */}
        <div className="cb-cabecera">
          <div className="cb-cabecera-info">
            <span className="cb-icono-asistente">🤝</span>
            <div>
              <p className="cb-cabecera-titulo">Asistente BarrioRed</p>
              <p className="cb-cabecera-sub">Estoy aquí para ayudarte</p>
            </div>
          </div>
          <button className="cb-btn-cerrar" onClick={cerrarChat} title="Cerrar">✕</button>
        </div>

        {/* Zona de mensajes */}
        <div className="cb-mensajes">
          {mensajes.map(msg => (
            <div key={msg.id} className={`cb-msg cb-msg--${msg.tipo}`}>
              <div className="cb-burbuja">
                {/* Reemplazamos \n por saltos de línea */}
                {msg.contenido.split("\n").map((linea, i) => (
                  <React.Fragment key={i}>
                    {linea}
                    {i < msg.contenido.split("\n").length - 1 && <br />}
                  </React.Fragment>
                ))}
              </div>
              <span className="cb-hora">{msg.hora}</span>
            </div>
          ))}

          {/* Indicador de "escribiendo..." */}
          {escribiendo && (
            <div className="cb-msg cb-msg--asistente">
              <div className="cb-burbuja cb-escribiendo">
                <span className="cb-punto" />
                <span className="cb-punto" />
                <span className="cb-punto" />
              </div>
            </div>
          )}

          {/* Ancla invisible para el scroll automático */}
          <div ref={refFinal} />
        </div>

        {/* Botones rápidos */}
        <div className="cb-botones">
          <p className="cb-botones-label">Elige una opción o escribe:</p>
          <div className="cb-botones-grid">
            {BOTONES_RAPIDOS.map(boton => (
              <button
                key={boton.codigo}
                className="cb-btn-rapido"
                onClick={() => pulsarBoton(boton.codigo)}
              >
                {boton.icono} {boton.texto}
              </button>
            ))}
          </div>
        </div>

        {/* Campo de escritura */}
        <div className="cb-escritura">
          <input
            type="text"
            className="cb-input"
            placeholder="Escribe tu pregunta..."
            value={texto}
            onChange={e => setTexto(e.target.value)}
            onKeyDown={manejarTecla}
            maxLength={200}
          />
          <button className="cb-btn-enviar" onClick={enviarMensaje}>
            Enviar
          </button>
        </div>
      </div>

      {/* ── Botón flotante para abrir ── */}
      <button
        className="cb-btn-abrir"
        onClick={abierto ? cerrarChat : abrirChat}
        title="Abrir asistente de ayuda"
      >
        {abierto ? "✕" : "👤"}
        {badgeVisible && !abierto && (
          <span className="cb-badge">1</span>
        )}
      </button>
    </>
  );
}