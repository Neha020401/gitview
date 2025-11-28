package com.example.gitLive.service;

import com.example.gitLive.model.BranchPreview;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PreviewService {
    private final Map<String, BranchPreview> previews = new HashMap<>();
    private final Map<String, Process> processes = new HashMap<>();

    // Add both preview and process together
    public void addPreview(String branchName, int port, Process process) {
        String url = "http://localhost:" + port + "/";
        previews.put(branchName, new BranchPreview(branchName, url, port));
        processes.put(branchName, process);
    }

    public boolean stopPreview(String branchName) {
        Process process = processes.get(branchName);
        if (process != null && process.isAlive()) {
            process.destroy(); // Kill npm/live-server
            processes.remove(branchName);
            previews.remove(branchName);
            return true;
        }
        return false;
    }

    public Collection<BranchPreview> getAllPreviews() {
        return previews.values();
    }

    public Optional<BranchPreview> getPreview(String branchName) {
        return Optional.ofNullable(previews.get(branchName));
    }
}
