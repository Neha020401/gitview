package com.example.gitView.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * ============================================================================
 * GitService - Core Git Operations Service
 * ============================================================================
 * 
 * PURPOSE:
 * This service handles all Git-related operations using the JGit library.
 * JGit is a pure Java implementation of Git, allowing us to clone and pull
 * repositories without needing Git installed on the system.
 * 
 * MAIN FEATURES:
 * 1. Clone a specific branch from a remote Git repository
 * 2. Pull latest changes if the branch already exists locally
 * 3. Cross-platform compatible (Windows, Mac, Linux)
 * 
 * USAGE EXAMPLE:
 * gitService.cloneOrPull("https://github.com/user/repo.git", "main",
 * "/path/to/dir");
 * 
 * ============================================================================
 */
@Service // Marks this class as a Spring Service Bean (singleton by default)
public class GitService {

    /**
     * DEFAULT_BASE_DIR - The default directory where repositories will be cloned.
     * 
     * Uses the user's Downloads folder for better accessibility:
     * - Windows: C:\Users\{username}\Downloads\gitviewer\
     * - Linux/Mac: /home/{username}/Downloads/gitviewer/
     * 
     * File.separator ensures correct path separators for each OS (\ or /)
     */
    public static final String DEFAULT_BASE_DIR = System.getProperty("user.home") + File.separator + "Downloads"
            + File.separator + "gitviewer" + File.separator;

    /**
     * Default constructor.
     * No dependencies are injected - this service is self-contained.
     */
    public GitService() {
        // No dependencies needed - clone only functionality
    }

    /**
     * ========================================================================
     * cloneOrPull - Main method to clone or update a Git repository
     * ========================================================================
     * 
     * LOGIC FLOW:
     * 1. Create base directory if it doesn't exist
     * 2. Check if target branch folder exists:
     * - If NOT exists → Clone the repository (fresh download)
     * - If EXISTS → Pull latest changes (update existing)
     * 3. Return the absolute path to the cloned/updated directory
     * 
     * @param repoUrl    - The Git repository URL (HTTPS or SSH)
     *                   Example: "https://github.com/username/repo.git"
     * 
     * @param branchName - The specific branch to clone/pull
     *                   Example: "main", "develop", "feature-xyz"
     *                   This also becomes the folder name for the clone
     * 
     * @param baseDir    - The parent directory where branch folders are created
     *                   Example: "C:/temp/gitviewer/"
     *                   Each branch gets its own subfolder: baseDir/branchName
     * 
     * @return String - The absolute path to the cloned/updated repository
     *         Example: "C:/temp/gitviewer/main"
     * 
     * @throws Exception - If Git operations fail (network issues, auth problems,
     *                   etc.)
     * 
     *                   DIRECTORY STRUCTURE CREATED:
     *                   baseDir/
     *                   ├── main/ ← Cloned "main" branch
     *                   │ ├── .git/
     *                   │ └── [project files]
     *                   ├── develop/ ← Cloned "develop" branch
     *                   │ ├── .git/
     *                   │ └── [project files]
     *                   └── feature-xyz/ ← Cloned "feature-xyz" branch
     *                   ├── .git/
     *                   └── [project files]
     */
    public String cloneOrPull(String repoUrl, String branchName, String baseDir) throws Exception {

        // Step 1: Create the base directory if it doesn't exist
        File baseDirectory = new File(baseDir);

        if (!baseDirectory.exists()) {
            System.out.println("Creating base directory: " + baseDir);
            // mkdirs() creates all necessary parent directories too
            if (!baseDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create base directory: " + baseDir);
            }
        }

        // Step 2: Define the target directory for this specific branch
        // Each branch gets its own folder: baseDir/branchName
        File targetDir = new File(baseDirectory, branchName);

        // Step 3: Clone or Pull based on whether directory exists
        if (!targetDir.exists()) {
            // ============ CLONE OPERATION ============
            // Target folder doesn't exist → Fresh clone needed
            System.out.println("Cloning branch " + branchName + " into " + targetDir.getAbsolutePath());

            // JGit clone command (equivalent to: git clone -b branchName repoUrl targetDir)
            Git.cloneRepository()
                    .setURI(repoUrl) // Set the remote repository URL
                    .setBranch(branchName) // Specify which branch to clone
                    .setDirectory(targetDir) // Set local destination folder
                    .call(); // Execute the clone operation

            System.out.println("Clone completed successfully!");
        } else {
            // ============ PULL OPERATION ============
            // Target folder exists → Update with latest changes
            System.out.println("Pulling branch " + branchName + " in " + targetDir.getAbsolutePath());

            // Open existing Git repository (try-with-resources auto-closes Git object)
            try (Git git = Git.open(targetDir)) {
                // First, checkout the correct branch (ensures we're on the right branch)
                git.checkout().setName(branchName).call();

                // Then pull latest changes from remote
                git.pull().call();
            }
            // Note: Git object is automatically closed after this block

            System.out.println("Pull completed successfully!");
        }

        // Step 4: Return the path where the repository now exists
        return targetDir.getAbsolutePath();
    }
}
