package com.example.gitView.service;

import com.example.gitView.model.TechStack;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * ============================================================================
 * TechStackDetector - Technology Stack Detection Service
 * ============================================================================
 * 
 * PURPOSE:
 * Analyzes a project directory to automatically detect what technology stack
 * is being used (React, Node.js, Python, Java, etc.)
 * 
 * HOW IT WORKS:
 * 1. Checks for presence of specific files (package.json, requirements.txt,
 * etc.)
 * 2. For Node.js projects, parses package.json to find framework dependencies
 * 3. Returns a TechStack object with appropriate run commands
 * 
 * DETECTION PRIORITY:
 * 1. package.json → React, Next.js, Vite, Vue, Angular, Express, Node
 * 2. requirements.txt / setup.py → Python, Flask, Django, FastAPI
 * 3. pom.xml → Java Maven
 * 4. build.gradle → Java Gradle
 * 5. index.html → Static HTML
 * 6. None of above → Unknown
 * 
 * USAGE:
 * TechStack stack = techStackDetector.detect("/path/to/project");
 * System.out.println(stack.getDisplayName()); // "React.js"
 * 
 * ============================================================================
 */
@Service // Spring annotation: Makes this class a singleton bean
public class TechStackDetector {

    /**
     * Jackson ObjectMapper for parsing JSON files (package.json)
     * Reused across all detections for efficiency
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Main detection method - analyzes a project directory
     * 
     * @param projectPath - Absolute path to the project directory
     * @return TechStack - Detected technology stack with run commands
     * 
     *         DETECTION FLOW:
     *         1. Verify directory exists
     *         2. Check for Node.js (package.json)
     *         3. Check for Python (requirements.txt, setup.py, pyproject.toml)
     *         4. Check for Java Maven (pom.xml)
     *         5. Check for Java Gradle (build.gradle)
     *         6. Check for Static HTML (index.html)
     *         7. Return unknown if nothing matches
     */
    public TechStack detect(String projectPath) {
        File projectDir = new File(projectPath);

        // Validate directory exists
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            return TechStack.unknown();
        }

        // ========================================
        // CHECK FOR NODE.JS / JAVASCRIPT PROJECTS
        // package.json is the definitive marker
        // ========================================
        File packageJson = new File(projectDir, "package.json");
        if (packageJson.exists()) {
            return detectNodeProject(packageJson);
        }

        // ========================================
        // CHECK FOR PYTHON PROJECTS
        // Look for dependency files
        // ========================================
        File requirementsTxt = new File(projectDir, "requirements.txt");
        File setupPy = new File(projectDir, "setup.py");
        File pyprojectToml = new File(projectDir, "pyproject.toml");
        if (requirementsTxt.exists() || setupPy.exists() || pyprojectToml.exists()) {
            return detectPythonProject(projectDir);
        }

        // ========================================
        // CHECK FOR JAVA MAVEN PROJECTS
        // pom.xml is the Maven project file
        // ========================================
        File pomXml = new File(projectDir, "pom.xml");
        if (pomXml.exists()) {
            return TechStack.javaMaven();
        }

        // ========================================
        // CHECK FOR JAVA GRADLE PROJECTS
        // build.gradle or build.gradle.kts
        // ========================================
        File buildGradle = new File(projectDir, "build.gradle");
        File buildGradleKts = new File(projectDir, "build.gradle.kts");
        if (buildGradle.exists() || buildGradleKts.exists()) {
            return TechStack.javaGradle();
        }

        // ========================================
        // CHECK FOR STATIC HTML PROJECTS
        // Just an index.html with no framework
        // ========================================
        File indexHtml = new File(projectDir, "index.html");
        if (indexHtml.exists()) {
            return TechStack.staticHtml();
        }

