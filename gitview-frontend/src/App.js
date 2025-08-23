"use client"

import { useState, useEffect } from "react"
import axios from "axios"
import "./App.css"

function App() {
  const [repoUrl, setRepoUrl] = useState("")
  const [branchName, setBranchName] = useState("")
  const [previews, setPreviews] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [isDarkMode, setIsDarkMode] = useState(false)
  const [baseDir, setBaseDir] = useState("")


  // Fetch previews on load
  useEffect(() => {
    fetchPreviews()
  }, [])

  useEffect(() => {
    document.documentElement.className = isDarkMode ? "dark-theme" : "light-theme"
  }, [isDarkMode])

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
  params: { repoUrl, branchName, baseDir },
})
      fetchPreviews() // refresh list
      setRepoUrl("")
      setBranchName("")
    } catch (err) {
      alert("Failed to add repo: " + err.message)
    } finally {
      setIsLoading(false)
    }
  }

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode)
  }

  return (
    <div className="app-container">
      <div className="container">
        <div className="header">
          <div className="header-content">
            <div className="logo-section">
              <div className="logo-icon">
                <span>🚀</span>
              </div>
              <h1>Git Branch Previewer</h1>
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
            </div>
          </form>
        </div>

        {/* Previews Section */}
        <div className="previews-card">
          <h2 className="section-title">
            <span className="section-icon preview-icon">👁</span>
            Available Previews
            <span className="preview-count">
              {previews.length} {previews.length === 1 ? "preview" : "previews"}
            </span>
          </h2>

          {previews.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <h3>No previews yet</h3>
              <p>Add your first repository to get started</p>
            </div>
          ) : (
            <div className="previews-grid">
              {previews.map((preview, index) => (
                <div key={index} className="preview-item">
                  <div className="preview-info">
                    <div className="preview-avatar">{preview.branchName.charAt(0).toUpperCase()}</div>
                    <div className="preview-details">
                      <h3>{preview.branchName}</h3>
                      <p>Branch preview</p>
                    </div>
                  </div>

                  <a href={preview.url} target="_blank" rel="noreferrer" className="preview-link">
                    <span>View Preview</span>
                    <svg className="external-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                      />
                    </svg>
                  </a>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default App
