package com.example.gitLive.controller;

import com.example.gitLive.model.Project;
import com.example.gitLive.service.ProjectRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * ProjectController - REST API for Project Management
 * ============================================================================
 * 
 * PURPOSE:
 * Provides REST endpoints for managing cloned projects.
 * This is the main API used by the frontend Files.js component.
 * 
 * ENDPOINTS:
 * GET /api/projects - List all projects
 * GET /api/projects/{branch} - Get specific project
 * POST /api/projects/{branch}/run - Run a project
 * POST /api/projects/{branch}/stop - Stop a running project
 * DELETE /api/projects/{branch} - Delete a project
 * GET /api/projects/{branch}/status - Get project status
 * 
 * RESPONSE FORMAT:
 * All responses return JSON with either:
 * - Project object(s) for successful operations
 * - { success: true/false, message/error: "..." } for actions
 * 
 * ============================================================================
 */
@RestController // Spring: This is a REST controller
@RequestMapping("/api/projects") // Base path for all endpoints
@CrossOrigin(origins = "*") // Allow requests from any origin (for CORS)
public class ProjectController {

    /**
     * Injected ProjectRunner service
     * Handles all the actual project operations
     */
    private final ProjectRunner projectRunner;

    /**
     * Constructor injection
     * Spring automatically injects the ProjectRunner bean
     */
    public ProjectController(ProjectRunner projectRunner) {
        this.projectRunner = projectRunner;
    }

    // ========================================================================
    // GET ENDPOINTS - Read operations
    // ========================================================================

    /**
     * GET /api/projects
     * 
     * List all registered projects with their current status
     * 
     * @return Collection<Project> - All projects as JSON array
     * 
     *         RESPONSE EXAMPLE:
     *         [
     *         {
     *         "branchName": "main",
     *         "projectPath": "C:\\temp\\gitviewer\\main",
     *         "techStack": { "type": "react", "displayName": "React.js", ... },
     *         "running": true,
     *         "previewUrl": "http://localhost:3000",
     *         "port": 3000,
     *         "status": "running"
     *         },
     *         ...
     *         ]
     */
    @GetMapping
    public Collection<Project> getAllProjects() {
        return projectRunner.getAllProjects();
    }

    /**
     * GET /api/projects/{branchName}
     * 
     * Get a specific project by its branch name
     * 
     * @param branchName - The project identifier (from URL path)
     * @return Project or 404 if not found
     */
    @GetMapping("/{branchName}")
    public ResponseEntity<Project> getProject(@PathVariable String branchName) {
        return projectRunner.getProject(branchName)
                .map(ResponseEntity::ok) // Return 200 OK with project
                .orElse(ResponseEntity.notFound().build()); // Return 404
    }

    /**
     * GET /api/projects/{branchName}/status
     * 
     * Get just the status information for a project
     * Useful for polling when waiting for a project to start
     * 
     * @param branchName - The project identifier
     * @return Status map with running state, URL, port, etc.
     */
    @GetMapping("/{branchName}/status")
    public ResponseEntity<Map<String, Object>> getProjectStatus(@PathVariable String branchName) {
        return projectRunner.getProject(branchName)
                .map(project -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("branchName", project.getBranchName());
                    status.put("running", project.isRunning());
                    status.put("status", project.getStatus());
                    status.put("previewUrl", project.getPreviewUrl());
                    status.put("port", project.getPort());
                    status.put("techStack", project.getTechStack());
                    status.put("errorMessage", project.getErrorMessage());
                    return ResponseEntity.ok(status);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ========================================================================
    // POST ENDPOINTS - Action operations
    // ========================================================================

    /**
     * POST /api/projects/{branchName}/run
     * 
     * Run a project (install dependencies + start dev server)
     * 
     * @param branchName - The project identifier
     * @return JSON with success status, preview URL, and port
     * 
     *         SUCCESS RESPONSE:
     *         {
     *         "success": true,
     *         "message": "Project started successfully",
     *         "project": { ... },
     *         "previewUrl": "http://localhost:3000",
     *         "port": 3000,
     *         "permissionNote": "If you encounter..."
     *         }
     * 
     *         ERROR RESPONSE (400 Bad Request):
     *         {
     *         "success": false,
     *         "error": "Install command failed with exit code: 1",
     *         "permissionNote": "This operation may require..."
     *         }
     */
    @PostMapping("/{branchName}/run")
    public ResponseEntity<Map<String, Object>> runProject(@PathVariable String branchName) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Attempt to run the project
            Project project = projectRunner.runProject(branchName);

            // Build success response
            response.put("success", true);
            response.put("message", "Project started successfully");
            response.put("project", project);
            response.put("previewUrl", project.getPreviewUrl());
            response.put("port", project.getPort());

            // Include permission note for user guidance
            response.put("permissionNote",
                    "If you encounter permission errors, try running your terminal as Administrator.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Build error response
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("permissionNote",
                    "This operation may require administrator permissions. Please ensure you have the necessary rights and that no antivirus is blocking the process.");

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/projects/{branchName}/stop
     * 
     * Stop a running project's dev server
     * 
     * @param branchName - The project identifier
     * @return JSON with success status
     * 
     *         SUCCESS RESPONSE:
     *         {
     *         "success": true,
     *         "message": "Project stopped successfully",
     *         "project": { ... }
     *         }
     */
    @PostMapping("/{branchName}/stop")
    public ResponseEntity<Map<String, Object>> stopProject(@PathVariable String branchName) {
        Map<String, Object> response = new HashMap<>();

        try {
            Project project = projectRunner.stopProject(branchName);

            response.put("success", true);
            response.put("message", "Project stopped successfully");
            response.put("project", project);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================================================================
    // DELETE ENDPOINT
    // ========================================================================

    /**
     * DELETE /api/projects/{branchName}
     * 
     * Delete a project (stops if running, then removes all files)
     * 
     * @param branchName - The project identifier
     * @return JSON with success status
     * 
     *         SUCCESS RESPONSE:
     *         {
     *         "success": true,
     *         "message": "Project deleted successfully"
     *         }
     * 
     *         NOT FOUND RESPONSE (404):
     *         {
     *         "success": false,
     *         "error": "Project not found"
     *         }
     */
    @DeleteMapping("/{branchName}")
    public ResponseEntity<Map<String, Object>> deleteProject(@PathVariable String branchName) {
        Map<String, Object> response = new HashMap<>();

        boolean deleted = projectRunner.deleteProject(branchName);

        if (deleted) {
            response.put("success", true);
            response.put("message", "Project deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("error", "Project not found");
            return ResponseEntity.notFound().build();
        }
    }
}
