package com.example.gitLive.controller;

import com.example.gitLive.service.GitService;
import com.example.gitLive.service.PreviewService;
import com.example.gitLive.model.BranchPreview;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
@RestController
@RequestMapping("/api/repos")
@CrossOrigin(origins = "http://localhost:3000") // allow React frontend
public class RepoController {

    private final GitService gitService;
    private final PreviewService previewService;

    public RepoController(GitService gitService, PreviewService previewService) {
        this.gitService = gitService;
        this.previewService = previewService;
    }

    // Add repo + branch
    @PostMapping
    public BranchPreview addRepo(@RequestParam String repoUrl,
                                 @RequestParam String branchName) throws Exception {
        gitService.cloneOrPull(repoUrl, branchName);
        return previewService.getPreview(branchName).orElseThrow();
    }
}
