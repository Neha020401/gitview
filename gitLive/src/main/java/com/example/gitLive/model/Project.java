package com.example.gitLive.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * Project - Cloned Repository Model
 * ============================================================================
 * 
 * PURPOSE:
 * Represents a cloned Git repository with all its metadata.
 * Tracks the project's tech stack, running status, and preview URL.
 * 
 * LIFECYCLE:
 * 1. Created when a repository is cloned (via RepoController.addRepo)
 * 2. Updated when project is run/stopped
 * 3. Deleted when user deletes the project
 * 
 * FIELDS:
 * - branchName: Unique identifier (the Git branch name)
 * - projectPath: Absolute path to cloned files on disk
 * - repoUrl: Original Git repository URL
 * - techStack: Detected technology stack
 * - running: Whether dev server is currently running
 * - previewUrl: URL where preview is accessible
 * - port: Port number where dev server is running
 * - status: Current state (stopped, installing, running, error)
 * - errorMessage: Error details if status is "error"
 * 
 * ============================================================================
 */
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@AllArgsConstructor // Lombok: Generates constructor with all fields
@NoArgsConstructor // Lombok: Generates empty constructor
public class Project {

    /**
     * Unique identifier for the project
     * This is the Git branch name used when cloning
     * Example: "main", "develop", "feature-login"
     */
    private String branchName;

    /**
     * Absolute path to the cloned repository on disk
     * Example: "C:\\Users\\username\\AppData\\Local\\Temp\\gitviewer\\main"
     * Used when running commands and deleting files
     */
    private String projectPath;

    /**
     * Original Git repository URL
     * Example: "https://github.com/facebook/react.git"
     * Stored for reference and display purposes
     */
    private String repoUrl;

    /**
     * Detected technology stack of the project
     * Contains info about how to run the project
     * 
     * @see TechStack
     */
    private TechStack techStack;

    /**
     * Whether the dev server is currently running
     * true = process is alive and serving on previewUrl
     * false = process is not running
     */
    private boolean running;

    /**
     * URL where the preview is accessible
     * Example: "http://localhost:3000"
     * null when project is not running
     */
    private String previewUrl;

    /**
     * Port number where the dev server is running
     * Example: 3000, 5173, 8080
     * 0 when project is not running
     */
    private int port;

    /**
     * Current status of the project
     * Values:
     * - "stopped": Not running
     * - "installing": Running npm install, pip install, etc.
     * - "starting": Dev server is starting up
     * - "running": Dev server is active
     * - "error": Something went wrong
     */
    private String status;

    /**
     * Error message when status is "error"
     * Contains details about what went wrong
     * null when there's no error
     */
    private String errorMessage;

    /**
     * Convenience constructor for creating a new project
     * Sets default values for running state
     * 
     * @param branchName  - Git branch name (project identifier)
     * @param projectPath - Path to cloned files
     * @param repoUrl     - Original repository URL
     * @param techStack   - Detected technology stack
     */
    public Project(String branchName, String projectPath, String repoUrl, TechStack techStack) {
        this.branchName = branchName;
        this.projectPath = projectPath;
        this.repoUrl = repoUrl;
        this.techStack = techStack;
        this.running = false; // Not running initially
        this.previewUrl = null; // No URL yet
        this.port = 0; // No port assigned
        this.status = "stopped"; // Initial status
        this.errorMessage = null; // No errors
    }
}
