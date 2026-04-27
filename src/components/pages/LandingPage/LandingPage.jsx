import { useNavigate } from "react-router-dom";
import "./LandingPage.css";
import aitorFoto from "../../../assets/AitorFoto.png";
import felixFoto from "../../../assets/FelixFoto.png";
import joelFoto from "../../../assets/JoelFoto.png";

const features = [
  {
    icon: "🔧",
    name: "Gestión de incidencias",
    desc: "Reporta problemas de la comunidad con descripción detallada y seguimiento hasta su resolución completa.",
  },
  {
    icon: "💬",
    name: "Chat interno",
    desc: "Mensajería directa entre miembros de la comunidad, mejorando la interacción y reduciendo el uso de canales externos.",
  },
  {
    icon: "📢",
    name: "Avisos comunitarios",
    desc: "Espacio para publicar avisos y noticias sobre la comunidad, disponible para usuarios con permisos asignados.",
  },
  {
    icon: "📁",
    name: "Gestión de documentación",
    desc: "Almacena, consulta y organiza actas, normas y comunicados con control de acceso según el rol del usuario.",
  },
];

const team = [
  {
    initial: "A",
    name: "Aitor García",
    role: "Desarrollador Full Stack",
    bio: "Desarrollador Full Stack especializado en frontend y backend, centrado en crear soluciones escalables y eficientes.",
    linkedin: "#",
    github: "#",
    photo: aitorFoto,
  },
  {
    initial: "B",
    name: "Felix Dominguez",
    role: "Desarrollador Frontend",
    bio: "Desarrollador Frontend enfocado en interfaces modernas, experiencia de usuario y componentes reutilizables.",
    linkedin: "#",
    github: "#",
    photo: felixFoto,
  },
  {
    initial: "C",
    name: "Joel Sánchez",
    role: "Desarrollador Backend",
    bio: "Desarrollador Backend especializado en bases de datos e integración entre frontend y backend.",
    linkedin: "#",
    github: "https://github.com/Joeel06",
    photo: joelFoto,
  },
];

const LinkedInIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
    <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.064 2.064 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
  </svg>
);

const GitHubIcon = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12c0-6.63-5.37-12-12-12" />
  </svg>
);

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div>
      {/* NAVBAR */}
      <nav className="navbar">
        <a href="#" className="logo-h">
          BarrioRed
        </a>
        <ul className="nav-links">
          <li>
            <a href="#proyecto">Proyecto</a>
          </li>
          <li>
            <a href="#funcionalidades">Funcionalidades</a>
          </li>
          <li>
            <a href="#equipo">Equipo</a>
          </li>
        </ul>
        <button className="btn-solid" onClick={() => navigate("/login")}>
          Ir a la plataforma
        </button>
      </nav>

      {/* HERO */}
      <section className="hero" id="inicio">
        <div className="hero-bg" />
        <div className="hero-grid" />
        <div className="hero-content">
          <div className="badge">Plataforma de gestión vecinal</div>
          <h1 className="hero-title">
            Bienvenido a <span>BarrioRed</span>
          </h1>
          <p className="hero-sub">
            BarrioRed moderniza la gestión de comunidades de vecinos,
            centralizando comunicación, incidencias y documentación en un único
            entorno accesible y seguro.
          </p>
          <div className="hero-actions">
            <a href="#proyecto" className="btn-primary">
              Ver proyecto
            </a>
            <a href="#funcionalidades" className="btn-outline-hero">
              Funcionalidades
            </a>
          </div>
        </div>
      </section>

      {/* PROYECTO — nuevo diseño: texto centrado + 3 pilares */}
      <section id="proyecto" className="section-proyecto">
        <div className="section-inner">
          <p className="section-label">Sobre el proyecto</p>
          <h2 className="section-title">
            Una plataforma pensada
            <br />
            para las personas
          </h2>
          <div className="divider" />
          <p className="proyecto-intro">
            BarrioRed nace para digitalizar la gestión de comunidades de
            vecinos: un espacio único donde vecinos, administradores y
            ayuntamientos pueden comunicarse, resolver incidencias y acceder a
            la documentación de forma sencilla y segura.
          </p>

          <div className="proyecto-pilares">
            <div className="pilar-card">
              <div className="pilar-icon">🏘️</div>
              <h3 className="pilar-title">Comunidad</h3>
              <p className="pilar-desc">
                Conecta a vecinos, administradores y ayuntamientos en un mismo
                entorno digital colaborativo.
              </p>
            </div>
            <div className="pilar-card">
              <div className="pilar-icon">⚡</div>
              <h3 className="pilar-title">Eficiencia</h3>
              <p className="pilar-desc">
                Centraliza gestión, comunicación y documentación para eliminar
                trámites lentos y canales dispersos.
              </p>
            </div>
            <div className="pilar-card">
              <div className="pilar-icon">🔒</div>
              <h3 className="pilar-title">Seguridad</h3>
              <p className="pilar-desc">
                Control de acceso por roles: cada usuario ve y hace exactamente
                lo que le corresponde.
              </p>
            </div>
          </div>

          <div className="proyecto-footer-row">
            <a
              href="https://github.com/Aitor1945/TFG"
              target="_blank"
              rel="noopener noreferrer"
              className="btn-github"
            >
              <GitHubIcon /> Ver en GitHub
            </a>
          </div>
        </div>
      </section>

      {/* FUNCIONALIDADES */}
      <section id="funcionalidades" className="section-features">
        <div className="section-inner">
          <p className="section-label">Funcionalidades</p>
          <h2 className="section-title">
            Todo lo que necesita
            <br />
            tu comunidad
          </h2>
          <div className="divider" />
          <p className="section-sub">
            Herramientas diseñadas para simplificar la convivencia y la
            administración vecinal en un solo lugar.
          </p>
          <div className="features-grid">
            {features.map((f) => (
              <div className="feature-card" key={f.name}>
                <div className="feature-icon">{f.icon}</div>
                <div className="feature-name">{f.name}</div>
                <div className="feature-desc">{f.desc}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* EQUIPO */}
      <section id="equipo" className="section-team">
        <div className="section-inner">
          <p className="section-label">El equipo</p>
          <h2 className="section-title">Quiénes somos</h2>
          <div className="divider" />
          <p className="section-sub">
            Somos tres desarrolladores apasionados por crear soluciones
            digitales que mejoran la vida en comunidad.
          </p>
          <div className="team-grid">
            {team.map((m) => (
              <div className="team-card" key={m.name}>
                <div className="team-avatar">
                  {m.photo ? <img src={m.photo} alt={m.name} /> : m.initial}
                </div>
                <div className="team-name">{m.name}</div>
                <div className="team-role">{m.role}</div>
                <div className="team-bio">{m.bio}</div>
                <div className="team-links">
                  <a
                    href={m.linkedin}
                    className="team-link"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <LinkedInIcon /> LinkedIn
                  </a>
                  <a
                    href={m.github}
                    className="team-link"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <GitHubIcon /> GitHub
                  </a>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="footer">
        <p>
          © 2025 <span className="footer-brand">BarrioRed</span> — Proyecto de
          gestión de comunidades de vecinos
        </p>
        <p className="footer-sub">Desarrollado por el equipo BarrioRed</p>
      </footer>
    </div>
  );
}
