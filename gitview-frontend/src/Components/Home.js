import React from 'react'
import axios from 'axios'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './Home.css'

const Home = () => {
  const [repoUrl, setRepoUrl] = useState("")
  const [branchName, setBranchName] = useState("")
  const [previews, setPreviews] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [isDarkMode, setIsDarkMode] = useState(true)
  const [baseDir, setBaseDir] = useState("")


  useEffect(() => {
    document.documentElement.className = isDarkMode ? "dark-theme" : "light-theme"
  }, [isDarkMode])


  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode)
  }


  const fetchPreviews = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/previews")
      setPreviews(res.data)
    } catch (err) {
      console.error("Error fetching previews", err)
    }
  }


  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)
    try {
      await axios.post("http://localhost:8080/api/repos", null, {
        params: {
          repoUrl,
          branchName,
          baseDir: baseDir.trim() === "" ? undefined : baseDir,
        },
      })
      fetchPreviews()
      setRepoUrl("")
      setBranchName("")
    } catch (err) {
      if (err.response && err.response.data && err.response.data.error) {
        alert("Failed to add repo: " + err.response.data.error);
      } else {
        alert("Failed to add repo: " + err.message);
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div><div className="app-container">
      <div className="container">
        <div className="header">
          <div className="header-content">
            <div className="logo-section">
              <div className="logo-icon">
                <span>🚀</span>
              </div>
              <h1>Git Branch Previewer </h1>
            </div>
            <button className="theme-toggle" onClick={toggleTheme}>
              {isDarkMode ? "☀️" : "🌙"}
            </button>
          </div>
          <p className="header-subtitle">Deploy and preview your Git branches instantly</p>
        </div>

        {/* Add Repository Form */}
        <div className="form-card">
          <h2 className="section-title">
            <span className="section-icon add-icon">+</span>
            Add New Repository
          </h2>

          <form onSubmit={handleSubmit} className="repo-form">
            <div className="form-grid">
              <div className="form-group">
                <label>Repository URL</label>
                <input
                  type="text"
                  value={repoUrl}
                  onChange={(e) => setRepoUrl(e.target.value)}
                  placeholder="https://github.com/username/repo.git"
                  required
                />
              </div>
              <div className="form-group">
                <label>Branch Name</label>
                <input
                  type="text"
                  value={branchName}
                  onChange={(e) => setBranchName(e.target.value)}
                  placeholder="main, develop, feature/new-ui"
                  required
                />
              </div>
              <div className="form-group">
                <label>Base Directory</label>
                <input
                  type="text"
                  value={baseDir}
                  onChange={(e) => setBaseDir(e.target.value)}
                  placeholder="/tmp/gitviewer/"
                  required
                />
              </div>

            </div>

            <div className="form-actions">
              <button type="submit" disabled={isLoading} className="submit-btn">
                {isLoading ? (
                  <>
                    <div className="spinner"></div>
                    Adding...
                  </>
                ) : (
                  <>
                    <span>+</span>
                    Add Repository
                  </>
                )}
              </button>
              <Link to="/dir" className="view-files-btn">
                📁 View Cloned Files
              </Link>
            </div>
          </form>
        </div>

      </div>
    </div>
    </div>
  )
}

export default Home