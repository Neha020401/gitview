package com.example.gitLive.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitService {

    // Use system temp directory for cross-platform compatibility
    public static final String DEFAULT_BASE_DIR = System.getProperty("java.io.tmpdir") + File.separator + "gitviewer"
            + File.separator;

    public GitService() {
        // No dependencies needed - clone only functionality
    }

    public String cloneOrPull(String repoUrl, String branchName, String baseDir) throws Exception {
        File baseDirectory = new File(baseDir);

        if (!baseDirectory.exists()) {
            System.out.println("Creating base directory: " + baseDir);
            if (!baseDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create base directory: " + baseDir);
            }
        }

        File targetDir = new File(baseDirectory, branchName);

        if (!targetDir.exists()) {
            System.out.println("Cloning branch " + branchName + " into " + targetDir.getAbsolutePath());
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setBranch(branchName)
                    .setDirectory(targetDir)
                    .call();
            System.out.println("Clone completed successfully!");
        } else {
            System.out.println("Pulling branch " + branchName + " in " + targetDir.getAbsolutePath());
            try (Git git = Git.open(targetDir)) {
                git.checkout().setName(branchName).call();
                git.pull().call();
            }
            System.out.println("Pull completed successfully!");
        }

        return targetDir.getAbsolutePath();
    }
}
