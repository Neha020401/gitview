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
 * - Run button to start dev server
 * - Stop button to terminate running projects
 * - Delete button with confirmation modal
 * - Preview URL display for running projects
 * - Permission guidance modal for troubleshooting
 * - Dark/Light theme toggle
 * 
 * API ENDPOINTS USED:
 * - GET /api/projects - Fetch all projects
 * - POST /api/projects/{branch}/run - Start a project
 * - POST /api/projects/{branch}/stop - Stop a project
 * - DELETE /api/projects/{branch} - Delete a project
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

  // Array of project objects from backend (contains branchName, techStack, status, etc.)
  const [projects, setProjects] = useState([])

  // Loading state while fetching projects
  const [isLoading, setIsLoading] = useState(true)

  // Theme toggle state (true = dark mode, false = light mode)
  const [isDarkMode, setIsDarkMode] = useState(true)

  // Stores branchName of project that has delete confirmation dialog open
  // null means no dialog is open
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null)

  // Stores branchName of project currently performing an action (run/stop/delete)
  // Used to show loading spinners and disable buttons
  const [runningAction, setRunningAction] = useState(null)

  // Controls visibility of the permission guide modal
  // Shown when a run command fails due to permissions
  const [showPermissionGuide, setShowPermissionGuide] = useState(false)

  // ========================================
  // THEME MANAGEMENT
  // ========================================

  /**
   * Effect: Apply theme class to document root
   * 
   * When isDarkMode changes, this updates the <html> element's class
   * to either 'dark-theme' or 'light-theme', which triggers CSS variable changes
   */
  useEffect(() => {
    document.documentElement.className = isDarkMode ? "dark-theme" : "light-theme"
  }, [isDarkMode])

  /**
   * Toggle between dark and light themes
   */
  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode)
  }

  // ========================================
  // DATA FETCHING
  // ========================================

  /**
   * Effect: Fetch projects on component mount
   * 
   * Runs once when component first renders to load project list
   */
  useEffect(() => {
    fetchProjects()
  }, [])

  /**
   * Fetch all projects from backend API
   * 
   * Calls GET /api/projects and updates the projects state
   * Sets loading state appropriately
   */
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

  /**
   * Run a project (install dependencies + start dev server)
   * 
   * @param {string} branchName - The project identifier (branch name)
   * 
   * FLOW:
   * 1. Set loading state for this project
   * 2. POST to /api/projects/{branchName}/run
   * 3. If successful, refresh project list and open preview URL
   * 4. If failed, show error alert and permission guide if needed
   */
  const handleRun = async (branchName) => {
    setRunningAction(branchName) // Show loading spinner on this project's buttons
    try {
      const res = await axios.post(`http://localhost:8080/api/projects/${branchName}/run`)
      if (res.data.success) {
        fetchProjects() // Refresh to show updated status
        // Open the preview URL in a new browser tab
        if (res.data.previewUrl) {
          window.open(res.data.previewUrl, '_blank')
        }
      } else {
        alert("Failed to run project: " + res.data.error)
        // Show permission guide if backend indicates permission issues
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
      setRunningAction(null) // Clear loading state
    }
  }

  /**
   * Stop a running project
   * 
   * @param {string} branchName - The project identifier
   * 
   * Calls POST /api/projects/{branchName}/stop to terminate the dev server process
   */
  const handleStop = async (branchName) => {
    setRunningAction(branchName)
    try {
      await axios.post(`http://localhost:8080/api/projects/${branchName}/stop`)
      fetchProjects() // Refresh to show updated status (stopped)
    } catch (err) {
      alert("Failed to stop project: " + err.message)
    } finally {
      setRunningAction(null)
    }
  }

  /**
   * Delete a project (files and registry entry)
   * 
   * @param {string} branchName - The project identifier
   * 
   * Calls DELETE /api/projects/{branchName}
   * This stops the project if running and deletes all files from disk
   */
  const handleDelete = async (branchName) => {
    setRunningAction(branchName)
    try {
      await axios.delete(`http://localhost:8080/api/projects/${branchName}`)
      setShowDeleteConfirm(null) // Close the confirmation modal
      fetchProjects() // Refresh to remove deleted project from list
    } catch (err) {
      alert("Failed to delete project: " + err.message)
    } finally {
      setRunningAction(null)
    }
  }

  // ========================================
  // HELPER FUNCTIONS
  // ========================================

  /**
   * Get CSS class for tech stack badge based on type
   * 
   * @param {string} type - Tech stack type (e.g., 'react', 'node', 'python')
   * @returns {string} - CSS class name for styling the badge
   * 
   * Each tech stack has unique colors defined in Files.css
   */
  const getTechStackBadgeClass = (type) => {
    const classMap = {
      'react': 'tech-badge-react',      // Cyan color
      'nextjs': 'tech-badge-nextjs',    // White/Black
      'vite': 'tech-badge-vite',        // Purple
      'node': 'tech-badge-node',        // Green
      'vue': 'tech-badge-vue',          // Green
      'angular': 'tech-badge-angular',  // Red
      'express': 'tech-badge-express',  // Gray
      'python': 'tech-badge-python',    // Blue
      'flask': 'tech-badge-flask',      // Gray
      'django': 'tech-badge-django',    // Green
      'fastapi': 'tech-badge-fastapi',  // Teal
      'java-maven': 'tech-badge-java',  // Orange
      'java-gradle': 'tech-badge-java', // Orange
      'static': 'tech-badge-static',    // Orange
      'unknown': 'tech-badge-unknown'   // Gray
    }
    return classMap[type] || 'tech-badge-unknown'
  }

  // ========================================
  // RENDER
  // ========================================

  return (
    <div className="files-page">
      <div className="files-container">
        {/* ===== Header Section ===== */}
        <div className="files-header">
          <div className="files-header-content">
            {/* Back navigation link to home page */}
            <Link to="/" className="back-link">
              <span>←</span> Back to Home
            </Link>
            <h1>📁 Cloned Repositories</h1>
            {/* Theme toggle button - sun for dark mode, moon for light mode */}
            <button className="theme-toggle" onClick={toggleTheme}>
              {isDarkMode ? "☀️" : "🌙"}
            </button>
          </div>
          <p className="files-header-subtitle">View and manage your cloned repository files</p>
        </div>

        {/* ===== Permission Guide Modal ===== 
            Shown when a run command fails, provides troubleshooting tips */}
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

        {/* ===== Projects List Section ===== */}
        <div className="previews-card">
          <h2 className="section-title">
            <span className="section-icon preview-icon">👁</span>
            Your Projects
            {/* Project count badge */}
            <span className="preview-count">
              {projects.length} {projects.length === 1 ? "project" : "projects"}
            </span>
          </h2>

          {/* Show loading spinner while fetching */}
          {isLoading ? (
            <div className="loading-state">
              <div className="loading-spinner"></div>
              <p>Loading projects...</p>
            </div>
          ) : projects.length === 0 ? (
            /* Empty state when no projects exist */
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <h3>No projects yet</h3>
              <p>Add your first repository to get started</p>
            </div>
          ) : (
            /* Project cards grid */
            <div className="previews-grid">
              {projects.map((project, index) => (
                <div key={index} className={`preview-item ${project.running ? 'running' : ''}`}>
                  {/* ===== Project Info Section ===== */}
                  <div className="preview-info">
                    {/* Avatar with first letter of branch name */}
                    <div className="preview-avatar">{project.branchName.charAt(0).toUpperCase()}</div>
                    <div className="preview-details">
                      <h3>{project.branchName}</h3>
                      <div className="project-meta">
                        {/* Tech Stack Badge - shows detected framework/language */}
                        <span className={`tech-badge ${getTechStackBadgeClass(project.techStack?.type)}`}>
                          <span className="tech-icon">{project.techStack?.icon || '❓'}</span>
                          {project.techStack?.displayName || 'Unknown'}
                        </span>

                        {/* Status Badge - shows running/stopped/installing state */}
                        <span className={`status-badge status-${project.status}`}>
                          {project.status === 'running' && '🟢'}
                          {project.status === 'stopped' && '⚫'}
                          {project.status === 'installing' && '📦'}
                          {project.status === 'starting' && '🔄'}
                          {project.status === 'error' && '🔴'}
                          {project.status}
                        </span>
                      </div>

                      {/* Preview URL - shown only when project is running */}
                      {project.running && project.previewUrl && (
                        <div className="preview-url-info">
                          <span className="url-label">Running at:</span>
                          <a href={project.previewUrl} target="_blank" rel="noreferrer" className="preview-url">
                            {project.previewUrl}
                          </a>
                          <span className="port-info">Port: {project.port}</span>
                        </div>
                      )}

                      {/* Install Command Info - shows what command will run (npm install, etc.) */}
                      {project.techStack?.installCommand && !project.running && (
                        <div className="install-info">
                          <span className="install-label">Install:</span>
                          <code>{project.techStack.installCommand}</code>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* ===== Action Buttons Section ===== */}
                  <div className="preview-actions">
                    {project.running ? (
                      /* Buttons shown when project is running */
                      <>
                        {/* View Preview button - opens preview URL in new tab */}
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
                        {/* Stop button - terminates the dev server */}
                        <button
                          className="stop-btn"
                          onClick={() => handleStop(project.branchName)}
                          disabled={runningAction === project.branchName}
                        >
                          {runningAction === project.branchName ? '⏳' : '⏹'} Stop
                        </button>
                      </>
                    ) : (
                      /* Run button - shown when project is stopped */
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

                    {/* Delete button - always visible */}
                    <button
                      className="delete-btn"
                      onClick={() => setShowDeleteConfirm(project.branchName)}
                      disabled={runningAction === project.branchName}
                    >
                      🗑️ Delete
                    </button>
                  </div>

                  {/* ===== Delete Confirmation Modal ===== 
                      Shown when user clicks delete, requires confirmation */}
                  {showDeleteConfirm === project.branchName && (
                    <div className="delete-confirm-overlay" onClick={() => setShowDeleteConfirm(null)}>
                      <div className="delete-confirm-modal" onClick={(e) => e.stopPropagation()}>
                        <h4>⚠️ Delete Project?</h4>
                        <p>This will permanently delete <strong>{project.branchName}</strong> and all its files.</p>
                        <div className="delete-confirm-actions">
                          <button className="cancel-btn" onClick={() => setShowDeleteConfirm(null)}>
                            Cancel
                          </button>
                          <button className="confirm-delete-btn" onClick={() => handleDelete(project.branchName)}>
                            Yes, Delete
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
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