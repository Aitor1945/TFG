import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../../lib/supabase.js";
import "./Dashboard.css";
import Cargando from "../../../components/Cargando/Cargando";

const MONTHS_ES = [
  "Enero",
  "Febrero",
  "Marzo",
  "Abril",
  "Mayo",
  "Junio",
  "Julio",
  "Agosto",
  "Septiembre",
  "Octubre",
  "Noviembre",
  "Diciembre",
];
const DAYS_ES = ["L", "M", "X", "J", "V", "S", "D"];
const DAYS_FULL = [
  "Domingo",
  "Lunes",
  "Martes",
  "Miércoles",
  "Jueves",
  "Viernes",
  "Sábado",
];

const EVENT_COLORS = {
  reunion: "#3b82f6",
  reserva: "#10b981",
  incidencia: "#f59e0b",
  nota: "#a78bfa",
};

const ROLE_LABELS = {
  vecino: "Vecino",
  presidente: "Presidente",
  administrador: "Administrador",
  ayuntamiento: "Ayuntamiento",
  admin: "Admin",
};

const ESTADO_COLORS = {
  abierta: "#f59e0b",
  "en curso": "#3b82f6",
  resuelta: "#10b981",
  pendiente: "#f59e0b",
  proceso: "#3b82f6",
};

const CURRENT_USER_FALLBACK = {
  name: "Usuario",
  role: "vecino",
  community: "",
};

const ACCESOS = [
  {
    label: "Incidencias",
    icon: "fa-triangle-exclamation",
    to: "/incidencias",
    color: "#f59e0b",
  },
  { label: "Muro", icon: "fa-thumbtack", to: "/muro", color: "#10b981" },
  {
    label: "Documentos",
    icon: "fa-folder-open",
    to: "/documentos",
    color: "#3b82f6",
  },
  { label: "Chat", icon: "fa-comments", to: "/chat", color: "#a78bfa" },
];

function weatherInfo(code) {
  if (code === 0) return { desc: "Despejado", icon: "fa-sun" };
  if ([1, 2].includes(code))
    return { desc: "Poco nuboso", icon: "fa-cloud-sun" };
  if (code === 3) return { desc: "Nublado", icon: "fa-cloud" };
  if ([45, 48].includes(code)) return { desc: "Niebla", icon: "fa-smog" };
  if ([51, 53, 55].includes(code))
    return { desc: "Llovizna", icon: "fa-cloud-drizzle" };
  if ([61, 63, 65].includes(code))
    return { desc: "Lluvia", icon: "fa-cloud-rain" };
  if ([71, 73, 75, 77].includes(code))
    return { desc: "Nieve", icon: "fa-snowflake" };
  if ([80, 81, 82].includes(code))
    return { desc: "Chubascos", icon: "fa-cloud-showers-heavy" };
  if ([95, 96, 99].includes(code)) return { desc: "Tormenta", icon: "fa-bolt" };
  return { desc: "Variable", icon: "fa-cloud" };
}

function getDays(year, month) {
  const first = new Date(year, month, 1).getDay();
  const offset = first === 0 ? 6 : first - 1;
  const total = new Date(year, month + 1, 0).getDate();
  const prevTotal = new Date(year, month, 0).getDate();
  const cells = [];
  for (let i = offset - 1; i >= 0; i--)
    cells.push({ d: prevTotal - i, other: true });
  for (let i = 1; i <= total; i++) cells.push({ d: i, other: false });
  while (cells.length % 7 !== 0)
    cells.push({ d: cells.length - total - offset + 1, other: true });
  return cells;
}

