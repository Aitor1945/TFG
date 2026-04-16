import { useEffect, useState } from "react";
//Archivo global para el uso de darkmode en la web entera
export function useTheme() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem("theme") || "dark";
  });

  useEffect(() => {
    localStorage.setItem("theme", theme);

    document.body.classList.remove("light-mode", "dark-mode");
    document.body.classList.add(`${theme}-mode`);
  }, [theme]);

  return [theme, setTheme];
}
