package com.example.gitLive.service;

import com.example.gitLive.model.BranchPreview;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ============================================================================
 * PreviewService - Branch Preview Management Service
 * ============================================================================
 * 
 * PURPOSE:
 * This service manages live previews of different Git branches.
 * Each branch can be served on a different localhost port, allowing
 * developers to compare multiple branches side-by-side in their browser.
 * 
 * MAIN FEATURES:
 * 1. Register new preview servers (store URL, port, and process reference)
 * 2. Stop running preview servers (kill the process)
 * 3. Retrieve information about active previews
 * 
 * HOW IT WORKS:
 * - When a branch is previewed, a local server (npm/live-server) starts
 * - The server process and metadata are stored in memory
 * - Users can access the preview via http://localhost:{port}/
 * - When stopped, the server process is killed and cleaned up
 * 
 * EXAMPLE USAGE:
 * previewService.addPreview("feature-login", 3001, serverProcess);
 * // Now accessible at http://localhost:3001/
 * 
 * previewService.stopPreview("feature-login");
 * // Server killed, preview removed
 * 
 * ============================================================================
 */
@Service // Marks this class as a Spring Service Bean (singleton by default)
public class PreviewService {

    /**
     * previews - Stores preview metadata indexed by branch name.
     * 
     * Key: Branch name (e.g., "main", "feature-xyz")
     * Value: BranchPreview object containing URL, port, and branch info
     * 
     * Example state:
     * {
     * "main" → BranchPreview("main", "http://localhost:3000/", 3000),
     * "develop" → BranchPreview("develop", "http://localhost:3001/", 3001)
     * }
     */
    private final Map<String, BranchPreview> previews = new HashMap<>();

    /**
     * processes - Stores running server processes indexed by branch name.
     * 
     * Key: Branch name (e.g., "main", "feature-xyz")
     * Value: Java Process object representing the running server
     * 
     * These Process objects are needed to:
     * - Check if the server is still running (isAlive())
     * - Stop the server when requested (destroy())
     */
    private final Map<String, Process> processes = new HashMap<>();

    /**
     * ========================================================================
     * addPreview - Register a new preview server for a branch
     * ========================================================================
     * 
     * Called when a new local server is started to preview a branch.
     * Stores both the preview metadata and the process reference.
     * 
     * @param branchName - The Git branch being previewed (e.g., "feature-login")
     *                   Used as the key to look up this preview later
     * 
     * @param port       - The localhost port where the server is running
     *                   Example: 3000, 3001, 8080
     *                   Each branch should use a unique port
     * 
     * @param process    - The Java Process object for the running server
     *                   This is the actual npm/live-server process
     *                   Needed to check status and stop the server later
     * 
     *                   WHAT GETS STORED:
     *                   - previews map: branchName → BranchPreview(branchName, URL,
     *                   port)
     *                   - processes map: branchName → Process object
     */
    public void addPreview(String branchName, int port, Process process) {
        // Construct the full URL for accessing the preview
        String url = "http://localhost:" + port + "/";

        // Store the preview metadata (for API responses)
        previews.put(branchName, new BranchPreview(branchName, url, port));

        // Store the process reference (for lifecycle management)
        processes.put(branchName, process);
    }

    /**
     * ========================================================================
     * stopPreview - Stop a running preview server for a branch
     * ========================================================================
     * 
     * Kills the server process and removes all associated data.
     * 
     * @param branchName - The Git branch whose preview should be stopped
     * 
     * @return boolean - true if a running server was found and stopped
     *         - false if no running server exists for this branch
     * 
     *         LOGIC FLOW:
     *         1. Look up the process for the given branch name
     *         2. Check if process exists AND is still running
     *         3. If yes: destroy process, clean up maps, return true
     *         4. If no: return false (nothing to stop)
     * 
     *         NOTE: process.destroy() sends SIGTERM to the process, which should
     *         gracefully shut down npm/live-server and free up the port.
     */
    public boolean stopPreview(String branchName) {
        // Get the process for this branch (may be null if not found)
        Process process = processes.get(branchName);

        // Only proceed if process exists and is still running
        if (process != null && process.isAlive()) {
            // Kill the server process (npm/live-server)
            process.destroy();

            // Clean up both maps to remove all traces of this preview
            processes.remove(branchName);
            previews.remove(branchName);

            return true; // Successfully stopped
        }

        return false; // No running preview found for this branch
    }

    /**
     * ========================================================================
     * getAllPreviews - Get all currently active previews
     * ========================================================================
     * 
     * Returns a collection of all preview metadata objects.
     * Useful for displaying a list of all active previews in a dashboard.
     * 
     * @return Collection<BranchPreview> - All active previews
     *         Example: [BranchPreview("main", "http://localhost:3000/", 3000),
     *         BranchPreview("develop", "http://localhost:3001/", 3001)]
     * 
     *         NOTE: This returns the values only, not the keys (branch names).
     *         However, each BranchPreview contains its own branchName field.
     */
    public Collection<BranchPreview> getAllPreviews() {
        return previews.values();
    }

    /**
     * ========================================================================
     * getPreview - Get preview details for a specific branch
     * ========================================================================
     * 
     * Retrieves the preview metadata for a single branch.
     * 
     * @param branchName - The Git branch to look up
     * 
     * @return Optional<BranchPreview> - The preview if found, empty if not
     *         Using Optional avoids null pointer issues and makes the
     *         "might not exist" case explicit in the API.
     * 
     *         USAGE EXAMPLE:
     *         Optional<BranchPreview> preview = previewService.getPreview("main");
     * 
     *         if (preview.isPresent()) {
     *         String url = preview.get().getUrl();
     *         } else {
     *         // No preview exists for "main"
     *         }
     * 
     *         // Or using modern Java:
     *         preview.ifPresent(p -> System.out.println("URL: " + p.getUrl()));
     */
    public Optional<BranchPreview> getPreview(String branchName) {
        // Optional.ofNullable handles null gracefully:
        // - If key exists: returns Optional.of(value)
        // - If key doesn't exist: returns Optional.empty()
        return Optional.ofNullable(previews.get(branchName));
    }
}
