package com.example.gitLive.controller;

import com.example.gitLive.service.GitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final GitService gitService;

    public WebhookController(GitService gitService) {
        this.gitService = gitService;
    }

    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String branchRef = (String) payload.get("ref"); // Example: "refs/heads/feature-login"
            if (branchRef == null) {
                return ResponseEntity.badRequest().body("Error: 'ref' field is missing from payload");
            }
            String branchName = branchRef.replace("refs/heads/", "");

            @SuppressWarnings("unchecked")
            Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
            if (repo == null || repo.get("clone_url") == null) {
                return ResponseEntity.badRequest().body("Error: 'repository.clone_url' is missing from payload");
            }
            String repoUrl = repo.get("clone_url").toString();

            gitService.cloneOrPull(repoUrl, branchName, GitService.DEFAULT_BASE_DIR);

            return ResponseEntity.ok("Branch updated and served!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
