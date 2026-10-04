package com.barriored.app.ui.asistente

/**
 * Mismas respuestas y palabras clave que src/components/ChatbotAyuda/ChatbotAyuda.jsx,
 * adaptadas a la app (en el móvil el menú está en la barra de abajo).
 */
object ContenidoAsistente {

    const val BIENVENIDA =
        "¡Hola! 👋 Soy el asistente de BarrioRed.\n\nEstoy aquí para ayudarte a usar la plataforma. Pulsa una opción o escríbeme tu pregunta."

    val RESPUESTAS = mapOf(
        "empezar" to "¡Bienvenido a BarrioRed! 🏘️\n\nEn la barra de abajo encontrarás todo:\n\n• 📊 Inicio → resumen de tu comunidad\n• 📰 Muro → anuncios y noticias\n• ⚠️ Incidencias → reportar problemas\n• 💬 Chat → hablar con vecinos\n• 👤 Perfil → tus datos, documentos y ajustes\n\n¿Sobre cuál quieres saber más?",
        "muro" to "El Muro es el tablón de anuncios de tu comunidad. 📰\n\nAhí el presidente o el ayuntamiento publican noticias importantes.\n\nPara verlo, pulsa 'Muro' en la barra de abajo.",
        "incidencias" to "Las incidencias sirven para reportar problemas en tu comunidad (averías, desperfectos, etc.). ⚠️\n\nPara crear una:\n1. Pulsa 'Incidencias' en la barra de abajo\n2. Toca '+ Nueva incidencia'\n3. Escribe el título y la descripción\n4. Pulsa 'Enviar incidencia'\n\nEl administrador recibirá tu aviso.",
        "chat" to "Con el Chat puedes hablar directamente con tus vecinos. 💬\n\nPara usarlo:\n1. Pulsa 'Chat' en la barra de abajo\n2. Elige el vecino con quien quieras hablar\n3. Escribe tu mensaje y pulsa Enviar\n\n¡Es como el WhatsApp pero dentro de BarrioRed!",
        "documentos" to "En Documentos encontrarás actas de reuniones, normativas y otros archivos importantes de tu comunidad. 📁\n\nEntra en 'Perfil' y pulsa 'Documentos de la comunidad': se abrirá la carpeta compartida de Google Drive.",
        "perfil" to "En 'Perfil' puedes ver y editar tus datos personales. 👤\n\nDesde ahí también puedes:\n• Cambiar tu contraseña\n• Actualizar tu nombre, teléfono y piso\n• Cambiar entre modo claro y oscuro",
        "contrasena" to "Para cambiar tu contraseña:\n\n1. Pulsa 'Perfil' en la barra de abajo\n2. Toca 'Cambiar contraseña'\n3. Escribe la actual y la nueva dos veces\n4. Pulsa 'Guardar'\n\nDebe tener al menos 6 caracteres.",
        "cerrarSesion" to "Para cerrar sesión:\n\n👉 Entra en 'Perfil' y baja hasta el final: ahí está el botón 'Cerrar sesión'.\n\nSi alguien más usa el móvil, siempre es buena idea cerrar sesión al terminar.",
        "ayuda" to "Estoy aquí para ayudarte con BarrioRed. 😊\n\nPuedes preguntarme sobre:\n• Cómo usar el Muro o el Chat\n• Cómo reportar una incidencia\n• Dónde están los documentos\n• Cómo cambiar tus datos o contraseña\n\n¿Qué necesitas?",
        "saludo" to "¡Hola! 👋 Soy el asistente de BarrioRed.\n\nEstoy aquí para ayudarte a usar la plataforma. Pulsa una de las opciones de abajo o escríbeme tu pregunta.",
        "gracias" to "¡De nada! Para eso estoy. 😊\n\nSi tienes más dudas, no dudes en preguntarme.",
        "noEntiendo" to "Lo siento, no he entendido bien tu pregunta. 😅\n\nPrueba a pulsar uno de los botones de abajo, o pregúntame cosas como:\n• '¿Cómo pongo una incidencia?'\n• '¿Dónde está el chat?'\n• '¿Cómo cambio mi contraseña?'",
    )

    private val PALABRAS_CLAVE = mapOf(
        "saludo" to listOf("hola", "buenas", "buenos días", "buenas tardes", "hey", "saludos"),
        "empezar" to listOf("empezar", "empiezo", "inicio", "comenzar", "nuevo", "primera vez", "cómo funciona"),
        "muro" to listOf("muro", "anuncio", "anuncios", "tablón", "noticias", "publicación"),
        "incidencias" to listOf("incidencia", "incidencias", "avería", "problema", "reportar", "desperfecto"),
        "chat" to listOf("chat", "mensaje", "mensajes", "hablar", "vecino", "escribir"),
        "documentos" to listOf("documento", "documentos", "acta", "archivo", "normativa", "drive"),
        "perfil" to listOf("perfil", "datos", "nombre", "teléfono", "piso", "ajustes"),
        "contrasena" to listOf("contraseña", "password", "clave", "cambiar contraseña"),
        "cerrarSesion" to listOf("cerrar sesión", "salir", "logout", "desconectar"),
        "gracias" to listOf("gracias", "muchas gracias", "perfecto", "genial", "ok gracias"),
    )

    // Primero los temas concretos y al final saludos/agradecimientos (igual que la web)
    private val ORDEN = listOf(
        "contrasena", "cerrarSesion", "incidencias", "muro", "documentos",
        "perfil", "chat", "empezar", "gracias", "saludo",
    )

    data class BotonRapido(val codigo: String, val icono: String, val texto: String)

    val BOTONES = listOf(
        BotonRapido("empezar", "🏘️", "¿Cómo empezar?"),
        BotonRapido("incidencias", "⚠️", "Incidencias"),
        BotonRapido("muro", "📰", "El Muro"),
        BotonRapido("chat", "💬", "El Chat"),
        BotonRapido("contrasena", "🔑", "Contraseña"),
        BotonRapido("documentos", "📁", "Documentos"),
    )

    fun respuesta(codigo: String): String = RESPUESTAS[codigo] ?: RESPUESTAS.getValue("noEntiendo")

    fun buscarRespuesta(texto: String): String {
        val t = texto.lowercase()
        val categoria = ORDEN.firstOrNull { c -> PALABRAS_CLAVE.getValue(c).any { t.contains(it) } }
        return respuesta(categoria ?: "noEntiendo")
    }
}