function WeatherCard() {
  const [weather, setWeather] = useState(null);
  const [status, setStatus] = useState("loading");

  useEffect(() => {
    if (!navigator.geolocation) {
      setStatus("error");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      async ({ coords }) => {
        try {
          const { latitude: lat, longitude: lon } = coords;
          const [geoRes, wtRes] = await Promise.all([
            fetch(
              `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`,
              { headers: { "Accept-Language": "es" } }
            ),
            fetch(
              `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}` +
                `&current=temperature_2m,weathercode,windspeed_10m,relative_humidity_2m&timezone=auto`
            ),
          ]);
          const geoData = await geoRes.json();
          const wtData = await wtRes.json();
          const cur = wtData.current;
          setWeather({
            temp: Math.round(cur.temperature_2m),
            code: cur.weathercode,
            wind: Math.round(cur.windspeed_10m),
            humidity: cur.relative_humidity_2m,
            city:
              geoData.address?.neighbourhood ||
              geoData.address?.suburb ||
              geoData.address?.village ||
              geoData.address?.town ||
              geoData.address?.city ||
              geoData.address?.municipality ||
              "Tu ubicación",
          });
          setStatus("ok");
        } catch {
          setStatus("error");
        }
      },
      () => setStatus("error")
    );
  }, []);

  if (status === "loading")
    return (
      <div className="weather-card weather-loading">
        <i className="fa-solid fa-spinner fa-spin"></i>
        <span>Obteniendo ubicación…</span>
      </div>
    );
  if (status === "error")
    return (
      <div className="weather-card weather-error">
        <i className="fa-solid fa-location-slash"></i>
        <span>Activa los permisos de ubicación.</span>
      </div>
    );

  const { desc, icon } = weatherInfo(weather.code);
  return (
    <div className="weather-card">
      <div className="weather-top">
        <div className="weather-left">
          <p className="weather-city">
            <i className="fa-solid fa-location-dot"></i> {weather.city}
          </p>
          <p className="weather-temp">{weather.temp}°C</p>
          <p className="weather-desc">{desc}</p>
        </div>
        <i className={`fa-solid ${icon} weather-icon`}></i>
      </div>
      <div className="weather-stats">
        <div className="weather-stat">
          <i className="fa-solid fa-wind"></i>
          <span>{weather.wind} km/h</span>
        </div>
        <div className="weather-stat">
          <i className="fa-solid fa-droplet"></i>
          <span>{weather.humidity}%</span>
        </div>
      </div>
    </div>
  );
}

