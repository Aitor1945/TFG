-- =====================================================================
--  BarrioRed · Esquema completo de la base de datos (Supabase)
-- =====================================================================
--  Basado en el script del anexo de la memoria del TFG y ajustado a las
--  consultas de la web (src/) y de la app Android (app-android/). Crea las
--  tablas, la seguridad (RLS), los triggers y el chat en tiempo real.
--
--  Cómo usarlo en un proyecto NUEVO de Supabase:
--    1. supabase.com > tu proyecto > SQL Editor > New query
--    2. Pega este archivo entero y pulsa "Run".
--    3. Sigue los pasos de supabase/README.md (crear usuarios, etc.).
--
--  Se puede ejecutar varias veces sin romper nada (no borra datos).
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. TABLAS
-- ---------------------------------------------------------------------

-- Comunidades de vecinos (cada usuario pertenece a una)
create table if not exists public.comunidades (
    id                 uuid primary key default gen_random_uuid(),
    nombre             text not null,
    tipo               text not null default 'Comunidad de vecinos',
    ciudad             text,
    num_vecinos        integer default 0,    -- lo mantiene el trigger actualizar_num_vecinos
    num_bloques        integer default 0,
    num_zonas_comunes  integer default 0,
    drive_url          text default 'https://drive.google.com/drive/folders',  -- página Documentos
    created_at         timestamptz not null default now()
);

-- Perfil de cada usuario (1 a 1 con auth.users)
create table if not exists public.profiles (
    id            uuid primary key references auth.users (id) on delete cascade,
    email         text not null,
    username      text,
    full_name     text,
    avatar_url    text,
    telefono      text,
    piso          text,
    role          text not null default 'vecino'
                  check (role in ('vecino', 'presidente', 'admin', 'ayuntamiento', 'conserje')),
    comunidad_id  uuid references public.comunidades (id) on delete set null,
    created_at    timestamptz not null default now()
);

-- Anuncios del Muro (los publican presidente / admin / ayuntamiento)
create table if not exists public.muro_publicaciones (
    id            uuid primary key default gen_random_uuid(),
    titulo        text,
    contenido     text not null,
    tipo          text not null default 'texto' check (tipo in ('texto', 'anuncio')),
    autor_id      uuid not null references public.profiles (id) on delete cascade,
    comunidad_id  uuid not null references public.comunidades (id) on delete cascade,
    created_at    timestamptz not null default now()
);

-- Incidencias (las crea cualquier vecino; las gestionan los roles de gestión)
create table if not exists public.incidencias (
    id            uuid primary key default gen_random_uuid(),
    titulo        text not null,
    descripcion   text,
    estado        text not null default 'pendiente'
                  check (estado in ('pendiente', 'en_proceso', 'resuelta')),
    fecha         date default current_date,
    autor_id      uuid not null references public.profiles (id) on delete cascade,
    comunidad_id  uuid not null references public.comunidades (id) on delete cascade,
    created_at    timestamptz not null default now()
);

-- Mensajes del chat entre vecinos
create table if not exists public.messages (
    id           uuid primary key default gen_random_uuid(),
    sender_id    uuid not null references public.profiles (id) on delete cascade,
    receiver_id  uuid not null references public.profiles (id) on delete cascade,
    content      text not null,
    read         boolean not null default false,
    created_at   timestamptz not null default now()
);

-- Actividad reciente que se muestra en "Mi perfil"
create table if not exists public.actividad_usuario (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid not null references public.profiles (id) on delete cascade,
    tipo         text not null,               -- 'incidencia', 'reserva', 'documento', 'chat'
    titulo       text not null,
    descripcion  text,
    created_at   timestamptz not null default now()
);

-- Índices para las consultas más habituales
create index if not exists idx_profiles_comunidad   on public.profiles (comunidad_id);
create index if not exists idx_muro_comunidad_fecha  on public.muro_publicaciones (comunidad_id, created_at desc);
create index if not exists idx_incid_comunidad_fecha on public.incidencias (comunidad_id, created_at desc);
create index if not exists idx_incid_autor           on public.incidencias (autor_id);
create index if not exists idx_msg_conversacion      on public.messages (sender_id, receiver_id, created_at);
create index if not exists idx_msg_no_leidos         on public.messages (receiver_id) where not read;
create index if not exists idx_actividad_usuario     on public.actividad_usuario (user_id, created_at desc);


-- ---------------------------------------------------------------------
-- 2. FUNCIONES DE AYUDA PARA LA SEGURIDAD
-- ---------------------------------------------------------------------
-- "security definer" permite leer profiles sin que la propia RLS de
-- profiles entre en bucle.

