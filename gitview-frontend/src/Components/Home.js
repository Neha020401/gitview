/**
 * ============================================================================
 * Home.js - Repository Clone Form Component
 * ============================================================================
 * 
 * PURPOSE:
 * Main landing page where users can clone Git repositories.
 * Shows a form to input repo URL, branch name, and clone destination.
 * 
 * FEATURES:
 * - Clone repository form with validation
 * - Default branch: "main" (if user doesn't specify)
 * - Default path: User's Downloads folder (if user doesn't specify)
 * - Success popup showing cloned path
 * - Dark/Light theme toggle
 * 
 * ============================================================================
 */

import React from 'react'
import axios from 'axios'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './Home.css'

const Home = () => {
  // ========================================
  // STATE VARIABLES
  // ========================================

  // Form input values
  const [repoUrl, setRepoUrl] = useState("")
  const [branchName, setBranchName] = useState("")  // Default "main" applied on submit
  const [baseDir, setBaseDir] = useState("")        // Default Downloads folder applied on submit

  // UI state
  const [previews, setPreviews] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [isDarkMode, setIsDarkMode] = useState(true)

  // Success popup state
  const [showSuccessPopup, setShowSuccessPopup] = useState(false)
  const [cloneResult, setCloneResult] = useState(null)  // Stores {branchName, clonedPath, techStack}

  // ========================================
  // THEME MANAGEMENT
  // ========================================

  useEffect(() => {
    document.documentElement.className = isDarkMode ? "dark-theme" : "light-theme"
  }, [isDarkMode])

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode)
  }

  // ========================================
  // DATA FETCHING
  // ========================================

  const fetchPreviews = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/previews")
      setPreviews(res.data)
    } catch (err) {
      console.error("Error fetching previews", err)
    }
  }

  // ========================================
  // FORM SUBMISSION
  // ========================================

  /**
   * Handle form submission - clone the repository
   * 
   * - Uses "main" as default branch if not specified
   * - Uses user's Downloads folder as default path if not specified
   * - Shows success popup with cloned path on completion
   */
  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    // Apply defaults if user didn't specify
    const finalBranch = branchName.trim() === "" ? "main" : branchName.trim()
    // Note: Empty baseDir is handled by backend to use Downloads folder

    try {
      const res = await axios.post("http://localhost:8080/api/repos", null, {
        params: {
          repoUrl,
          branchName: finalBranch,
          baseDir: baseDir.trim() === "" ? undefined : baseDir.trim(),
        },
      })

      // Store result for success popup
      setCloneResult({
        branchName: res.data.branchName,
        clonedPath: res.data.clonedPath,
        techStack: res.data.techStack
      })
      setShowSuccessPopup(true)

      fetchPreviews()
      // Clear form
      setRepoUrl("")
      setBranchName("")
      setBaseDir("")

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

  // ========================================
  // RENDER
  // ========================================

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

        {/* ===== Success Popup Modal ===== */}
        {showSuccessPopup && cloneResult && (
          <div className="success-popup-overlay" onClick={() => setShowSuccessPopup(false)}>
            <div className="success-popup" onClick={(e) => e.stopPropagation()}>
              <div className="success-icon">✅</div>
              <h3>Repository Cloned Successfully!</h3>

              <div className="clone-details">
                <div className="detail-row">
                  <span className="detail-label">Branch:</span>
                  <span className="detail-value">{cloneResult.branchName}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Cloned to:</span>
                  <span className="detail-value path-value">{cloneResult.clonedPath}</span>
                </div>
                {cloneResult.techStack && (
                  <div className="detail-row">
                    <span className="detail-label">Tech Stack:</span>
                    <span className="detail-value">
                      {cloneResult.techStack.icon} {cloneResult.techStack.displayName}
                    </span>
                  </div>
                )}
              </div>

              <div className="success-actions">
                <button className="success-close-btn" onClick={() => setShowSuccessPopup(false)}>
                  Close
                </button>
                <Link to="/dir" className="success-view-btn" onClick={() => setShowSuccessPopup(false)}>
                  📁 View Projects
                </Link>
              </div>
            </div>
          </div>
        )}

        {/* Add Repository Form */}
        <div className="form-card">
          <h2 className="section-title">
            <span className="section-icon add-icon">+</span>
            Add New Repository
          </h2>

          <form onSubmit={handleSubmit} className="repo-form">
            <div className="form-grid">
              <div className="form-group">
                <label>Repository URL <span className="required">*</span></label>
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
                  placeholder="main (default)"
                />
                <span className="form-hint">Leave empty for 'main' branch</span>
              </div>
              <div className="form-group">
                <label>Clone Directory</label>
                <input
                  type="text"
                  value={baseDir}
                  onChange={(e) => setBaseDir(e.target.value)}
                  placeholder="Downloads folder (default)"
                />
                <span className="form-hint">Leave empty for Downloads folder</span>
              </div>

            </div>

            <div className="form-actions">
              <button type="submit" disabled={isLoading} className="submit-btn">
                {isLoading ? (
                  <>
                    <div className="spinner"></div>
                    Cloning...
                  </>
                ) : (
                  <>
                    <span>+</span>
                    Clone Repository
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