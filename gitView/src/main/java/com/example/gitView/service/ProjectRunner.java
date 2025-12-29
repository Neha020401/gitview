package com.example.gitView.service;

import com.example.gitView.model.Project;
import com.example.gitView.model.TechStack;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================================
 * ProjectRunner - Project Lifecycle Management Service
 * ============================================================================
 * 
 * PURPOSE:
 * Manages the complete lifecycle of cloned projects:
 * - Registering new projects (after cloning)
 * - Running projects (install + start dev server)
 * - Stopping running projects
 * - Deleting projects (stop + remove files)
 * 
 * HOW IT WORKS:
 * 1. Uses ProcessBuilder to spawn OS processes for running commands
 * 2. Tracks running processes by branch name
 * 3. Manages port allocation to avoid conflicts
 * 4. Stores project metadata in memory (lost on restart)
 * 
 * THREAD SAFETY:
 * Uses ConcurrentHashMap for all storage to support concurrent requests
 * 
 * USAGE:
 * Project p = projectRunner.registerProject("main", "/path/to/repo", "url");
 * projectRunner.runProject("main"); // Starts dev server
 * projectRunner.stopProject("main"); // Stops dev server
 * projectRunner.deleteProject("main"); // Removes everything
 * 
 * ============================================================================
 */
@Service // Spring annotation: Makes this class a singleton bean
public class ProjectRunner {

    /**
     * Injected service for detecting project tech stacks
     * Used when registering new projects
     */
    private final TechStackDetector techStackDetector;

    /**
     * Map of branch name -> running Process
     * Stores references to running dev server processes
     * Needed to stop processes later
     */
    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    /**
     * Map of branch name -> Project
     * Stores all registered project metadata
     * This is the main data store for the application
     */
    private final Map<String, Project> projects = new ConcurrentHashMap<>();

    /**
     * Set of currently used port numbers
     * Prevents port conflicts between multiple running projects
     */
    private final Set<Integer> usedPorts = ConcurrentHashMap.newKeySet();

    /**
     * Constructor injection of dependencies
     * Spring automatically injects TechStackDetector
     */
    public ProjectRunner(TechStackDetector techStackDetector) {
        this.techStackDetector = techStackDetector;
    }

    // ========================================================================
    // PROJECT REGISTRATION
    // ========================================================================

    /**
     * Register a new project after cloning
     * 
     * @param branchName  - Git branch name (used as project identifier)
     * @param projectPath - Absolute path to cloned files
     * @param repoUrl     - Original repository URL
     * @return Project - The newly registered project with detected tech stack
     * 
     *         FLOW:
     *         1. Detect tech stack by analyzing project files
     *         2. Create Project object with detected info
     *         3. Store in projects map
     */
    public Project registerProject(String branchName, String projectPath, String repoUrl) {
        // Automatically detect what type of project this is
        TechStack techStack = techStackDetector.detect(projectPath);

        // Create new project with default stopped state
        Project project = new Project(branchName, projectPath, repoUrl, techStack);

        // Store in our registry
        projects.put(branchName, project);

        return project;
    }

    // ========================================================================
    // PROJECT QUERIES
    // ========================================================================

    /**
     * Get all registered projects
     * 
     * @return Collection of all projects
     *         Used by GET /api/projects endpoint
     */
    public Collection<Project> getAllProjects() {
        return projects.values();
    }

    /**
     * Get a specific project by branch name
     * 
     * @param branchName - The project identifier
     * @return Optional<Project> - The project if found, empty otherwise
     */
    public Optional<Project> getProject(String branchName) {
        return Optional.ofNullable(projects.get(branchName));
    }

    // ========================================================================
    // PROJECT EXECUTION
    // ========================================================================