create or replace function public.mi_comunidad()
returns uuid
language sql stable security definer
set search_path = public
as $$
    select comunidad_id from public.profiles where id = auth.uid()
$$;

create or replace function public.es_gestor()
returns boolean
language sql stable security definer
set search_path = public
as $$
    select coalesce(
        (select role in ('presidente', 'admin', 'ayuntamiento')
           from public.profiles where id = auth.uid()),
        false)
$$;


-- ---------------------------------------------------------------------
-- 3. SEGURIDAD (Row Level Security)
-- ---------------------------------------------------------------------
-- Regla general: cada usuario solo ve los datos de SU comunidad.

alter table public.comunidades        enable row level security;
alter table public.profiles           enable row level security;
alter table public.muro_publicaciones enable row level security;
alter table public.incidencias        enable row level security;
alter table public.messages           enable row level security;
alter table public.actividad_usuario  enable row level security;

-- comunidades: ver solo la propia
drop policy if exists "comunidades: ver la mia" on public.comunidades;
create policy "comunidades: ver la mia" on public.comunidades
    for select to authenticated
    using (id = public.mi_comunidad());

-- profiles: ver los vecinos de mi comunidad (y siempre el mío)
drop policy if exists "profiles: ver mi comunidad" on public.profiles;
create policy "profiles: ver mi comunidad" on public.profiles
    for select to authenticated
    using (id = auth.uid() or comunidad_id = public.mi_comunidad());

-- profiles: editar solo mi fila...
drop policy if exists "profiles: editar el mio" on public.profiles;
create policy "profiles: editar el mio" on public.profiles
    for update to authenticated
    using (id = auth.uid())
    with check (id = auth.uid());
-- ...y solo los datos personales: nadie puede cambiarse el rol ni la comunidad
revoke update on public.profiles from authenticated, anon;
grant  update (username, full_name, avatar_url, telefono, piso) on public.profiles to authenticated;

-- muro: ver los de mi comunidad
drop policy if exists "muro: ver mi comunidad" on public.muro_publicaciones;
create policy "muro: ver mi comunidad" on public.muro_publicaciones
    for select to authenticated
    using (comunidad_id = public.mi_comunidad());

-- muro: publicar / editar / borrar solo los gestores, en su comunidad
drop policy if exists "muro: gestores publican" on public.muro_publicaciones;
create policy "muro: gestores publican" on public.muro_publicaciones
    for insert to authenticated
    with check (public.es_gestor()
                and autor_id = auth.uid()
                and comunidad_id = public.mi_comunidad());

drop policy if exists "muro: gestores editan" on public.muro_publicaciones;
create policy "muro: gestores editan" on public.muro_publicaciones
    for update to authenticated
    using (public.es_gestor() and comunidad_id = public.mi_comunidad())
    with check (comunidad_id = public.mi_comunidad());

drop policy if exists "muro: gestores borran" on public.muro_publicaciones;
create policy "muro: gestores borran" on public.muro_publicaciones
    for delete to authenticated
    using (public.es_gestor() and comunidad_id = public.mi_comunidad());

-- incidencias: ver las de mi comunidad
drop policy if exists "incidencias: ver mi comunidad" on public.incidencias;
create policy "incidencias: ver mi comunidad" on public.incidencias
    for select to authenticated
    using (comunidad_id = public.mi_comunidad());

-- incidencias: cualquier vecino puede crear (a su nombre y en su comunidad)
drop policy if exists "incidencias: vecinos crean" on public.incidencias;
create policy "incidencias: vecinos crean" on public.incidencias
    for insert to authenticated
    with check (autor_id = auth.uid() and comunidad_id = public.mi_comunidad());

-- incidencias: cambiar estado solo los gestores (borrar, también el conserje)
drop policy if exists "incidencias: gestores editan" on public.incidencias;
create policy "incidencias: gestores editan" on public.incidencias
    for update to authenticated
    using (public.es_gestor() and comunidad_id = public.mi_comunidad())
    with check (comunidad_id = public.mi_comunidad());

drop policy if exists "incidencias: gestores borran" on public.incidencias;
create policy "incidencias: gestores borran" on public.incidencias
    for delete to authenticated
    using (comunidad_id = public.mi_comunidad()
           and (public.es_gestor()
                or exists (select 1 from public.profiles
                            where id = auth.uid() and role = 'conserje')));

-- messages: ver solo mis conversaciones
drop policy if exists "messages: ver los mios" on public.messages;
create policy "messages: ver los mios" on public.messages
    for select to authenticated
    using (sender_id = auth.uid() or receiver_id = auth.uid());

