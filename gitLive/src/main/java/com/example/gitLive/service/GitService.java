package com.example.gitLive.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class GitService {

    public static final String DEFAULT_BASE_DIR = "/tmp/gitviewer/";

    private final PreviewService previewService;

    public GitService(PreviewService previewService) {
        this.previewService = previewService;
    }

    public void cloneOrPull(String repoUrl, String branchName, String baseDir) throws Exception {
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
        } else {
            System.out.println("Pulling branch " + branchName + " in " + targetDir.getAbsolutePath());
            try (Git git = Git.open(targetDir)) {
                git.checkout().setName(branchName).call();
                git.pull().call();
            }
        }

        // After code is updated locally, register it for preview
        serveDirectory(targetDir, branchName);
    }

    /**
     * Generic "preview registration" – no tech-stack assumptions.
     * Let PreviewService decide how to expose this directory.
     */
    private void serveDirectory(File branchDir, String branchName) {
        // You can still keep the "port" idea if your UI expects it,
        // or treat it as a logical ID.
        int port = 3000 + Math.abs(branchName.hashCode() % 1000);
        System.out.println("Registering preview for branch " + branchName +
                " at " + branchDir.getAbsolutePath() + " (logical port " + port + ")");

        // If PreviewService.addPreview expects a Process because before we started
        // a dev server, you can pass null or overload the method.
        previewService.addPreview(branchName, port, null);
    }
}
