package com.example.gitView.repository;

import com.example.gitView.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    // Spring Data JPA will auto-implement basic CRUD operations
    // You can add custom queries if needed, for example:
    // List<Project> findByRunning(boolean running);
}