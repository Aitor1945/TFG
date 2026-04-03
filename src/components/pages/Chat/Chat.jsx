import React, { useEffect, useRef, useState } from "react";
import "./chat.css";
import { supabase } from "../../../lib/supabase";

export default function Chat() {

  const [usuarioActual, setUsuarioActual] = useState(null)
  const [listaVecinos, setListaVecinos] = useState([])
  const [vecinoSeleccionado, setVecinoSeleccionado] = useState(null)
  const [listaMensajes, setListaMensajes] = useState([])
  const [mensajeEscrito, setMensajeEscrito] = useState("")
  const [pantallaMovil, setPantallaMovil] = useState("lista")
  // guardo el ultimo mensaje y los no leidos de cada vecino
  const [datosConversacion, setDatosConversacion] = useState({})

  const refFinal = useRef(null)
  const refCanal = useRef(null)
  // necesito una ref del vecino activo para usarla dentro del canal de supabase
  const refVecinoActivo = useRef(null)

  // sincronizo la ref con el estado cada vez que cambia el vecino
  useEffect(() => {
    refVecinoActivo.current = vecinoSeleccionado

    // si abro una conversacion pongo los no leidos a 0
    if (vecinoSeleccionado) {
      setDatosConversacion((prev) => {
        if ((prev[vecinoSeleccionado.id]?.noLeidos || 0) === 0) return prev
        return {
          ...prev,
          [vecinoSeleccionado.id]: { ...prev[vecinoSeleccionado.id], noLeidos: 0 },
        }
      })
    }
  }, [vecinoSeleccionado])

  // cojo el usuario que esta logueado
  useEffect(() => {
    supabase.auth.getUser().then(({ data }) => setUsuarioActual(data.user))
  }, [])

  // cuando tengo el usuario cargo los vecinos
  useEffect(() => {
    if (!usuarioActual) return
    supabase
      .from("profiles")
      .select("id, full_name, username, email, role")
      .neq("id", usuarioActual.id)
      .then(({ data }) => setListaVecinos(data || []))
  }, [usuarioActual])

  // cargo los mensajes de todas las conversaciones para saber cuales no se han leido
  useEffect(() => {
    if (!usuarioActual || listaVecinos.length === 0) return

    const ids = listaVecinos.map((v) => v.id)

    supabase
      .from("messages")
      .select("id, sender_id, receiver_id, created_at, read")
      .or(
        ids.map((id) =>
          `and(sender_id.eq.${usuarioActual.id},receiver_id.eq.${id}),` +
          `and(sender_id.eq.${id},receiver_id.eq.${usuarioActual.id})`
        ).join(",")
      )
      .order("created_at", { ascending: false })
      .then(({ data }) => {
        if (!data) return
        const info = {}
        data.forEach((msg) => {
          // saco quien es el otro en este mensaje
          const otroId = msg.sender_id === usuarioActual.id ? msg.receiver_id : msg.sender_id
          if (!info[otroId]) {
            info[otroId] = { ultimoMensaje: msg.created_at, noLeidos: 0 }
          }
          // si me lo mando el y no lo he leido lo cuento
          if (msg.sender_id === otroId && !msg.read) {
            info[otroId].noLeidos += 1
          }
        })
        setDatosConversacion(info)
      })
  }, [usuarioActual, listaVecinos])

  // canal global para recibir mensajes nuevos de cualquier vecino y actualizar el badge
  useEffect(() => {
    if (!usuarioActual) return

    const canal = supabase
      .channel(`inbox-${usuarioActual.id}`)
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: "messages" },
        (payload) => {
          const msg = payload.new
          // solo me interesan los mensajes que me llegan a mi
          if (msg.receiver_id !== usuarioActual.id) return

          const quienManda = msg.sender_id
          const estaAbierto = refVecinoActivo.current?.id === quienManda

          setDatosConversacion((prev) => {
            const actual = prev[quienManda] || { ultimoMensaje: null, noLeidos: 0 }
            return {
              ...prev,
              [quienManda]: {
                ultimoMensaje: msg.created_at,
                // si tengo la conversacion abierta no sumo
                noLeidos: estaAbierto ? 0 : actual.noLeidos + 1,
              },
            }
          })
        }
      )
      .subscribe()

    return () => supabase.removeChannel(canal)
  }, [usuarioActual])

  // cuando selecciono un vecino cargo su historial y abro el canal de esa conv
  useEffect(() => {
    if (!usuarioActual || !vecinoSeleccionado) return

    // cierro el canal anterior para no acumular
    if (refCanal.current) {
      supabase.removeChannel(refCanal.current)
      refCanal.current = null
    }

    setListaMensajes([])

    // pongo a 0 los no leidos cuando abro la conversacion
    setDatosConversacion((prev) => ({
      ...prev,
      [vecinoSeleccionado.id]: {
        ...(prev[vecinoSeleccionado.id] || {}),
        noLeidos: 0,
      },
    }))

    // marco como leidos en la base de datos
    supabase
      .from("messages")
      .update({ read: true })
      .eq("sender_id", vecinoSeleccionado.id)
      .eq("receiver_id", usuarioActual.id)
      .or("read.eq.false,read.is.null")
      .then(() => {})

    // cargo el historial de mensajes entre los dos
    supabase
      .from("messages")
      .select("*")
      .or(
        `and(sender_id.eq.${usuarioActual.id},receiver_id.eq.${vecinoSeleccionado.id}),` +
        `and(sender_id.eq.${vecinoSeleccionado.id},receiver_id.eq.${usuarioActual.id})`
      )
      .order("created_at", { ascending: true })
      .then(({ data }) => setListaMensajes(data || []))

    // abro canal de tiempo real para esta conversacion concreta
    const canal = supabase
      .channel(`chat-${[usuarioActual.id, vecinoSeleccionado.id].sort().join("-")}`)
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: "messages" },
        (payload) => {
          const msg = payload.new
          const esDeEstaConv =
            (msg.sender_id === usuarioActual.id && msg.receiver_id === vecinoSeleccionado.id) ||
            (msg.sender_id === vecinoSeleccionado.id && msg.receiver_id === usuarioActual.id)

          if (!esDeEstaConv) return

          // evito duplicados por si llega dos veces
          setListaMensajes((prev) => {
            if (prev.some((m) => m.id === msg.id)) return prev
            return [...prev, msg]
          })

          // actualizo el timestamp del ultimo mensaje
          const otroId = msg.sender_id === usuarioActual.id ? msg.receiver_id : msg.sender_id
          setDatosConversacion((prev) => ({
            ...prev,
            [otroId]: {
              ...(prev[otroId] || {}),
              ultimoMensaje: msg.created_at,
              noLeidos: 0,
            },
          }))
        }
      )
      .subscribe()

    refCanal.current = canal

    return () => {
      supabase.removeChannel(canal)
      refCanal.current = null
    }
  }, [usuarioActual, vecinoSeleccionado])

  // scroll automatico al ultimo mensaje
  useEffect(() => {
    refFinal.current?.scrollIntoView({ behavior: "smooth" })
  }, [listaMensajes])

  // funcion para enviar mensaje
  const enviarMensaje = async (e) => {
    e.preventDefault()
    if (!mensajeEscrito.trim() || !vecinoSeleccionado || !usuarioActual) return

    const contenido = mensajeEscrito.trim()
    setMensajeEscrito("")

    const { data } = await supabase
      .from("messages")
      .insert({
        sender_id: usuarioActual.id,
        receiver_id: vecinoSeleccionado.id,
        content: contenido,
      })
      .select()
      .single()

    // actualizo el timestamp del ultimo mensaje que he mandado yo
    if (data) {
      setDatosConversacion((prev) => ({
        ...prev,
        [vecinoSeleccionado.id]: {
          ...(prev[vecinoSeleccionado.id] || {}),
          ultimoMensaje: data.created_at,
          noLeidos: 0,
        },
      }))
    }
  }

  // saco el nombre del usuario para mostrarlo
  const getNombre = (u) =>
    u?.full_name || u?.username || u?.email?.split("@")[0] || "Usuario"

  // formateo la hora del mensaje
  const getHora = (ts) =>
    new Date(ts).toLocaleTimeString("es-ES", { hour: "2-digit", minute: "2-digit" })

  // ordeno los vecinos por ultimo mensaje, el mas reciente arriba
  const vecinosOrdenados = [...listaVecinos].sort((a, b) => {
    const tsA = datosConversacion[a.id]?.ultimoMensaje || null
    const tsB = datosConversacion[b.id]?.ultimoMensaje || null
    if (!tsA && !tsB) return 0
    if (!tsA) return 1
    if (!tsB) return -1
    return new Date(tsB) - new Date(tsA)
  })

  return (
    <div className="bc-wrapper">

      {/* lista de vecinos */}
      <div className={`bc-panel${pantallaMovil === "chat" ? " oculto" : ""}`}>
        <div className="bc-panel-header">
          <button className="bc-back-btn" onClick={() => setPantallaMovil("lista")}>≡</button>
          <h2 className="bc-panel-title">BarrioChat</h2>
        </div>
        <ul className="bc-user-list">
          {vecinosOrdenados.length === 0 && (
            <li className="bc-empty-panel">No hay vecinos disponibles</li>
          )}
          {vecinosOrdenados.map((v) => {
            const datos = datosConversacion[v.id] || {}
            const noLeidos = datos.noLeidos || 0
            return (
              <li
                key={v.id}
                className={`bc-user-item${vecinoSeleccionado?.id === v.id ? " activo" : ""}`}
                onClick={() => { setVecinoSeleccionado(v); setPantallaMovil("chat") }}
              >
                <div className="bc-avatar-wrap">
                  <div className="bc-avatar">{getNombre(v)[0].toUpperCase()}</div>
                  {noLeidos > 0 && (
                    <span className="bc-badge">{noLeidos > 99 ? "99+" : noLeidos}</span>
                  )}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div className="bc-user-name">{getNombre(v)}</div>
                  <div className="bc-user-rol">{v.role || "Vecino"}</div>
                </div>
              </li>
            )
          })}
        </ul>
      </div>

      {/* zona de mensajes */}
      <div
        className={`bc-chat${pantallaMovil === "lista" ? " oculto" : ""}`}
        style={{ display: window.innerWidth > 640 ? "flex" : undefined }}
      >
        <div className="bc-chat-box">
          {vecinoSeleccionado ? (
            <>
              {/* cabecera con el nombre del vecino */}
              <div className="bc-chat-header">
                <button className="bc-back-btn" onClick={() => setPantallaMovil("lista")}>←</button>
                <div className="bc-avatar" style={{ background: "#eef2ff", color: "#2f6bff" }}>
                  {getNombre(vecinoSeleccionado)[0].toUpperCase()}
                </div>
                <div>
                  <div className="bc-chat-header-name">{getNombre(vecinoSeleccionado)}</div>
                  <div className="bc-chat-header-rol">{vecinoSeleccionado.role || "Vecino"}</div>
                </div>
              </div>

              {/* mensajes */}
              <ul className="bc-messages">
                {listaMensajes.length === 0 && (
                  <li className="bc-hello">Di hola a {getNombre(vecinoSeleccionado)} 👋</li>
                )}
                {listaMensajes.map((msg) => {
                  const esMio = msg.sender_id === usuarioActual?.id
                  return (
                    <li key={msg.id} className={`bc-msg-row${esMio ? " mio" : " otro"}`}>
                      <div className={`bc-bubble${esMio ? " mio" : " otro"}`}>
                        {!esMio && (
                          <div className="bc-bubble-remitente">{getNombre(vecinoSeleccionado)}</div>
                        )}
                        {msg.content}
                        <div className="bc-bubble-hora">{getHora(msg.created_at)}</div>
                      </div>
                    </li>
                  )
                })}
                <li ref={refFinal} style={{ listStyle: "none" }} />
              </ul>

              {/* input para escribir */}
              <form className="bc-form" onSubmit={enviarMensaje}>
                <div className="bc-input-row">
                  <input
                    className="bc-input"
                    value={mensajeEscrito}
                    onChange={(e) => setMensajeEscrito(e.target.value)}
                    placeholder="Escribe tu mensaje..."
                    autoComplete="off"
                  />
                  <button type="submit" className="bc-send-btn" disabled={!mensajeEscrito.trim()}>
                    Enviar
                  </button>
                </div>
              </form>
            </>
          ) : (
            /* si no hay vecino seleccionado muestro esto */
            <div className="bc-empty-chat">
              <div className="bc-empty-icon">💬</div>
              <p>Selecciona un vecino</p>
              <span>para empezar a chatear</span>
            </div>
          )}
        </div>
      </div>

    </div>
  )
}