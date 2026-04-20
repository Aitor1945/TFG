import { useNavigate } from "react-router-dom";
import "./LandingPage.css";

export default function LandingPage() {
  const navigate = useNavigate();

  const irLogin = () => {
    navigate("/login");
  };

  return (
    <div>
      <nav>
        <a href="#" className="logo-h">
          BarrioRed
        </a>

        <div className="nav-actions">
          <button className="btn-solid" onClick={irLogin}>
            Ir a la plataforma
          </button>
        </div>
      </nav>

      <section className="introduccion">
        <div className="intro-container">
          <h2 className="intro-title">
            Bienvenido a <span>BarrioRed</span>
          </h2>

          <p className="intro-text">
            BarrioRed es una plataforma digital diseñada para modernizar la
            gestión de comunidades de vecinos, centralizando la comunicación,
            incidencias y documentación en un único entorno accesible y seguro.
          </p>

          <div className="intro-actions">
            <a href="#proyecto" className="intro-btn">
              Ver proyecto
            </a>
            <a href="#objetivos" className="intro-btn-outline">
              Objetivos
            </a>
          </div>
        </div>
      </section>
    </div>
  );
}
