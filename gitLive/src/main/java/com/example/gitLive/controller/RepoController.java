package com.example.gitLive.controller;

import com.example.gitLive.service.GitService;
import com.example.gitLive.service.PreviewService;
import com.example.gitLive.model.BranchPreview;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // allow React frontend
public class RepoController {

    private final GitService gitService;
    private final PreviewService previewService;

    public RepoController(GitService gitService, PreviewService previewService) {
        this.gitService = gitService;
        this.previewService = previewService;
    }

    // Add repo + branch
    @PostMapping("/repos")
    public BranchPreview addRepo(@RequestParam String repoUrl,
                                 @RequestParam String branchName) throws Exception {
        gitService.cloneOrPull(repoUrl, branchName);
        return previewService.getPreview(branchName).orElseThrow();
    }

    // List all previews
    @GetMapping("/previews")
    public Collection<BranchPreview> getPreviews() {
        return previewService.getAllPreviews();
    }

    // Get one preview
    @GetMapping("/previews/{branch}")
    public BranchPreview getPreview(@PathVariable String branch) {
        return previewService.getPreview(branch)
                .orElseThrow(() -> new RuntimeException("Preview not found"));
    }
}
