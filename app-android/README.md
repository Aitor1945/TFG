# BarrioRed – App Android

App nativa de BarrioRed hecha con **Kotlin + Jetpack Compose**. Usa **el mismo Supabase que la web**,
así que los usuarios, el muro, las incidencias y el chat son compartidos: lo que publicas en el móvil
aparece en la web y al revés.

## Cómo abrirla

1. Instala **Android Studio** (versión reciente).
2. `File > Open` y elige la carpeta `app-android/` (no la raíz del repo).
3. Copia `local.properties.example` como `local.properties` y pon tus claves de Supabase
   (las mismas que `VITE_SUPABASE_URL` y `VITE_SUPABASE_ANON_KEY` del `.env` de la web).
   `local.properties` está en `.gitignore`, así que no se sube a GitHub.
4. Espera a que Gradle sincronice y dale a ▶ con un emulador o un móvil conectado.

> Si Android Studio te propone actualizar AGP o alguna librería, puedes aceptar sin problema.

## Descargar el APK sin Android Studio

Cada cambio en `app-android/` lanza el workflow **App Android** (`.github/workflows/android.yml`), que compila la app.
En GitHub: pestaña **Actions** → la última ejecución de *App Android* → abajo, en *Artifacts*, descarga **BarrioRed-apk**
(un .zip con el `.apk`). Pásalo al móvil e instálalo (Android pedirá permitir "orígenes desconocidos").

Para que el APK se conecte a Supabase, añade una vez en el repo `Settings > Secrets and variables > Actions`
los secrets `SUPABASE_URL` y `SUPABASE_ANON_KEY`.

## Probar en varios tamaños de móvil

En Android Studio, en **Device Manager** crea varios dispositivos virtuales (por ejemplo, un Pixel pequeño,
uno grande y una tablet) y elige en cuál ejecutar con el desplegable que hay junto al botón ▶.
También existe el dispositivo *Resizable*, que permite cambiar entre móvil, plegable y tablet sin reiniciar.

## Qué incluye

| Pantalla | Equivale en la web a | Qué hace |
|---|---|---|
| Login | `Login.jsx` | Iniciar sesión y recuperar contraseña |
| Inicio | `Dashboard.jsx` | Saludo, últimos anuncios e incidencias |
| Muro | `Muro.jsx` | Ver anuncios; publicar si eres presidente/admin/ayuntamiento |
| Incidencias | `Incidencias.jsx` | Ver, filtrar y crear; los gestores cambian el estado o las borran |
| Chat | `Chat.jsx` | Lista de vecinos con no leídos y conversación **en tiempo real** |
| Perfil | `MiPerfil.jsx` + `Ajustes.jsx` + `Documentos.jsx` | Datos, comunidad, editar perfil, documentos de Drive, cerrar sesión |

La sesión se guarda en el móvil: al volver a abrir la app no hace falta iniciar sesión otra vez.

## Arquitectura (MVVM)

```
app/src/main/java/com/barriored/app/
├── MainActivity.kt              Punto de entrada
├── data/
│   ├── SupabaseCliente.kt       Cliente único (como src/lib/supabase.js)
│   ├── model/Modelos.kt         Data classes de las tablas (@Serializable)
│   └── repo/                    Repositorios: toda la comunicación con Supabase
│       ├── AuthRepositorio.kt
│       ├── PerfilRepositorio.kt
│       ├── MuroRepositorio.kt
│       ├── IncidenciasRepositorio.kt
│       └── ChatRepositorio.kt   Incluye el canal Realtime
└── ui/
    ├── theme/Tema.kt            Colores de la web, modo claro/oscuro del sistema
    ├── navegacion/Navegacion.kt Login o app según la sesión + barra inferior
    ├── componentes/             Piezas reutilizables (tarjetas, cargando, diálogos…)
    └── pantallas/               Cada archivo = ViewModel + pantalla Compose
```

- **Vista (Compose)**: solo pinta el estado y avisa al ViewModel de lo que hace el usuario.
- **ViewModel**: guarda el estado en un `StateFlow` y lanza las corrutinas.
- **Repositorio**: hace las consultas a Supabase (`supabase-kt`).

## Librerías

- [supabase-kt](https://github.com/supabase-community/supabase-kt): Auth, Postgrest y Realtime
- Jetpack Compose + Material 3
- Navigation Compose
- Kotlinx Serialization y Ktor (red)

## Importante: seguridad

La `anon key` va dentro del APK y cualquiera puede sacarla. Es normal en Supabase, pero
**todas las tablas tienen que tener RLS (Row Level Security) activado** con políticas correctas
(por ejemplo, que solo los gestores puedan cambiar el estado de una incidencia). La comprobación
de roles de la app es solo visual; la seguridad real la pone Supabase.

## Ideas para seguir

- Notificaciones push de chat y muro (Firebase Cloud Messaging + Edge Function de Supabase)
- Tiempo y ubicación en Inicio (como el Dashboard de la web), pidiendo el permiso de ubicación
- Foto de perfil con la cámara (Supabase Storage)
- Enlace profundo `barriored://reset-password` para cambiar la contraseña dentro de la app
- Chatbot de ayuda (`ChatbotAyuda.jsx`)
