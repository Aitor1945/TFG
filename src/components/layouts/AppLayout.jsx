import React from "react"
import { Outlet } from "react-router-dom"
import SideBar from "./SideBar"

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