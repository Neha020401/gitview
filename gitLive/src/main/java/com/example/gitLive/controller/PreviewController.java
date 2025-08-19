package com.example.gitLive.controller;

import com.example.gitLive.model.BranchPreview;
import com.example.gitLive.service.PreviewService;
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
}
