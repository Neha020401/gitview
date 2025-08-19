package com.example.gitLive.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class GitService {


    private final PreviewService previewService ;

    public GitService(PreviewService previewService) {
        this.previewService = previewService;
    }

    private static final String BASE_DIR = "/tmp/gitviewer/";  // Where branches will be cloned

    public void cloneOrPull(String repoUrl, String branchName) throws Exception {
        File targetDir = new File(BASE_DIR + branchName);

        if (!targetDir.exists()) {
            System.out.println("Cloning branch " + branchName);
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setBranch(branchName)
                    .setDirectory(targetDir)
                    .call();
        } else {
            System.out.println("Pulling branch " + branchName);
            try (Git git = Git.open(targetDir)) {
                git.checkout().setName(branchName).call();
                git.pull().call();
            }
        }

        serveDirectory(targetDir, branchName);
    }

    private void serveDirectory(File branchDir, String branchName) throws IOException {
        int port = 9000 + Math.abs(branchName.hashCode() % 1000);
        System.out.println("Serving branch on port " + port);

        new ProcessBuilder("npx", "live-server", "--port=" + port)
                .directory(branchDir)
                .inheritIO()
                .start();

        previewService.addPreview(branchName, port);

    }
}

