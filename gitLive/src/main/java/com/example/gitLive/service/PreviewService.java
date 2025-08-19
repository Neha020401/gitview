package com.example.gitLive.service;



import com.example.gitLive.model.BranchPreview;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PreviewService {
    private final Map<String, BranchPreview> previews = new HashMap<>();

    public void addPreview(String branchName, int port) {
        String url = "http://localhost:" + port + "/";
        previews.put(branchName, new BranchPreview(branchName, url, port));
    }

    public Collection<BranchPreview> getAllPreviews() {
        return previews.values();
    }

    public Optional<BranchPreview> getPreview(String branchName) {
        return Optional.ofNullable(previews.get(branchName));
    }
}

