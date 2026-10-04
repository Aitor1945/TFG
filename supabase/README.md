# Base de datos de BarrioRed (Supabase)

`schema.sql` crea desde cero la base de datos que usan la web y la app Android:

| Tabla | Para qué sirve |
|---|---|
| `comunidades` | Cada comunidad de vecinos (nombre, ciudad, enlace de Drive…) |
| `profiles` | Datos de cada usuario: nombre, piso, teléfono, **rol** y **comunidad** |
| `muro_publicaciones` | Anuncios del Muro |
| `incidencias` | Incidencias y su estado (`pendiente`, `en_proceso`, `resuelta`) |
| `messages` | Mensajes del chat (con tiempo real activado) |
| `actividad_usuario` | "Actividad reciente" de Mi perfil |

Roles posibles en `profiles.role`: `vecino`, `presidente`, `admin`, `ayuntamiento` y `conserje`.
Presidente, admin y ayuntamiento publican en el Muro y gestionan incidencias; el conserje puede borrar incidencias.

Triggers (los mismos que en el anexo de la memoria): `create_profile` (crea el perfil al dar de alta un usuario),
`trigger_actualizar_vecinos` (mantiene `comunidades.num_vecinos`) y `trg_actividad_incidencia` (rellena la actividad reciente).

> Respecto al script original de la memoria, la seguridad es **más estricta**: antes cualquiera podía leer todos
> los perfiles y todas las comunidades; ahora cada usuario solo ve los de su comunidad.

## Seguridad incluida (RLS)

- Cada usuario **solo ve los datos de su comunidad**.
- Cualquier vecino puede crear incidencias; **solo los gestores** cambian su estado, las borran o publican en el Muro.
- Cada uno solo puede editar **sus** datos personales; **nadie puede cambiarse el rol ni la comunidad** desde la web o la app.
- Los mensajes solo los ven emisor y receptor, y solo se puede escribir a vecinos de la misma comunidad.
- Sin sesión iniciada no se ve nada.

## Montarla en un proyecto nuevo

### 1. Crear el proyecto
1. Entra en [supabase.com](https://supabase.com) con **tu** cuenta → **New project**.
2. Nombre: `barriored`, una contraseña de base de datos (guárdala) y la región **West EU** (o la más cercana).
3. Espera 1-2 minutos a que se cree.

### 2. Crear las tablas
1. Menú izquierdo → **SQL Editor** → **New query**.
2. Copia **todo** el contenido de `schema.sql`, pégalo y pulsa **Run**.
3. Debe poner *"Success. No rows returned"*. En **Table Editor** verás las 6 tablas y una comunidad de ejemplo.

### 3. Configurar el inicio de sesión
Menú izquierdo → **Authentication** → **URL Configuration**:
- **Site URL**: la URL de tu web en Vercel (por ejemplo `https://barriored.vercel.app`).
- **Redirect URLs**: añade `https://barriored.vercel.app/**` y `http://localhost:5173/**`.

Así funcionan los enlaces de "¿Has olvidado tu contraseña?".

### 4. Crear usuarios
La web no tiene página de registro, así que los usuarios se crean desde el panel:
1. **Authentication → Users → Add user → Create new user**.
2. Correo y contraseña → marca **Auto Confirm User** → **Create user**.
3. Su perfil se crea solo (rol `vecino`, en la primera comunidad).

Para hacer a alguien **presidente**, en **Table Editor → profiles** cambia su `role`, o en el SQL Editor:
```sql
update profiles set role = 'presidente' where email = 'tu@correo.com';
```

### 5. Poner las claves nuevas en todos los sitios
Las claves están en **Project Settings → Data API** (Project URL) y **Project Settings → API Keys** (`anon` / *publishable*).

| Dónde | Variables |
|---|---|
| Web en local: archivo `.env` en la raíz (no se sube a git) | `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY` |
| Web publicada: Vercel → proyecto → Settings → Environment Variables (y luego **Redeploy**) | `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY` |
| App Android: `app-android/local.properties` | `SUPABASE_URL`, `SUPABASE_ANON_KEY` |
| APK de GitHub: repo → Settings → Secrets and variables → Actions | `SUPABASE_URL`, `SUPABASE_ANON_KEY` |

### 6. Opcional
- **Documentos**: en `comunidades`, pega en `drive_url` el enlace de la carpeta de Google Drive de la comunidad.
- **Más comunidades**: añade filas en `comunidades` y asigna a cada usuario su `comunidad_id`.

> ⚠️ El plan gratuito **pausa el proyecto tras ~7 días sin uso**. Si la web da *"Failed to fetch"*, entra en Supabase y pulsa **Restore project**.
