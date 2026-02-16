import React, { useState } from "react"
import { Outlet } from "react-router-dom"
import SideBar from "./SideBar"

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <div className="app">
      <SideBar collapsed={collapsed} setCollapsed={setCollapsed} />
      <main className="main" style={{ marginLeft: collapsed ? "68px" : "240px", transition: "margin-left 0.3s ease" }}>
        <Outlet />
      </main>
    </div>
  )
}