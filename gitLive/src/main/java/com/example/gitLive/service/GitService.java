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

    public static final String DEFAULT_BASE_DIR = "/tmp/gitviewer/";

    public void cloneOrPull(String repoUrl, String branchName, String baseDir) throws Exception {
        File baseDirectory = new File(baseDir);

        if (!baseDirectory.exists()) {
            System.out.println("Creating base directory: " + baseDir);
            baseDirectory.mkdirs(); // create if missing
        }

        File targetDir = new File(baseDirectory, branchName);

        if (!targetDir.exists()) {
            System.out.println("Cloning branch " + branchName + " into " + baseDir);
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


    private String getNpmCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "npx.cmd";  // Windows needs .cmd
        } else {
            return "npx";      // Linux / Mac
        }
    }

    private void serveDirectory(File branchDir, String branchName) throws IOException, InterruptedException {
        int port = 3000 + Math.abs(branchName.hashCode() % 1000); // React usually runs on 3000+
        System.out.println("Serving React app for branch " + branchName + " on port " + port);

        // Check if it's a React project by looking at package.json
        File packageJson = new File(branchDir, "package.json");
        if (!packageJson.exists()) {
            throw new RuntimeException("Not a Node/React project (missing package.json)");
        }

        String npmCommand = getNpmCommand();

        Process install = new ProcessBuilder(npmCommand, "install")
                .directory(branchDir)
                .inheritIO()
                .start();
        int installExit = install.waitFor();
        if (installExit != 0) {
            throw new RuntimeException("npm install failed for branch: " + branchName);
        }

        new ProcessBuilder(npmCommand, "start")
                .directory(branchDir)
                .inheritIO()
                .start();

        previewService.addPreview(branchName, port);
    }
}