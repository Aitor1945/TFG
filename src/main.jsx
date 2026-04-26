import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

const savedFont = localStorage.getItem("br-fontsize");
if (savedFont) {
  const map = { pequeño: "14px", normal: "16px", grande: "18px" };
  const size = map[savedFont] || "16px";
  document.documentElement.style.fontSize = size;
  document.documentElement.style.setProperty("--app-font-size", size);
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
