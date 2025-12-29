package com.example.gitView.controller;

import com.example.gitView.model.Project;
import com.example.gitView.service.GitService;
import com.example.gitView.service.ProjectRunner;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/repos")
@CrossOrigin(origins = "*") // allow any frontend
public class RepoController {

    private final GitService gitService;
    private final ProjectRunner projectRunner;

    public RepoController(GitService gitService, ProjectRunner projectRunner) {
        this.gitService = gitService;
        this.projectRunner = projectRunner;
    }

    @PostMapping
    public Map<String, Object> addRepo(@RequestParam String repoUrl,
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

        // Register the project with tech stack detection
        Project project = projectRunner.registerProject(branchName, clonedPath, repoUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("branchName", branchName);
        response.put("clonedPath", clonedPath);
        response.put("techStack", project.getTechStack());
        return response;
    }
}
