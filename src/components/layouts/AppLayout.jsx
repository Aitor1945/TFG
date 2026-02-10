import React from "react"
import { Outlet } from "react-router-dom"
import SideBar from "./SideBar" // está en la misma carpeta según tu captura

export default function AppLayout() {
  return (
    <div className="app">
      <SideBar />
      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
