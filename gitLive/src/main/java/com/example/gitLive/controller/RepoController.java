package com.example.gitLive.controller;

import com.example.gitLive.service.GitService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/repos")
@CrossOrigin(origins = "*") // allow any frontend
public class RepoController {

    private final GitService gitService;

    public RepoController(GitService gitService) {
        this.gitService = gitService;
    }

    @PostMapping
    public Map<String, String> addRepo(@RequestParam String repoUrl,
            @RequestParam String branchName,
            @RequestParam(required = false) String baseDir) throws Exception {
        if (baseDir == null || baseDir.isBlank()) {
            baseDir = GitService.DEFAULT_BASE_DIR;
        }

        System.out.println("Received request:");
        System.out.println("  Repo URL: " + repoUrl);
        System.out.println("  Branch: " + branchName);
        System.out.println("  BaseDir: " + baseDir);

        String clonedPath = gitService.cloneOrPull(repoUrl, branchName, baseDir);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("branchName", branchName);
        response.put("clonedPath", clonedPath);
        return response;
    }
}
