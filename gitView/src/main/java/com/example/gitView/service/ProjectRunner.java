package com.example.gitView.service;

import com.example.gitView.model.Project;
import com.example.gitView.model.TechStack;
import com.example.gitView.repository.ProjectRepository; // ← ADD THIS IMPORT
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectRunner {

    private final TechStackDetector techStackDetector;
    private final ProjectRepository projectRepository; // ← ADD THIS LINE

    // Keep these for runtime state (processes are NOT stored in database)
    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    // ← REMOVE THIS LINE: private final Map<String, Project> projects = new ConcurrentHashMap<>();
    // We're replacing the 'projects' Map with the database repository

    private final Set<Integer> usedPorts = ConcurrentHashMap.newKeySet();

    // ← UPDATE CONSTRUCTOR: Add projectRepository parameter
    public ProjectRunner(TechStackDetector techStackDetector, ProjectRepository projectRepository) {
        this.techStackDetector = techStackDetector;
        this.projectRepository = projectRepository; // ← ADD THIS LINE
    }

    // ========================================================================
    // PROJECT REGISTRATION
    // ========================================================================

    public Project registerProject(String branchName, String projectPath, String repoUrl) {
        TechStack techStack = techStackDetector.detect(projectPath);
        Project project = new Project(branchName, projectPath, repoUrl, techStack);

        // ← CHANGE THIS LINE from: projects.put(branchName, project);
        return projectRepository.save(project); // ← TO THIS: Save to database and return
    }

    // ========================================================================
    // PROJECT QUERIES
    // ========================================================================

    public Collection<Project> getAllProjects() {
        // ← CHANGE THIS LINE from: return projects.values();
        return projectRepository.findAll(); // ← TO THIS: Get all from database
    }

    public Optional<Project> getProject(String branchName) {
        // ← CHANGE THIS LINE from: return Optional.ofNullable(projects.get(branchName));
        return projectRepository.findById(branchName); // ← TO THIS: Get from database
    }

    // ========================================================================
    // PROJECT EXECUTION
    // ========================================================================

    public Project runProject(String branchName) throws Exception {
        // ← CHANGE THIS BLOCK from: Project project = projects.get(branchName);
        Project project = projectRepository.findById(branchName)  // ← TO THIS
                .orElseThrow(() -> new RuntimeException("Project not found: " + branchName));

        // Remove the null check since orElseThrow handles it
        // ← DELETE: if (project == null) { throw... }

        if (project.isRunning()) {
            throw new RuntimeException("Project is already running");
        }

        TechStack techStack = project.getTechStack();
        String projectPath = project.getProjectPath();
        int port = findAvailablePort(techStack.getDefaultPort());

        try {
            if (techStack.getInstallCommand() != null && !techStack.getInstallCommand().isEmpty()) {
                project.setStatus("installing");
                projectRepository.save(project); // ← ADD THIS: Save status to database
                runInstallCommand(projectPath, techStack.getInstallCommand());
            }

            project.setStatus("starting");
            projectRepository.save(project); // ← ADD THIS: Save status to database

            Process process = startDevServer(projectPath, techStack.getRunCommand(), port);

            runningProcesses.put(branchName, process);
            usedPorts.add(port);

            project.setRunning(true);
            project.setPort(port);
            project.setPreviewUrl("http://localhost:" + port);
            project.setStatus("running");
            project.setErrorMessage(null);

            return projectRepository.save(project); // ← CHANGE: Save to database and return

        } catch (Exception e) {
            project.setStatus("error");
            project.setErrorMessage(e.getMessage());
            projectRepository.save(project); // ← ADD THIS: Save error state to database
            throw e;
        }
    }

    public Project stopProject(String branchName) {
        // ← CHANGE THIS BLOCK from: Project project = projects.get(branchName);
        Project project = projectRepository.findById(branchName)  // ← TO THIS
                .orElseThrow(() -> new RuntimeException("Project not found: " + branchName));

        // Remove the null check
        // ← DELETE: if (project == null) { throw... }

        Process process = runningProcesses.get(branchName);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            runningProcesses.remove(branchName);
        }

        usedPorts.remove(project.getPort());

        project.setRunning(false);
        project.setPort(0);
        project.setPreviewUrl(null);
        project.setStatus("stopped");

        return projectRepository.save(project); // ← CHANGE: Save to database and return
    }

    public boolean deleteProject(String branchName) {
        // ← CHANGE THIS BLOCK from: Project project = projects.get(branchName);
        Optional<Project> projectOpt = projectRepository.findById(branchName); // ← TO THIS
        if (projectOpt.isEmpty()) {
            return false;
        }

        Project project = projectOpt.get();

        if (project.isRunning()) {
            stopProject(branchName);
        }

        File projectDir = new File(project.getProjectPath());
        if (projectDir.exists()) {
            deleteDirectory(projectDir);
        }

        // ← CHANGE THIS LINE from: projects.remove(branchName);
        projectRepository.deleteById(branchName); // ← TO THIS: Delete from database

        return true;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - NO CHANGES NEEDED BELOW THIS LINE
    // ========================================================================

    private void runInstallCommand(String projectPath, String installCommand) throws Exception {
        ProcessBuilder pb = createProcessBuilder(projectPath, installCommand);
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Install command failed with exit code: " + exitCode);
        }
    }

    private Process startDevServer(String projectPath, String runCommand, int port) throws Exception {
        String modifiedCommand = modifyPortInCommand(runCommand, port);

        ProcessBuilder pb = createProcessBuilder(projectPath, modifiedCommand);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        Thread.sleep(2000);

        if (!process.isAlive()) {
            throw new RuntimeException("Dev server failed to start");
        }

        return process;
    }

    private ProcessBuilder createProcessBuilder(String projectPath, String command) {
        ProcessBuilder pb;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows")) {
            pb = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            pb = new ProcessBuilder("sh", "-c", command);
        }

        pb.directory(new File(projectPath));
        pb.environment().put("PORT", String.valueOf(findAvailablePort(3000)));

        return pb;
    }

    private String modifyPortInCommand(String command, int port) {
        if (command.contains("npm start") || command.contains("npm run dev")) {
            return command;
        }

        if (command.contains("flask run")) {
            return command + " --port " + port;
        }

        if (command.contains("manage.py runserver")) {
            return command + " " + port;
        }

        if (command.contains("uvicorn")) {
            return command + " --port " + port;
        }

        if (command.contains("serve")) {
            return command + " -p " + port;
        }

        return command;
    }

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

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}