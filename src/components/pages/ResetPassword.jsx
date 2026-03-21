import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "../../lib/supabase";
import "./login.css"; // reutilizamos el mismo CSS del login

export default function ResetPassword() {
  const navigate = useNavigate();

  const [password, setPassword]           = useState("");
  const [confirmar, setConfirmar]         = useState("");
  const [showPass, setShowPass]           = useState(false);
  const [showConfirm, setShowConfirm]     = useState(false);
  const [cargando, setCargando]           = useState(false);
  const [error, setError]                 = useState("");
  const [exito, setExito]                 = useState(false);
  const [sesionLista, setSesionLista]     = useState(false);
  const [theme, setTheme]                 = useState("light");

  // Supabase manda el token en el hash de la URL (#access_token=...)
  // onAuthStateChange lo detecta automáticamente y crea la sesión
  useEffect(() => {
    const { data: listener } = supabase.auth.onAuthStateChange((event, session) => {
      if (event === "PASSWORD_RECOVERY" && session) {
        setSesionLista(true);
      }
    });
    return () => listener.subscription.unsubscribe();
  }, []);

  // Tema
  useEffect(() => {
    document.body.classList.toggle("light-mode", theme === "light");
    document.body.classList.toggle("dark-mode",  theme === "dark");
    return () => document.body.classList.remove("light-mode", "dark-mode");
  }, [theme]);

  const isValida    = password.trim().length >= 6;
  const coincide    = password === confirmar;

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!isValida) {
      setError("La contraseña debe tener al menos 6 caracteres.");
      return;
    }
    if (!coincide) {
      setError("Las contraseñas no coinciden.");
      return;
    }

    setCargando(true);

    const { error: err } = await supabase.auth.updateUser({
      password: password.trim()
    });

    setCargando(false);

    if (err) {
      setError("Error al actualizar: " + err.message);
      return;
    }

    setExito(true);
    // Redirigir al login después de 3 segundos
    setTimeout(() => navigate("/login"), 3000);
  };

  return (
    <div>
      {/* Botón tema */}
      <button
        className="theme-btn shadow-sm"
        onClick={() => setTheme(t => t === "light" ? "dark" : "light")}
        type="button"
      >
        <i className={`fa-solid ${theme === "light" ? "fa-sun" : "fa-moon"}`} />
      </button>

      <div className="animated-bg" />

      <div className="container-fluid min-vh-100 d-flex align-items-center justify-content-center position-fixed z-1">
        <div className="login-wrapper shadow-lg">
          <div className="row g-0 h-100">

            {/* Branding */}
            <div className="col-md-6 d-none d-md-flex flex-column justify-content-center align-items-center bg-branding text-white p-5">
              <div className="text-center">
                <img src="/BarrioRedLogo.png" alt="Logo" className="app-logo mb-3" />
                <h2 className="fw-bold brand-title">BARRIORED</h2>
                <p className="brand-subtitle">JUNTOS EN CONEXIÓN</p>
                <hr className="my-4 border-white opacity-50 w-50 mx-auto" />
                <p className="small opacity-75 mt-3">Plataforma de gestión integral.</p>
              </div>
            </div>

            {/* Formulario */}
            <div className="col-md-6 form-side p-5 d-flex flex-column justify-content-center">

              {/* Token no recibido aún */}
              {!sesionLista && !exito && (
                <div className="text-center">
                  <div style={{ fontSize: "2.5rem", marginBottom: 12 }}>⏳</div>
                  <h5 className="fw-bold">Verificando enlace...</h5>
                  <p className="text-adaptive small">
                    Si llevas más de 10 segundos aquí, el enlace puede haber expirado.{" "}
                    <button
                      type="button"
                      className="btn btn-link p-0 small fw-bold text-decoration-none text-primary"
                      onClick={() => navigate("/login")}
                    >
                      Volver al login
                    </button>
                  </p>
                </div>
              )}

              {/* Formulario nueva contraseña */}
              {sesionLista && !exito && (
                <>
                  <div className="text-start mb-4">
                    <h3 className="fw-bold">Nueva contraseña</h3>
                    <p className="text-adaptive small">
                      Elige una contraseña segura de al menos 6 caracteres.
                    </p>
                  </div>

                  <form noValidate onSubmit={onSubmit}>

                    {/* Nueva contraseña */}
                    <div className="form-floating mb-3 position-relative">
                      <input
                        type={showPass ? "text" : "password"}
                        className={`form-control ${error && !isValida ? "is-invalid" : ""}`}
                        id="newPassword"
                        placeholder="Nueva contraseña"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                      />
                      <label htmlFor="newPassword">Nueva contraseña</label>
                      <button
                        type="button"
                        className="password-toggle"
                        onClick={() => setShowPass(s => !s)}
                      >
                        <i className={`fa-regular ${showPass ? "fa-eye-slash" : "fa-eye"}`} />
                      </button>
                    </div>

                    {/* Confirmar contraseña */}
                    <div className="form-floating mb-4 position-relative">
                      <input
                        type={showConfirm ? "text" : "password"}
                        className={`form-control ${error && !coincide ? "is-invalid" : ""}`}
                        id="confirmPassword"
                        placeholder="Confirmar contraseña"
                        value={confirmar}
                        onChange={(e) => setConfirmar(e.target.value)}
                        required
                      />
                      <label htmlFor="confirmPassword">Confirmar contraseña</label>
                      <button
                        type="button"
                        className="password-toggle"
                        onClick={() => setShowConfirm(s => !s)}
                      >
                        <i className={`fa-regular ${showConfirm ? "fa-eye-slash" : "fa-eye"}`} />
                      </button>
                    </div>

                    {/* Error */}
                    {error && (
                      <div className="alert alert-danger d-flex align-items-center mb-3 p-2 small" role="alert">
                        <i className="fa-solid fa-triangle-exclamation me-2" />
                        {error}
                      </div>
                    )}

                    <div className="d-grid">
                      <button
                        type="submit"
                        className="btn btn-primary btn-lg custom-btn"
                        disabled={cargando}
                      >
                        {cargando
                          ? <><i className="fa-solid fa-spinner fa-spin me-2" />Guardando...</>
                          : "Guardar nueva contraseña"
                        }
                      </button>
                    </div>

                  </form>
                </>
              )}

              {/* Éxito */}
              {exito && (
                <div className="text-center recovery-success">
                  <div className="success-icon mb-3">
                    <i className="fa-solid fa-circle-check" />
                  </div>
                  <h4 className="fw-bold mb-2">¡Contraseña actualizada!</h4>
                  <p className="text-adaptive small">
                    Redirigiendo al login en unos segundos...
                  </p>
                  <div className="success-bar-wrap mt-4">
                    <div className="success-bar" />
                  </div>
                </div>
              )}

            </div>
          </div>
        </div>
      </div>
    </div>
  );
}