    /**
     * Run a project (install dependencies + start dev server)
     * 
     * @param branchName - The project identifier
     * @return Project - Updated project with running status
     * @throws Exception - If project not found or execution fails
     * 
     *                   FLOW:
     *                   1. Find the project by branchName
     *                   2. Check it's not already running
     *                   3. Find an available port
     *                   4. Run install command (npm install, pip install, etc.)
     *                   5. Start dev server
     *                   6. Update project status and return
     */
    public Project runProject(String branchName) throws Exception {
        // Find the project
        Project project = projects.get(branchName);
        if (project == null) {
            throw new RuntimeException("Project not found: " + branchName);
        }

        // Check if already running
        if (project.isRunning()) {
            throw new RuntimeException("Project is already running");
        }

        TechStack techStack = project.getTechStack();
        String projectPath = project.getProjectPath();

        // Find a port that's not in use
        int port = findAvailablePort(techStack.getDefaultPort());

        try {
            // ===== STEP 1: RUN INSTALL COMMAND =====
            // Only if there's an install command defined
            if (techStack.getInstallCommand() != null && !techStack.getInstallCommand().isEmpty()) {
                project.setStatus("installing");
                runInstallCommand(projectPath, techStack.getInstallCommand());
            }

            // ===== STEP 2: START DEV SERVER =====
            project.setStatus("starting");
            Process process = startDevServer(projectPath, techStack.getRunCommand(), port);

            // Store the process for later stop/status checks
            runningProcesses.put(branchName, process);
            usedPorts.add(port);

            // ===== STEP 3: UPDATE PROJECT STATUS =====
            project.setRunning(true);
            project.setPort(port);
            project.setPreviewUrl("http://localhost:" + port);
            project.setStatus("running");
            project.setErrorMessage(null);

            return project;

        } catch (Exception e) {
            // Mark as error on failure
            project.setStatus("error");
            project.setErrorMessage(e.getMessage());
            throw e;
        }
    }

    /**
     * Stop a running project
     * 
     * @param branchName - The project identifier
     * @return Project - Updated project with stopped status
     * 
     *         FLOW:
     *         1. Find the project
     *         2. Get the running process
     *         3. Kill the process (destroyForcibly for Windows compatibility)
     *         4. Clean up maps and update status
     */
    public Project stopProject(String branchName) {
        Project project = projects.get(branchName);
        if (project == null) {
            throw new RuntimeException("Project not found: " + branchName);
        }

        // Get and kill the running process
        Process process = runningProcesses.get(branchName);
        if (process != null && process.isAlive()) {
            // destroyForcibly() is needed on Windows to kill child processes too
            process.destroyForcibly();
            runningProcesses.remove(branchName);
        }

        // Free up the port for reuse
        usedPorts.remove(project.getPort());

        // Reset project status
        project.setRunning(false);
        project.setPort(0);
        project.setPreviewUrl(null);
        project.setStatus("stopped");

        return project;
    }

