package com.example.gitView.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * TechStack - Technology Stack Model
 * ============================================================================
 * 
 * PURPOSE:
 * Represents the detected technology stack of a project.
 * Contains all information needed to install dependencies and run the project.
 * 
 * FIELDS:
 * - type: Internal identifier for the tech stack (e.g., "react", "node")
 * - displayName: Human-readable name shown in UI (e.g., "React.js")
 * - installCommand: Command to install dependencies (e.g., "npm install")
 * - runCommand: Command to start dev server (e.g., "npm start")
 * - defaultPort: Default port the dev server runs on
 * - icon: Emoji icon for UI display
 * 
 * USAGE:
 * TechStack stack = TechStack.react();
 * String cmd = stack.getInstallCommand(); // "npm install"
 * 
 * ============================================================================
 */
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@AllArgsConstructor // Lombok: Generates constructor with all fields
@NoArgsConstructor // Lombok: Generates empty constructor (needed for JSON serialization)
public class TechStack {

    /**
     * Internal type identifier
     * Examples: "react", "node", "python", "java-maven", "static", "unknown"
     * Used for CSS class names and internal logic
     */
    private String type;

    /**
     * Human-readable display name
     * Examples: "React.js", "Node.js", "Python", "Java (Maven)"
     * Shown in the UI tech stack badge
     */
    private String displayName;

    /**
     * Command to install project dependencies
     * Examples: "npm install", "pip install -r requirements.txt", "mvn clean
     * install"
     * Run before starting the dev server
     */
    private String installCommand;

    /**
     * Command to start the development server
     * Examples: "npm start", "npm run dev", "flask run", "python app.py"
     * This command starts the local preview server
     */
    private String runCommand;

    /**
     * Default port number for the dev server
     * Examples: 3000 (React), 5173 (Vite), 5000 (Flask), 8080 (Spring Boot)
     * May be overridden if port is already in use
     */
    private int defaultPort;

    /**
     * Emoji icon for UI display
     * Examples: "⚛️" (React), "🟢" (Node), "🐍" (Python), "☕" (Java)
     * Shown next to the tech stack name in the badge
     */
    private String icon;

    // ========================================================================
    // FACTORY METHODS
    // These create pre-configured TechStack objects for common project types
    // ========================================================================

    /**
     * Create TechStack for React.js projects (Create React App)
     * Detected when: package.json contains "react-scripts" dependency
     */
    public static TechStack react() {
        return new TechStack("react", "React.js", "npm install", "npm start", 3000, "⚛️");
    }

    /**
     * Create TechStack for Next.js projects
     * Detected when: package.json contains "next" dependency
     */
    public static TechStack nextjs() {
        return new TechStack("nextjs", "Next.js", "npm install", "npm run dev", 3000, "▲");
    }

    /**
     * Create TechStack for Vite projects
     * Detected when: package.json contains "vite" in devDependencies
     */
    public static TechStack vite() {
        return new TechStack("vite", "Vite", "npm install", "npm run dev", 5173, "⚡");
    }

    /**
     * Create TechStack for plain Node.js projects
     * Detected when: package.json exists but no framework detected
     */
    public static TechStack node() {
        return new TechStack("node", "Node.js", "npm install", "npm start", 3000, "🟢");
    }

    /**
     * Create TechStack for Python projects
     * Detected when: requirements.txt, setup.py, or pyproject.toml exists
     */
    public static TechStack python() {
        return new TechStack("python", "Python", "pip install -r requirements.txt", "python app.py", 5000, "🐍");
    }

    /**
     * Create TechStack for Java Maven projects
     * Detected when: pom.xml exists
     */
    public static TechStack javaMaven() {
        return new TechStack("java-maven", "Java (Maven)", "mvn clean install", "mvn spring-boot:run", 8080, "☕");
    }

    /**
     * Create TechStack for Java Gradle projects
     * Detected when: build.gradle or build.gradle.kts exists
     */
    public static TechStack javaGradle() {
        return new TechStack("java-gradle", "Java (Gradle)", "gradle build", "gradle bootRun", 8080, "☕");
    }

    /**
     * Create TechStack for static HTML projects
     * Detected when: index.html exists but no other project files
     * Uses 'npx serve' to start a simple static file server
     */
    public static TechStack staticHtml() {
        return new TechStack("static", "Static HTML", "", "npx serve -s .", 3000, "📄");
    }

    /**
     * Create TechStack for unknown/unsupported projects
     * Used when no recognizable project files are found
     * Cannot be run - Run button will be disabled
     */
    public static TechStack unknown() {
        return new TechStack("unknown", "Unknown", "", "", 0, "❓");
    }
}
