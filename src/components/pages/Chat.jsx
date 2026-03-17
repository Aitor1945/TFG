import React, { useEffect, useRef, useState } from "react";
import "./chat.css";
import { supabase } from "../../lib/supabase";

export default function Chat() {
  const [yo, setYo]                     = useState(null);
  const [vecinos, setVecinos]           = useState([]);
  const [vecinoActivo, setVecinoActivo] = useState(null);
  const [mensajes, setMensajes]         = useState([]);
  const [texto, setTexto]               = useState("");
  const [vistaMovil, setVistaMovil]     = useState("lista");
  const bottomRef                       = useRef(null);
  const canalRef                        = useRef(null);

  // ── 1. Obtener quién soy ──────────────────────────────────
  useEffect(() => {
    supabase.auth.getUser().then(({ data }) => setYo(data.user));
  }, []);

  // ── 2. Cargar vecinos cuando ya sé quién soy ─────────────
  useEffect(() => {
    if (!yo) return;
    supabase
      .from("profiles")
      .select("id, full_name, username, email, role")
      .neq("id", yo.id)
      .then(({ data }) => setVecinos(data || []));
  }, [yo]);

  // ── 3. Al seleccionar vecino: historial + realtime ────────
  useEffect(() => {
    if (!yo || !vecinoActivo) return;

    // Cerrar canal anterior para no acumular suscripciones
    if (canalRef.current) {
      supabase.removeChannel(canalRef.current);
      canalRef.current = null;
    }

    // Limpiar mensajes de la conversación anterior
    setMensajes([]);

    // Cargar historial de esta conversación
    supabase
      .from("messages")
      .select("*")
      .or(
        `and(sender_id.eq.${yo.id},receiver_id.eq.${vecinoActivo.id}),` +
        `and(sender_id.eq.${vecinoActivo.id},receiver_id.eq.${yo.id})`
      )
      .order("created_at", { ascending: true })
      .then(({ data }) => setMensajes(data || []));

    // Abrir canal realtime para mensajes nuevos
    const canal = supabase
      .channel(`chat-${[yo.id, vecinoActivo.id].sort().join("-")}`)
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: "messages" },
        (payload) => {
          const msg = payload.new;

          // Solo mensajes de esta conversación
          const relevante =
            (msg.sender_id === yo.id         && msg.receiver_id === vecinoActivo.id) ||
            (msg.sender_id === vecinoActivo.id && msg.receiver_id === yo.id);

          if (!relevante) return;

          // Evitar duplicados comprobando el id
          setMensajes((prev) => {
            if (prev.some((m) => m.id === msg.id)) return prev;
            return [...prev, msg];
          });
        }
      )
      .subscribe();

    canalRef.current = canal;

    // Al cambiar de vecino o desmontar: cerrar canal
    return () => {
      supabase.removeChannel(canal);
      canalRef.current = null;
    };
  }, [yo, vecinoActivo]);

  // ── 4. Auto-scroll al último mensaje ─────────────────────
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [mensajes]);

  // ── 5. Enviar mensaje ─────────────────────────────────────
  // No añadimos el mensaje al estado aquí — el realtime lo hará solo
  const enviar = async (e) => {
    e.preventDefault();
    if (!texto.trim() || !vecinoActivo || !yo) return;
    const contenido = texto.trim();
    setTexto("");
    await supabase.from("messages").insert({
      sender_id:   yo.id,
      receiver_id: vecinoActivo.id,
      content:     contenido,
    });
  };

  // ── Helpers ───────────────────────────────────────────────
  const nombre = (u) =>
    u?.full_name || u?.username || u?.email?.split("@")[0] || "Usuario";

  const hora = (ts) =>
    new Date(ts).toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit" });

  // ── Render ────────────────────────────────────────────────
  return (
    <div className="bc-wrapper">

      {/* ── LISTA DE VECINOS ── */}
      <div className={`bc-panel${vistaMovil === "chat" ? " oculto" : ""}`}>
        <div className="bc-panel-header">
          <h2 className="bc-panel-title">BarrioChat</h2>
          <span className="bc-panel-count">{vecinos.length}</span>
        </div>
        <ul className="bc-user-list">
          {vecinos.length === 0 && (
            <li className="bc-empty-panel">No hay vecinos disponibles</li>
          )}
          {vecinos.map((v) => (
            <li
              key={v.id}
              className={`bc-user-item${vecinoActivo?.id === v.id ? " activo" : ""}`}
              onClick={() => { setVecinoActivo(v); setVistaMovil("chat"); }}
            >
              <div className="bc-avatar">{nombre(v)[0].toUpperCase()}</div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="bc-user-name">{nombre(v)}</div>
                <div className="bc-user-rol">{v.role || "Vecino"}</div>
              </div>
            </li>
          ))}
        </ul>
      </div>

      {/* ── ÁREA DE MENSAJES ── */}
      <div
        className={`bc-chat${vistaMovil === "lista" ? " oculto" : ""}`}
        style={{ display: window.innerWidth > 640 ? "flex" : undefined }}
      >
        <div className="bc-chat-box">
          {vecinoActivo ? (
            <>
              {/* Header */}
              <div className="bc-chat-header">
                <button className="bc-back-btn" onClick={() => setVistaMovil("lista")}>←</button>
                <div className="bc-avatar" style={{ background: "#eef2ff", color: "#2f6bff" }}>
                  {nombre(vecinoActivo)[0].toUpperCase()}
                </div>
                <div>
                  <div className="bc-chat-header-name">{nombre(vecinoActivo)}</div>
                  <div className="bc-chat-header-rol">{vecinoActivo.role || "Vecino"}</div>
                </div>
              </div>

              {/* Mensajes */}
              <ul className="bc-messages">
                {mensajes.length === 0 && (
                  <li className="bc-hello">Di hola a {nombre(vecinoActivo)} 👋</li>
                )}
                {mensajes.map((msg) => {
                  const esMio = msg.sender_id === yo?.id;
                  return (
                    <li key={msg.id} className={`bc-msg-row${esMio ? " mio" : " otro"}`}>
                      <div className={`bc-bubble${esMio ? " mio" : " otro"}`}>
                        {!esMio && (
                          <div className="bc-bubble-remitente">{nombre(vecinoActivo)}</div>
                        )}
                        {msg.content}
                        <div className="bc-bubble-hora">{hora(msg.created_at)}</div>
                      </div>
                    </li>
                  );
                })}
                <li ref={bottomRef} style={{ listStyle: "none" }} />
              </ul>

              {/* Input */}
              <form className="bc-form" onSubmit={enviar}>
                <div className="bc-input-row">
                  <input
                    className="bc-input"
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                    placeholder="Escribe tu mensaje..."
                    autoComplete="off"
                  />
                  <button type="submit" className="bc-send-btn" disabled={!texto.trim()}>
                    Enviar
                  </button>
                </div>
              </form>
            </>
          ) : (
            <div className="bc-empty-chat">
      <div className="bc-empty-icon">💬</div>
       <p>Selecciona un vecino</p>
        <span>para empezar a chatear</span>
      </div>
          )}
        </div>
      </div>

    </div>
  );
}