"use client"
import { Routes, Route } from "react-router-dom"
import Home from "./Components/Home"
import Files from "./Components/Files"
import "./App.css"

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/dir" element={<Files />} />
    </Routes>
  )
}

export default App