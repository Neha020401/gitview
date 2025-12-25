import React from 'react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import './Files.css'

const Files = () => {
  const [previews, setPreviews] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isDarkMode, setIsDarkMode] = useState(true)

  // Apply theme class to document
  useEffect(() => {
    document.documentElement.className = isDarkMode ? "dark-theme" : "light-theme"
  }, [isDarkMode])

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode)
  }

  useEffect(() => {
    fetchPreviews()
  }, [])

  const fetchPreviews = async () => {
    setIsLoading(true)
    try {
      const res = await axios.get("http://localhost:8080/api/previews")
      setPreviews(res.data)
    } catch (err) {
      console.error("Error fetching previews", err)
    } finally {
      setIsLoading(false)
    }
  }

  const handleStop = async (branch) => {
    try {
      await axios.delete(`http://localhost:8080/api/previews/${branch}`)
      fetchPreviews()
    } catch (err) {
      alert("Failed to stop preview: " + err.message)
    }
  }

  return (
    <div className="files-page">
      <div className="files-container">
        {/* Header Section */}
        <div className="files-header">
          <div className="files-header-content">
            <Link to="/" className="back-link">
              <span>←</span> Back to Home
            </Link>
            <h1>📁 Cloned Repositories</h1>
            <button className="theme-toggle" onClick={toggleTheme}>
              {isDarkMode ? "☀️" : "🌙"}
            </button>
          </div>
          <p className="files-header-subtitle">View and manage your cloned repository files</p>
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

                  <div className="preview-actions">
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
                    <button className="stop-btn" onClick={() => handleStop(preview.branchName)}>
                      ⏹ Stop
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Files