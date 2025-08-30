package com.example.gitLive.model;

import lombok.Data;

@Data
public class BranchPreview {
    private String branchName;
    private String previewUrl;
    private int port;

    public BranchPreview(String branchName, String previewUrl, int port) {
        this.branchName = branchName;
        this.previewUrl = previewUrl;
        this.port = port;
    }

  }