        // No recognizable project structure found
        return TechStack.unknown();
    }

    /**
     * Detect specific Node.js framework from package.json
     * 
     * @param packageJson - The package.json file to analyze
     * @return TechStack - The detected Node framework
     * 
     *         DETECTION PRIORITY:
     *         1. Next.js (next in dependencies)
     *         2. Vite (vite in devDependencies)
     *         3. React/CRA (react-scripts in dependencies)
     *         4. Vue.js (vue in dependencies)
     *         5. Angular (@angular/core in dependencies)
     *         6. Express.js (express in dependencies)
     *         7. Plain Node.js (default fallback)
     */
    private TechStack detectNodeProject(File packageJson) {
        try {
            // Read and parse the package.json file
            String content = Files.readString(packageJson.toPath());
            JsonNode root = objectMapper.readTree(content);

            // Get both dependencies and devDependencies
            JsonNode dependencies = root.get("dependencies");
            JsonNode devDependencies = root.get("devDependencies");

            // Check for Next.js (SSR React framework)
            if (hasDependency(dependencies, "next") || hasDependency(devDependencies, "next")) {
                return TechStack.nextjs();
            }

            // Check for Vite (fast build tool)
            if (hasDependency(devDependencies, "vite")) {
                return TechStack.vite();
            }

            // Check for React (Create React App or plain React)
            if (hasDependency(dependencies, "react")) {
                // Check if it's Create React App by looking for react-scripts
                if (hasDependency(dependencies, "react-scripts") || hasDependency(devDependencies, "react-scripts")) {
                    return TechStack.react();
                }
                // Plain React with Vite or other bundler
                return TechStack.react();
            }

            // Check for Vue.js
            if (hasDependency(dependencies, "vue")) {
                return new TechStack("vue", "Vue.js", "npm install", "npm run dev", 5173, "💚");
            }

            // Check for Angular
            if (hasDependency(dependencies, "@angular/core")) {
                return new TechStack("angular", "Angular", "npm install", "ng serve", 4200, "🅰️");
            }

            // Check for Express.js (backend framework)
            if (hasDependency(dependencies, "express")) {
                return new TechStack("express", "Express.js", "npm install", "npm start", 3000, "🚀");
            }

            // Default to generic Node.js
            return TechStack.node();

        } catch (IOException e) {
            System.err.println("Error reading package.json: " + e.getMessage());
            return TechStack.node(); // Fallback to Node.js on error
        }
    }

    /**
     * Detect Python framework by analyzing requirements.txt
     * 
     * @param projectDir - The project directory to analyze
     * @return TechStack - The detected Python framework
     * 
     *         DETECTION:
     *         - Searches requirements.txt for framework names
     *         - Flask, Django, and FastAPI have specific run commands
     *         - Falls back to generic Python if no framework found
     */
    private TechStack detectPythonProject(File projectDir) {
        File requirementsTxt = new File(projectDir, "requirements.txt");

        if (requirementsTxt.exists()) {
            try {
                // Read requirements and search for framework names
                String content = Files.readString(requirementsTxt.toPath()).toLowerCase();

                // Check for Flask web framework
                if (content.contains("flask")) {
                    return new TechStack("flask", "Flask", "pip install -r requirements.txt", "flask run", 5000, "🌶️");
                }

                // Check for Django web framework
                if (content.contains("django")) {
                    return new TechStack("django", "Django", "pip install -r requirements.txt",
                            "python manage.py runserver", 8000, "🎸");
                }

                // Check for FastAPI (async web framework)
                if (content.contains("fastapi")) {
                    return new TechStack("fastapi", "FastAPI", "pip install -r requirements.txt",
                            "uvicorn main:app --reload", 8000, "⚡");
                }
            } catch (IOException e) {
                System.err.println("Error reading requirements.txt: " + e.getMessage());
            }
        }

        // Determine the main Python file to run
        // Check for common entry point names
        File appPy = new File(projectDir, "app.py");
        File mainPy = new File(projectDir, "main.py");

        String runCommand = "python app.py"; // Default
        if (mainPy.exists() && !appPy.exists()) {
            runCommand = "python main.py";
        }

        // Generic Python project
        return new TechStack("python", "Python", "pip install -r requirements.txt", runCommand, 5000, "🐍");
    }

    /**
     * Helper method to check if a dependency exists in a JsonNode
     * 
     * @param dependencies - JsonNode containing dependencies object
     * @param name         - Name of the dependency to check for
     * @return true if dependency exists, false otherwise
     * 
     *         Handles null safely - returns false if dependencies is null
     */
    private boolean hasDependency(JsonNode dependencies, String name) {
        return dependencies != null && dependencies.has(name);
    }
}
