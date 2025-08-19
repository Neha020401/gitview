package com.example.gitLive.model;

public class BranchPreview {
    private String branchName;
    private String previewUrl;
    private int port;

    // Constructor
    public BranchPreview(String branchName, String previewUrl, int port) {
        this.branchName = branchName;
        this.previewUrl = previewUrl;
        this.port = port;
    }

    // Getters and Setters
    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
