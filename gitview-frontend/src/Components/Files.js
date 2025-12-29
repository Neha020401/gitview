/**
 * ============================================================================
 * Files.js - Project Management Dashboard Component
 * ============================================================================
 * 
 * PURPOSE:
 * This component displays all cloned repositories with their detected tech 
 * stacks and provides controls to run, stop, and delete projects.
 * 
 * FEATURES:
 * - Lists all cloned projects from backend
 * - Shows tech stack badge (React, Node, Python, etc.)
 * - Displays cloned path for each project
 * - Run button to start dev server
 * - Stop button to terminate running projects
 * - Delete button with confirmation modal
 * - Preview URL display for running projects
 * - Permission guidance modal for troubleshooting
 * - Dark/Light theme toggle
 * 
 * ============================================================================
 */

import React from 'react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import './Files.css'

const Files = () => {
  // ========================================
  // STATE VARIABLES
  // ========================================

  const [projects, setProjects] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isDarkMode, setIsDarkMode] = useState(true)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null) // branchName or null
  const [runningAction, setRunningAction] = useState(null)
  const [showPermissionGuide, setShowPermissionGuide] = useState(false)

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

  useEffect(() => {
    fetchProjects()
  }, [])

  const fetchProjects = async () => {
    setIsLoading(true)
    try {
      const res = await axios.get("http://localhost:8080/api/projects")
      setProjects(res.data)
    } catch (err) {
      console.error("Error fetching projects", err)
    } finally {
      setIsLoading(false)
    }
  }

  // ========================================
  // PROJECT ACTIONS
  // ========================================

  const handleRun = async (branchName) => {
    setRunningAction(branchName)
    try {
      const res = await axios.post(`http://localhost:8080/api/projects/${branchName}/run`)
      if (res.data.success) {
        fetchProjects()
        if (res.data.previewUrl) {
          window.open(res.data.previewUrl, '_blank')
        }
      } else {
        alert("Failed to run project: " + res.data.error)
        if (res.data.permissionNote) {
          setShowPermissionGuide(true)
        }
      }
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.message
      alert("Failed to run project: " + errorMsg)
      if (err.response?.data?.permissionNote) {
        setShowPermissionGuide(true)
      }
    } finally {
      setRunningAction(null)
    }
  }

  const handleStop = async (branchName) => {
    setRunningAction(branchName)
    try {
      await axios.post(`http://localhost:8080/api/projects/${branchName}/stop`)
      fetchProjects()
    } catch (err) {
      alert("Failed to stop project: " + err.message)
    } finally {
      setRunningAction(null)
    }
  }

  const handleDelete = async (branchName) => {
    setRunningAction(branchName)
    try {
      await axios.delete(`http://localhost:8080/api/projects/${branchName}`)
      setShowDeleteConfirm(null)
      fetchProjects()
    } catch (err) {
      alert("Failed to delete project: " + err.message)
    } finally {
      setRunningAction(null)
    }
  }

  // ========================================
  // HELPER FUNCTIONS
  // ========================================

  const getTechStackBadgeClass = (type) => {
    const classMap = {
      'react': 'tech-badge-react',
      'nextjs': 'tech-badge-nextjs',
      'vite': 'tech-badge-vite',
      'node': 'tech-badge-node',
      'vue': 'tech-badge-vue',
      'angular': 'tech-badge-angular',
      'express': 'tech-badge-express',
      'python': 'tech-badge-python',
      'flask': 'tech-badge-flask',
      'django': 'tech-badge-django',
      'fastapi': 'tech-badge-fastapi',
      'java-maven': 'tech-badge-java',
      'java-gradle': 'tech-badge-java',
      'static': 'tech-badge-static',
      'unknown': 'tech-badge-unknown'
    }
    return classMap[type] || 'tech-badge-unknown'
  }

  // Get the project being deleted for modal display
  const projectToDelete = projects.find(p => p.branchName === showDeleteConfirm)

  // ========================================
  // RENDER
  // ========================================

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

        {/* Permission Guide Modal - Fixed position outside cards */}
        {showPermissionGuide && (
          <div className="permission-modal-overlay" onClick={() => setShowPermissionGuide(false)}>
            <div className="permission-modal" onClick={(e) => e.stopPropagation()}>
              <h3>⚠️ Permission Required</h3>
              <p>To run projects, you may need additional permissions:</p>
              <ul>
                <li><strong>Windows:</strong> Run your terminal as Administrator</li>
                <li>Ensure Node.js/Python/Java are installed and in PATH</li>
                <li>Check if antivirus is blocking the process</li>
                <li>Make sure the port is not already in use</li>
              </ul>
              <button className="permission-close-btn" onClick={() => setShowPermissionGuide(false)}>
                Got it!
              </button>
            </div>
          </div>
        )}

        {/* Delete Confirmation Modal - MOVED OUTSIDE preview-item for proper positioning */}
        {showDeleteConfirm && projectToDelete && (
          <div className="delete-confirm-overlay" onClick={() => setShowDeleteConfirm(null)}>
            <div className="delete-confirm-modal" onClick={(e) => e.stopPropagation()}>
              <h4>⚠️ Delete Project?</h4>
              <p>This will permanently delete <strong>{projectToDelete.branchName}</strong> and all its files.</p>
              <p className="delete-path-info">
                <span className="path-label">Location:</span>
                <code>{projectToDelete.projectPath}</code>
              </p>
              <div className="delete-confirm-actions">
                <button className="cancel-btn" onClick={() => setShowDeleteConfirm(null)}>
                  Cancel
                </button>
                <button
                  className="confirm-delete-btn"
                  onClick={() => handleDelete(projectToDelete.branchName)}
                  disabled={runningAction === projectToDelete.branchName}
                >
                  {runningAction === projectToDelete.branchName ? 'Deleting...' : 'Yes, Delete'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Projects Section */}
        <div className="previews-card">
          <h2 className="section-title">
            <span className="section-icon preview-icon">👁</span>
            Your Projects
            <span className="preview-count">
              {projects.length} {projects.length === 1 ? "project" : "projects"}
            </span>
          </h2>

          {isLoading ? (
            <div className="loading-state">
              <div className="loading-spinner"></div>
              <p>Loading projects...</p>
            </div>
          ) : projects.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <h3>No projects yet</h3>
              <p>Add your first repository to get started</p>
            </div>
          ) : (
            <div className="previews-grid">
              {projects.map((project, index) => (
                <div key={index} className={`preview-item ${project.running ? 'running' : ''}`}>
                  <div className="preview-info">
                    <div className="preview-avatar">{project.branchName.charAt(0).toUpperCase()}</div>
                    <div className="preview-details">
                      <h3>{project.branchName}</h3>

                      {/* Cloned Path Display */}
                      <div className="project-path">
                        <span className="path-icon">📂</span>
                        <span className="path-text" title={project.projectPath}>
                          {project.projectPath}
                        </span>
                      </div>

                      <div className="project-meta">
                        {/* Tech Stack Badge */}
                        <span className={`tech-badge ${getTechStackBadgeClass(project.techStack?.type)}`}>
                          <span className="tech-icon">{project.techStack?.icon || '❓'}</span>
                          {project.techStack?.displayName || 'Unknown'}
                        </span>

                        {/* Status Badge */}
                        <span className={`status-badge status-${project.status}`}>
                          {project.status === 'running' && '🟢'}
                          {project.status === 'stopped' && '⚫'}
                          {project.status === 'installing' && '📦'}
                          {project.status === 'starting' && '🔄'}
                          {project.status === 'error' && '🔴'}
                          {project.status}
                        </span>
                      </div>

                      {/* Preview URL - shown when running */}
                      {project.running && project.previewUrl && (
                        <div className="preview-url-info">
                          <span className="url-label">Running at:</span>
                          <a href={project.previewUrl} target="_blank" rel="noreferrer" className="preview-url">
                            {project.previewUrl}
                          </a>
                          <span className="port-info">Port: {project.port}</span>
                        </div>
                      )}

                      {/* Install Command Info */}
                      {project.techStack?.installCommand && !project.running && (
                        <div className="install-info">
                          <span className="install-label">Install:</span>
                          <code>{project.techStack.installCommand}</code>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="preview-actions">
                    {project.running ? (
                      <>
                        <a href={project.previewUrl} target="_blank" rel="noreferrer" className="preview-link">
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
                        <button
                          className="stop-btn"
                          onClick={() => handleStop(project.branchName)}
                          disabled={runningAction === project.branchName}
                        >
                          {runningAction === project.branchName ? '⏳' : '⏹'} Stop
                        </button>
                      </>
                    ) : (
                      <button
                        className="run-btn"
                        onClick={() => handleRun(project.branchName)}
                        disabled={runningAction === project.branchName || project.techStack?.type === 'unknown'}
                      >
                        {runningAction === project.branchName ? (
                          <>
                            <span className="btn-spinner"></span>
                            Starting...
                          </>
                        ) : (
                          <>▶ Run</>
                        )}
                      </button>
                    )}

                    <button
                      className="delete-btn"
                      onClick={() => setShowDeleteConfirm(project.branchName)}
                      disabled={runningAction === project.branchName}
                    >
                      🗑️ Delete
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