document.addEventListener('DOMContentLoaded', function() {
    
    // --- 1. LÓGICA DE TEMA Y COOKIES ---
    const themeToggleBtn = document.getElementById('themeToggle');
    const body = document.body;
    const icon = themeToggleBtn.querySelector('i');

    // Función para crear una cookie
    function setCookie(cname, cvalue, exdays) {
        const d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        let expires = "expires="+ d.toUTCString();
        document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
    }

    // Función para leer una cookie
    function getCookie(cname) {
        let name = cname + "=";
        let decodedCookie = decodeURIComponent(document.cookie);
        let ca = decodedCookie.split(';');
        for(let i = 0; i <ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) == ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) == 0) {
                return c.substring(name.length, c.length);
            }
        }
        return "";
    }

    // Función para actualizar el icono
    function updateIcon(isDark) {
        if (isDark) {
            icon.classList.remove('fa-sun');
            icon.classList.add('fa-moon');
        } else {
            icon.classList.remove('fa-moon');
            icon.classList.add('fa-sun');
        }
    }

    // Comprobar cookie al cargar
    const savedTheme = getCookie("barriored_theme");
    
    if (savedTheme === "dark") {
        body.classList.remove('light-mode');
        body.classList.add('dark-mode');
        updateIcon(true);
    } else {
        body.classList.add('light-mode'); // Por defecto
        updateIcon(false);
    }

    // Evento Click en el botón
    themeToggleBtn.addEventListener('click', () => {
        body.classList.toggle('dark-mode');
        body.classList.toggle('light-mode');
        
        // Comprobar estado actual tras el click
        const isDark = body.classList.contains('dark-mode');
        
        // Actualizar icono
        updateIcon(isDark);
        
        // Guardar en cookie (30 días)
        setCookie("barriored_theme", isDark ? "dark" : "light", 30);
    });

    // --- 2. LÓGICA DEL FORMULARIO (EXISTENTE) ---
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('passwordInput');

    togglePassword.addEventListener('click', function () {
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        this.querySelector('i').classList.toggle('fa-eye');
        this.querySelector('i').classList.toggle('fa-eye-slash');
    });

    const form = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');

    form.addEventListener('submit', function(event) {
        event.preventDefault();
        
        if (!form.checkValidity()) {
            event.stopPropagation();
            form.classList.add('was-validated');
            return;
        }

        const email = document.getElementById('emailInput').value;
        const originalBtnText = loginBtn.innerHTML;
        loginBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Validando...';
        loginBtn.disabled = true;

        setTimeout(() => {
            console.log("Login intentado con:", email);
            loginBtn.innerHTML = '<i class="fa-solid fa-check"></i> Acceso Correcto';
            loginBtn.classList.remove('btn-primary');
            loginBtn.classList.add('btn-success');
            
            setTimeout(() => {
               // Aquí redirigirías
               alert("Redirigiendo al Dashboard...");
            }, 500);
        }, 1500);
    });
});