    /**
     * Delete a project completely
     * 
     * @param branchName - The project identifier
     * @return true if deleted, false if not found
     * 
     *         FLOW:
     *         1. Stop the project if running
     *         2. Delete all project files from disk
     *         3. Remove from registry
     */
    public boolean deleteProject(String branchName) {
        Project project = projects.get(branchName);
        if (project == null) {
            return false;
        }

        // Stop if running (to free up resources)
        if (project.isRunning()) {
            stopProject(branchName);
        }

        // Delete files from disk
        File projectDir = new File(project.getProjectPath());
        if (projectDir.exists()) {
            deleteDirectory(projectDir);
        }

        // Remove from our registry
        projects.remove(branchName);

        return true;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    /**
     * Run the install command (npm install, pip install, etc.)
     * 
     * @param projectPath    - Directory to run command in
     * @param installCommand - The command to run
     * @throws Exception - If command fails (non-zero exit code)
     * 
     *                   This runs synchronously - waits for install to complete
     */
    private void runInstallCommand(String projectPath, String installCommand) throws Exception {
        ProcessBuilder pb = createProcessBuilder(projectPath, installCommand);
        pb.inheritIO(); // Show output in console for debugging

        Process process = pb.start();
        int exitCode = process.waitFor(); // Wait for completion

        if (exitCode != 0) {
            throw new RuntimeException("Install command failed with exit code: " + exitCode);
        }
    }

    /**
     * Start the dev server process
     * 
     * @param projectPath - Directory to run server in
     * @param runCommand  - The start command (npm start, flask run, etc.)
     * @param port        - Port to run server on
     * @return Process - The running server process
     * @throws Exception - If server fails to start
     */
    private Process startDevServer(String projectPath, String runCommand, int port) throws Exception {
        // Modify command to use the specific port
        String modifiedCommand = modifyPortInCommand(runCommand, port);

        ProcessBuilder pb = createProcessBuilder(projectPath, modifiedCommand);
        pb.redirectErrorStream(true); // Combine stdout and stderr

        Process process = pb.start();

        // Wait a bit for the server to start up
        Thread.sleep(2000);

        // Check if process is still alive (didn't crash immediately)
        if (!process.isAlive()) {
            throw new RuntimeException("Dev server failed to start");
        }

        return process;
    }

    /**
     * Create a ProcessBuilder for running shell commands
     * 
     * @param projectPath - Working directory for the command
     * @param command     - The command to run
     * @return ProcessBuilder - Configured for the OS
     * 
     *         Handles Windows vs Unix differences:
     *         - Windows: cmd.exe /c command
     *         - Unix: sh -c command
     */
    private ProcessBuilder createProcessBuilder(String projectPath, String command) {
        ProcessBuilder pb;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows")) {
            // Windows: use cmd.exe
            pb = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            // Unix/Linux/Mac: use sh
            pb = new ProcessBuilder("sh", "-c", command);
        }

        // Set working directory
        pb.directory(new File(projectPath));

        // Set PORT environment variable (used by many Node.js apps)
        pb.environment().put("PORT", String.valueOf(findAvailablePort(3000)));

        return pb;
    }

    /**
     * Modify the run command to use a specific port
     * 
     * @param command - Original command
     * @param port    - Port to use
     * @return Modified command with port specified
     * 
     *         Different frameworks have different ways to specify ports:
     *         - Node.js: Uses PORT env var (handled in createProcessBuilder)
     *         - Flask: --port flag
     *         - Django: port as argument
     *         - Uvicorn: --port flag
     */
    private String modifyPortInCommand(String command, int port) {
        // For npm commands, PORT env var is usually sufficient
        if (command.contains("npm start") || command.contains("npm run dev")) {
            return command;
        }

        // Flask: add --port flag
        if (command.contains("flask run")) {
            return command + " --port " + port;
        }

        // Django: add port as argument
        if (command.contains("manage.py runserver")) {
            return command + " " + port;
        }

        // FastAPI/Uvicorn: add --port flag
        if (command.contains("uvicorn")) {
            return command + " --port " + port;
        }

        // Static file server: -p flag
        if (command.contains("serve")) {
            return command + " -p " + port;
        }

        return command;
    }

    /**
     * Find an available port starting from the given port
     * 
     * @param startPort - Port to try first
     * @return int - An available port number
     * 
     *         Checks both our usedPorts set and actually tries to bind
     *         to make sure the port is truly available
     */
    private int findAvailablePort(int startPort) {
        int port = startPort;
        while (usedPorts.contains(port) || !isPortAvailable(port)) {
            port++;
            if (port > 65535) {
                throw new RuntimeException("No available ports found");
            }
        }
        return port;
    }

    /**
     * Check if a port is available by trying to bind to it
     * 
     * @param port - Port to check
     * @return true if available, false if in use
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false; // Port already in use
        }
    }

    /**
     * Recursively delete a directory and all its contents
     * 
     * @param directory - Directory to delete
     * 
     *                  Used when deleting a project to clean up all files
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file); // Recursive call for subdirectories
                } else {
                    file.delete(); // Delete file
                }
            }
        }
        directory.delete(); // Delete the now-empty directory
    }
}