export default function Dashboard() {
  const navigate = useNavigate();
  const now = new Date();

  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth());
  const [selected, setSel] = useState(now.getDate());
  const [events, setEvents] = useState({});

  const [currentUser, setCurrentUser] = useState(CURRENT_USER_FALLBACK);
  const [incidencias, setIncidencias] = useState([]);
  const [loadingData, setLoadingData] = useState(true);
  const [muroPosts, setMuroPosts] = useState([]);

  useEffect(() => {
    async function fetchData() {
      setLoadingData(true);
      try {
        const {
          data: { user },
        } = await supabase.auth.getUser();
        if (user) {
          const { data: profile } = await supabase
            .from("profiles")
            .select(
              "full_name, username, email, role, comunidad_id, comunidades(nombre)"
            )
            .eq("id", user.id)
            .single();

          if (profile) {
            setCurrentUser({
              name: profile.full_name || profile.username || user.email,
              role: profile.role || "vecino",
              community: profile.comunidades?.nombre || "",
            });
          }

          const { data: posts } = await supabase
            .from("muro_publicaciones")
            .select("id, titulo, contenido, autor_id, created_at")
            .order("created_at", { ascending: false })
            .limit(1);

          if (posts && posts.length > 0) {
            const post = posts[0];
            const { data: author } = await supabase
              .from("profiles")
              .select("full_name")
              .eq("id", post.autor_id)
              .single();
            setMuroPosts([
              { ...post, autor_name: author?.full_name || "Usuario" },
            ]);
          } else {
            setMuroPosts([]);
          }
        }

        const { data: incs } = await supabase
          .from("incidencias")
          .select("id, titulo, estado")
          .order("created_at", { ascending: false })
          .limit(3);

        if (incs) setIncidencias(incs);
      } catch (err) {
        console.error("Error cargando datos dashboard:", err);
      } finally {
        setLoadingData(false);
      }
    }
    fetchData();
  }, []);

  const isCurrentMonth = year === now.getFullYear() && month === now.getMonth();
  const cells = getDays(year, month);
  const selDay = new Date(year, month, selected);
  const dayEvents = events[selected] || [];

  function prevMonth() {
    if (month === 0) {
      setMonth(11);
      setYear((y) => y - 1);
    } else setMonth((m) => m - 1);
    setSel(1);
  }
  function nextMonth() {
    if (month === 11) {
      setMonth(0);
      setYear((y) => y + 1);
    } else setMonth((m) => m + 1);
    setSel(1);
  }
  if (loadingData) return <Cargando />;

  return (
    <div className="dashboard-page">
      {/* ── Bienvenida ── */}
      <div className="welcome-bar">
        <div className="welcome-left">
          <div className="welcome-avatar">
            {currentUser.name
              .split(" ")
              .map((n) => n[0])
              .join("")
              .slice(0, 2)
              .toUpperCase()}
          </div>
          <div>
            <p className="welcome-name">
              Bienvenido, {currentUser.name.split(" ")[0]}
            </p>
            <p className="welcome-meta">{currentUser.community}</p>
          </div>
        </div>
        <span className="welcome-role">
          {ROLE_LABELS[currentUser.role] || currentUser.role}
        </span>
      </div>

      {/* ── Grid 3 columnas ── */}
      <div className="dashboard-grid">
        {/* ══ COL 1: Calendario ══ */}
        <div className="cal-card">
          <div className="big-date-row">
            <div className="big-num">{String(selected).padStart(2, "0")}</div>
            <div className="big-info">
              <div className="big-day">{DAYS_FULL[selDay.getDay()]}</div>
              <div className="big-mon">
                {MONTHS_ES[month]} {year}
              </div>
            </div>
            <div className="nav-group">
              <button className="nav-btn" onClick={prevMonth}>
                &#8249;
              </button>
              <button className="nav-btn" onClick={nextMonth}>
                &#8250;
              </button>
            </div>
          </div>

          <div className="mini-grid">
            {DAYS_ES.map((d) => (
              <div key={d} className="day-name">
                {d}
              </div>
            ))}
            {cells.map((cell, i) => {
              const isToday =
                isCurrentMonth && !cell.other && cell.d === now.getDate();
              const isSel = !cell.other && cell.d === selected;
              const hasDot = !cell.other && events[cell.d]?.length > 0;
              let cls = "day-cell";
              if (cell.other) cls += " day-cell--other";
              if (isSel) cls += " day-cell--selected";
              else if (isToday) cls += " day-cell--today";
              if (hasDot) cls += " day-cell--dot";
              return (
                <div
                  key={i}
                  className={cls}
                  onClick={() => !cell.other && setSel(cell.d)}
                >
                  {cell.d}
                </div>
              );
            })}
          </div>

          <div className="cal-divider" />

          <div className="agenda-section">
            <p className="agenda-header">
              {dayEvents.length > 0
                ? `${dayEvents.length} evento${dayEvents.length > 1 ? "s" : ""}`
                : "Sin eventos"}
            </p>
            {dayEvents.length === 0 && (
              <p className="agenda-empty">
                No hay nada programado para este día.
              </p>
            )}
            {dayEvents.map((ev, i) => (
              <div key={i} className="agenda-item">
                <div
                  className="agenda-bar"
                  style={{
                    background: EVENT_COLORS[ev.type] || EVENT_COLORS.nota,
                  }}
                />
                <div>
                  <div className="agenda-time">{ev.time}</div>
                  <div className="agenda-title">{ev.title}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* ══ COL 2 FILA 1: Tiempo ══ */}
        <WeatherCard />

        {/* ══ COL 3 FILA 1: Accesos rápidos ══ */}
        <div className="dash-card dash-card-accesos">
          <div className="dash-card-header">
            <i className="fa-solid fa-bolt"></i>
            <span>Accesos rápidos</span>
          </div>
          <div className="accesos-grid">
            {ACCESOS.map((a) => (
              <button
                key={a.to}
                className="acceso-btn"
                onClick={() => navigate(a.to)}
                style={{ "--acceso-color": a.color }}
              >
                <i className={`fa-solid ${a.icon}`}></i>
                <span>{a.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* ══ COL 2 FILA 2: Últimas incidencias ══ */}
        <div className="dash-card dash-card-incidencias">
          <div className="dash-card-header">
            <i className="fa-solid fa-triangle-exclamation"></i>
            <span>Últimas incidencias</span>
          </div>
          <div className="dash-list">
            {incidencias.length === 0 && (
              <p
                style={{
                  fontSize: 12,
                  color: "var(--text-secondary)",
                  opacity: 0.6,
                }}
              >
                No hay incidencias registradas.
              </p>
            )}
            {incidencias.map((inc) => (
              <div key={inc.id} className="dash-list-item">
                <div
                  className="dash-list-bar"
                  style={{ background: ESTADO_COLORS[inc.estado] || "#a78bfa" }}
                />
                <span className="dash-list-title">{inc.titulo}</span>
                <span
                  className="dash-badge"
                  style={{
                    background: (ESTADO_COLORS[inc.estado] || "#a78bfa") + "22",
                    color: ESTADO_COLORS[inc.estado] || "#a78bfa",
                  }}
                >
                  {inc.estado}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* ══ COL 3 FILA 2: Último anuncio ══ */}
        {muroPosts.length > 0 ? (
          <div className="anuncio-card">
            <div className="anuncio-header">
              <i className="fa-solid fa-thumbtack"></i>
              <span>Último anuncio del muro</span>
            </div>
            <p className="anuncio-autor">
              {muroPosts[0].autor_name} ·{" "}
              {new Date(muroPosts[0].created_at).toLocaleDateString("es-ES", {
                day: "2-digit",
                month: "short",
                year: "numeric",
              })}
            </p>
            <p className="anuncio-titulo">{muroPosts[0].titulo}</p>
            <p className="anuncio-cuerpo">{muroPosts[0].contenido}</p>
          </div>
        ) : (
          <div className="anuncio-card">
            <div className="anuncio-header">
              <i className="fa-solid fa-thumbtack"></i>
              <span>Muro vacío</span>
            </div>
            <p className="anuncio-cuerpo">
              No hay anuncios publicados todavía.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
