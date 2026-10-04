import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

// "Recuérdame": con la casilla marcada la sesión se guarda en localStorage
// (sigue abierta al cerrar el navegador); sin marcar, en sessionStorage
// (se cierra al cerrar la pestaña o el navegador).
const CLAVE_RECORDAR = 'br-recordar'

export const getRecordarSesion = () => localStorage.getItem(CLAVE_RECORDAR) !== 'false'
export const setRecordarSesion = (recordar) =>
  localStorage.setItem(CLAVE_RECORDAR, String(recordar))

const almacenSesion = {
  getItem: (clave) => sessionStorage.getItem(clave) ?? localStorage.getItem(clave),
  setItem: (clave, valor) => {
    if (getRecordarSesion()) {
      localStorage.setItem(clave, valor)
      sessionStorage.removeItem(clave)
    } else {
      sessionStorage.setItem(clave, valor)
      localStorage.removeItem(clave)
    }
  },
  removeItem: (clave) => {
    localStorage.removeItem(clave)
    sessionStorage.removeItem(clave)
  },
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: { storage: almacenSesion },
})