-- messages: enviar a mi nombre y solo a vecinos de mi comunidad
drop policy if exists "messages: enviar" on public.messages;
create policy "messages: enviar" on public.messages
    for insert to authenticated
    with check (
        sender_id = auth.uid()
        and exists (select 1 from public.profiles p
                     where p.id = receiver_id
                       and p.comunidad_id = public.mi_comunidad())
    );

-- messages: el receptor solo puede marcarlos como leídos
drop policy if exists "messages: marcar leidos" on public.messages;
create policy "messages: marcar leidos" on public.messages
    for update to authenticated
    using (receiver_id = auth.uid())
    with check (receiver_id = auth.uid());
revoke update on public.messages from authenticated, anon;
grant  update (read) on public.messages to authenticated;

-- actividad: ver y añadir solo la mía
drop policy if exists "actividad: ver la mia" on public.actividad_usuario;
create policy "actividad: ver la mia" on public.actividad_usuario
    for select to authenticated
    using (user_id = auth.uid());

drop policy if exists "actividad: crear la mia" on public.actividad_usuario;
create policy "actividad: crear la mia" on public.actividad_usuario
    for insert to authenticated
    with check (user_id = auth.uid());


-- ---------------------------------------------------------------------
-- 4. TRIGGERS
-- ---------------------------------------------------------------------

-- handle_new_user: al crear un usuario en Authentication se crea su perfil.
-- Comunidad: la indicada en los metadatos (comunidad_id) o, si no, la primera
-- comunidad que exista (cómodo cuando solo hay una).
create or replace function public.handle_new_user()
returns trigger
language plpgsql security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, email, username, full_name, role, comunidad_id)
    values (
        new.id,
        new.email,
        coalesce(new.raw_user_meta_data ->> 'username', split_part(new.email, '@', 1)),
        coalesce(new.raw_user_meta_data ->> 'full_name', split_part(new.email, '@', 1)),
        'vecino',
        coalesce(
            nullif(new.raw_user_meta_data ->> 'comunidad_id', '')::uuid,
            (select id from public.comunidades order by created_at limit 1)
        )
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists create_profile on auth.users;
create trigger create_profile
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- actualizar_num_vecinos: recalcula comunidades.num_vecinos al añadir un
-- perfil o cambiarlo de comunidad.
create or replace function public.actualizar_num_vecinos()
returns trigger
language plpgsql security definer
set search_path = public
as $$
begin
    if new.comunidad_id is not null then
        update public.comunidades
           set num_vecinos = (select count(*) from public.profiles where comunidad_id = new.comunidad_id)
         where id = new.comunidad_id;
    end if;
    if tg_op = 'UPDATE' and old.comunidad_id is distinct from new.comunidad_id
       and old.comunidad_id is not null then
        update public.comunidades
           set num_vecinos = (select count(*) from public.profiles where comunidad_id = old.comunidad_id)
         where id = old.comunidad_id;
    end if;
    return new;
end;
$$;

drop trigger if exists trigger_actualizar_vecinos on public.profiles;
create trigger trigger_actualizar_vecinos
    after insert or update of comunidad_id on public.profiles
    for each row execute function public.actualizar_num_vecinos();

-- registrar_actividad: apunta en actividad_usuario lo que hace cada vecino.
-- (Preparada para reservas y documentos cuando existan esas tablas.)
create or replace function public.registrar_actividad()
returns trigger
language plpgsql security definer
set search_path = public
as $$
begin
    if tg_table_name = 'incidencias' then
        insert into public.actividad_usuario (user_id, tipo, titulo, descripcion)
        values (new.autor_id, 'incidencia', 'Incidencia reportada', new.titulo);
    end if;
    return new;
end;
$$;

drop trigger if exists trg_actividad_incidencia on public.incidencias;
create trigger trg_actividad_incidencia
    after insert on public.incidencias
    for each row execute function public.registrar_actividad();


-- ---------------------------------------------------------------------
-- 5. TIEMPO REAL (chat)
-- ---------------------------------------------------------------------
-- La web y la app escuchan los INSERT de "messages" para el chat en vivo.
do $$
begin
    if not exists (
        select 1 from pg_publication_tables
         where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'messages'
    ) then
        alter publication supabase_realtime add table public.messages;
    end if;
end;
$$;


-- ---------------------------------------------------------------------
-- 6. DATOS INICIALES
-- ---------------------------------------------------------------------
-- Una comunidad de ejemplo para empezar (cámbiale los datos cuando quieras
-- desde Table Editor > comunidades).
insert into public.comunidades (nombre, tipo, ciudad, num_vecinos, num_bloques, num_zonas_comunes)
select 'Comunidad BarrioRed', 'Comunidad de vecinos', 'Madrid', 40, 2, 3
where not exists (select 1 from public.comunidades);
