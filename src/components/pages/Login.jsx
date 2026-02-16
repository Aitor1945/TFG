import React, { useEffect, useMemo, useState } from "react";
import "./login.css";


export default function Login() {
  const [theme, setTheme] = useState("light"); // "light" | "dark"
  const [showPassword, setShowPassword] = useState(false);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [remember, setRemember] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  // Aplica clase al body como en tu HTML original
  useEffect(() => {
    document.body.classList.toggle("light-mode", theme === "light");
    document.body.classList.toggle("dark-mode", theme === "dark");
    return () => {
      document.body.classList.remove("light-mode", "dark-mode");
    };
  }, [theme]);

  const isEmailValid = useMemo(() => {
    // validación simple (puedes mejorarla)
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  }, [email]);

  const isPasswordValid = useMemo(() => password.trim().length > 0, [password]);

  const onSubmit = (e) => {
    e.preventDefault();
    setSubmitted(true);

    if (!isEmailValid || !isPasswordValid) return;

    // Aquí llamarías a tu API
    // login({ email, password, remember })
    console.log("Login OK", { email, password, remember });
  };

  return (
    <div>
      {/* Botón tema */}
      <button
        id="themeToggle"
        className="theme-btn shadow-sm"
        aria-label="Cambiar tema"
        onClick={() => setTheme((t) => (t === "light" ? "dark" : "light"))}
        type="button"
      >
        <i className={`fa-solid ${theme === "light" ? "fa-sun" : "fa-moon"}`}></i>
      </button>

      <div className="animated-bg" />

      <div className="container-fluid min-vh-100 d-flex align-items-center justify-content-center position-fixed z-1">
        <div className="login-wrapper shadow-lg">
          <div className="row g-0 h-100">
            {/* Branding */}
            <div className="col-md-6 d-none d-md-flex flex-column justify-content-center align-items-center bg-branding text-white p-5">
              <div className="text-center content-branding">
                <img
                  src="./public/BarrioRedLogo.png"
                  alt="Logo BarrioRed"
                  className="app-logo mb-3"
                />

                <h2 className="fw-bold brand-title">BARRIORED</h2>
                <p className="brand-subtitle ls-2">JUNTOS EN CONEXIÓN</p>
                <hr className="my-4 border-white opacity-50 w-50 mx-auto" />
                <p className="small opacity-75 mt-3">
                  Plataforma de gestión integral.
                </p>
              </div>
            </div>

            {/* Form */}
            <div className="col-md-6 form-side p-5 d-flex flex-column justify-content-center">
              <div className="text-start mb-4">
                <h3 className="fw-bold welcome-text">Bienvenido</h3>
                <p className="text-adaptive small">
                  Introduce tus credenciales facilitadas por la administración.
                </p>
              </div>

              <form id="loginForm" noValidate onSubmit={onSubmit}>
                <div className="form-floating mb-3">
                  <input
                    type="email"
                    className={`form-control ${
                      submitted && !isEmailValid ? "is-invalid" : ""
                    }`}
                    id="emailInput"
                    placeholder="nombre@ejemplo.com"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                  <label htmlFor="emailInput">Correo Electrónico</label>
                  <div className="invalid-feedback">Introduce un correo válido.</div>
                </div>

                <div className="form-floating mb-3 position-relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    className={`form-control ${
                      submitted && !isPasswordValid ? "is-invalid" : ""
                    }`}
                    id="passwordInput"
                    placeholder="Contraseña"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <label htmlFor="passwordInput">Contraseña</label>

                  <button
                    className="password-toggle"
                    id="togglePassword"
                    type="button"
                    onClick={() => setShowPassword((s) => !s)}
                    aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                    title={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                  >
                    <i className={`fa-regular ${showPassword ? "fa-eye-slash" : "fa-eye"}`} />
                  </button>

                  <div className="invalid-feedback">Introduce tu contraseña.</div>
                </div>

                <div className="d-flex justify-content-between align-items-center mb-4">
                  <div className="form-check">
                    <input
                      className="form-check-input"
                      type="checkbox"
                      id="rememberMe"
                      checked={remember}
                      onChange={(e) => setRemember(e.target.checked)}
                    />
                    <label className="form-check-label small text-adaptive" htmlFor="rememberMe">
                      Recuérdame
                    </label>
                  </div>

                  <a href="#" className={`text-decoration-none small ${theme === "light" ? "text-primary" : "text-info"} fw-bold`}>
                    ¿Olvidaste tu contraseña?
                  </a>
                </div>

                <div className="d-grid gap-2">
                  <button
                    type="submit"
                    className="btn btn-primary btn-lg custom-btn"
                    id="loginBtn"
                  >
                    Acceder
                  </button>
                </div>
              </form>

              <div className="mt-5 text-center">
                <div
                  className="alert alert-secondary d-flex align-items-center p-2 small text-muted"
                  role="alert"
                >
                  <i className="fa-solid fa-circle-info me-2" />
                  <div>Contacta con tu administrador para recibir tus claves.</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
