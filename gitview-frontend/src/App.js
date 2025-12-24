"use client"
import { useState, useEffect } from "react"
import { Routes, Route } from "react-router-dom"
import Home from "./Components/Home"
import Files from "./Components/Files"
import axios from "axios"
import "./App.css"

function App() {
  // const [repoUrl, setRepoUrl] = useState("")
  // const [branchName, setBranchName] = useState("")
  // const [previews, setPreviews] = useState([])
  // const [isLoading, setIsLoading] = useState(false)
  // const [isDarkMode, setIsDarkMode] = useState(false)
  // const [baseDir, setBaseDir] = useState("")


  // useEffect(() => {
  //   fetchPreviews()
  // }, [])


  // const fetchPreviews = async () => {
  //   try {
  //     const res = await axios.get("http://localhost:8080/api/previews")
  //     setPreviews(res.data)
  //   } catch (err) {
  //     console.error("Error fetching previews", err)
  //   }
  // }



  //  const handleStop = async (branch) => {
  //   try {
  //     await axios.delete(`http://localhost:8080/api/previews/${branch}`)
  //     fetchPreviews()
  //   } catch (err) {
  //     alert("Failed to stop preview: " + err.message)
  //   }
  // }



  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/dir" element={<Files />} />
    </Routes>
  )
}

export default App