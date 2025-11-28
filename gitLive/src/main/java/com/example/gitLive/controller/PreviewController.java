package com.example.gitLive.controller;

import com.example.gitLive.model.BranchPreview;
import com.example.gitLive.service.PreviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
@RestController
@RequestMapping("/api/previews")
public class PreviewController {

    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    @GetMapping
    public Collection<BranchPreview> getAll() {
        return previewService.getAllPreviews();
    }

    @GetMapping("/{branch}")
    public BranchPreview getOne(@PathVariable String branch) {
        return previewService.getPreview(branch)
                .orElseThrow(() -> new RuntimeException("Preview not found"));
    }

    @DeleteMapping("/{branch}")
    public ResponseEntity<String> stopPreview(@PathVariable String branch) {
        boolean stopped = previewService.stopPreview(branch);
        if (!stopped) {
            throw new RuntimeException("Preview not running or not found");
        }
        return ResponseEntity.ok("Stopped preview for branch: " + branch);
    }

}
