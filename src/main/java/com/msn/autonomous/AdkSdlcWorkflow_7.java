package com.msn.autonomous;

// Google ADK Java Console App: Full SDLC Flow with Spring Boot Microservice Code Generation, File Output, Zip Packaging, Git Init, and GitHub Push and CI CONFIG
// Here Agent Read data from SRS document
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdkSdlcWorkflow_7 {

    private static final Logger logger = LoggerFactory.getLogger(AdkSdlcWorkflow_7.class);

    // --- Constants for Agent Configuration ---
    private static final String REQUIREMENTS_AGENT_NAME = "RequirementsAgent";
    private static final String DEPENDENCY_AGENT_NAME = "DependencyAgent";
    private static final String CODEGEN_AGENT_NAME = "CodeGenAgent";
    private static final String TESTGEN_AGENT_NAME = "TestGenAgent";
    private static final String CHANGE_ANALYSIS_AGENT_NAME = "ChangeAnalysisAgent";

    private static final String KEY_REQUIREMENTS = "requirements";
    private static final String KEY_DEPENDENCIES = "dependencies";
    private static final String KEY_CODE = "code";
    private static final String KEY_TEST = "test";
    private static final String KEY_CHANGE_ANALYSIS = "change_analysis";

    // --- Constants for File Parsing and Naming ---
    private static final String DEPS_SEPARATOR = "---END-DEPS---";
    private static final String COMMIT_SUMMARY_PREFIX = "Commit-Summary: ";
    private static final String NO_CHANGES_DETECTED = "No changes detected.";
    private static final String FILE_PATH_MARKER_PREFIX = "// File: ";
    private static final String SRS_KEY_GITHUB_URL = "GitHub-URL";
    private static final String SRS_KEY_CHECKOUT_BRANCH = "checkout_branch";
    private static final String SRS_KEY_REPO_NAME = "Repository-Name";

    // --- Constants for File System and Git ---
    private static final String AI_STATE_DIR = ".ai-state";
    private static final String SRS_FILE_NAME = "srs.txt";
    private static final String CHANGELOG_FILE_NAME = "AI_CHANGELOG.md";


    /**
     * A simple data class to hold the results from the main AI workflow execution.
     * This avoids passing multiple mutable objects through the workflow logic.
     */
    private static class WorkflowResult {
        String commitMessage = "feat: Initial project scaffold by AI agent";
        String requirementsSummary = "";
        String codeAndTestOutput = "";
        final List<String> dependencyList = new ArrayList<>();
    }

    /**
     * A simple data class for storing Git repository configuration.
     */
    private static class GitConfig {
        final String repoUrl;
        final String baseBranch;
        final String repoName;

        GitConfig(String repoUrl, String baseBranch, String repoName) {
            this.repoUrl = repoUrl;
            this.baseBranch = baseBranch;
            this.repoName = repoName;
        }
    }

    /**
     * A simple data class to hold the SRS content and its parsed Git configuration.
     */
    private static class SrsData {
        final GitConfig gitConfig;
        final String srsContent;

        SrsData(GitConfig gitConfig, String srsContent) {
            this.gitConfig = gitConfig;
            this.srsContent = srsContent;
        }
    }

    public static SequentialAgent buildWorkflow() {
        LlmAgent req = LlmAgent.builder()
                .name(REQUIREMENTS_AGENT_NAME)
                .description("Extracts structured functional requirements from a feature prompt.")
                .instruction("""
First, create a one-line summary of the following SRS, formatted as a conventional Git commit message. Prefix it with "Commit-Summary: ".
Then, on new lines, extract the structured requirements from the same SRS.

The structured requirements format is:
Feature:
Input:
Output:
Constraints:
Logic:
""")
                .model("gemini-2.0-flash")
                .outputKey(KEY_REQUIREMENTS)
                .build();

        LlmAgent deps = LlmAgent.builder()
                .name(DEPENDENCY_AGENT_NAME)
                .description("Determines required dependency features from the requirements.")
                .instruction("""
Based on the following requirements, identify the necessary Maven dependencies.
Provide ONLY a list of `groupId:artifactId:scope` tuples, one per line.
Use 'compile' for standard dependencies, 'runtime' for runtime-only, and 'optional' for tools like Lombok.
If scope is 'compile', you can omit it.

After the dependency list, you MUST add a separator line containing exactly "---END-DEPS---".
After the separator, you MUST repeat the original requirements text provided below, exactly and without modification.

Example output format:
org.springframework.boot:spring-boot-starter-web
org.springframework.boot:spring-boot-starter-data-jpa
---END-DEPS---
Feature: User Management API
...
Requirements:
{requirements}
""")
                .model("gemini-2.0-flash")
                .outputKey(KEY_DEPENDENCIES)
                .build();

        LlmAgent code = LlmAgent.builder()
                .name(CODEGEN_AGENT_NAME)
                .description("Generates a complete Spring Boot microservice skeleton based on structured requirements.")
                .instruction("""
You will be provided with text that contains a list of dependencies followed by structured requirements, separated by "---END-DEPS---".
You MUST IGNORE the dependency list and generate the core Java source code based ONLY on the structured requirements that appear AFTER the separator.

Generate the core Java source code for a Spring Boot microservice. Include:
- Controller, Service, Repository, and Entity classes
- REST endpoints with proper annotations
- Validation annotations on fields
- PostgreSQL integration using Spring Data JPA
- Lombok annotations

The structured requirements are:
{requirements}

Wrap each file with proper syntax and include a comment at the top indicating the file path, for example:
// File: src/main/java/com/example/service/UserService.java
""")
                .model("gemini-2.0-flash")
                .outputKey(KEY_CODE)
                .build();

        LlmAgent test = LlmAgent.builder()
                .name(TESTGEN_AGENT_NAME)
                .description("Generates JUnit 5 test cases for a Spring Boot microservice.")
                .instruction("""
Write appropriate JUnit 5 test cases for the Spring Boot controller and service layer based on this code:
{code}

Wrap each test class in Java syntax and include a comment at the top indicating the file path, for example:
// File: src/test/java/com/example/controller/UserControllerTest.java
""")
                .model("gemini-2.0-flash")
                .outputKey(KEY_TEST)
                .build();

        return SequentialAgent.builder()
                .name("FullSpringBootMicroserviceWorkflow")
                .subAgents(req, deps, code, test)
                .build();
    }

    private static String runChangeAnalysisAgent(String oldSrs, String newSrs) {
        logger.info("🤖 Running Change Analysis Agent...");
        LlmAgent changeAgent = LlmAgent.builder()
                .name(CHANGE_ANALYSIS_AGENT_NAME)
                .description("Compares old and new SRS to generate a changelog.")
                .instruction("""
You will be given an old and a new version of a Software Requirements Specification (SRS), separated by markers.
Analyze the differences and generate a concise, human-readable changelog in Markdown format.
Focus on added, removed, and modified features. If the old SRS is empty, state that this is the initial version of the project.
If there are no functional changes between the two versions, respond with ONLY the text "No changes detected.".
""")
                .model("gemini-2.0-flash")
                .outputKey(KEY_CHANGE_ANALYSIS)
                .build();

        String combinedInput = "--- OLD SRS ---\n" + oldSrs + "\n\n--- NEW SRS ---\n" + newSrs;

        // Use the simpler, synchronous-style run method that handles session creation internally.
        // This is more robust for single-shot agent invocations and avoids potential session state issues.
        final InMemoryRunner runner = new InMemoryRunner(changeAgent);
        final Content userMsg = Content.fromParts(Part.fromText(combinedInput));

        Event finalEvent = retryWithBackoff(() -> {
            Session session = runner.sessionService().createSession(runner.appName(), "user-change-analyzer").blockingGet();
            return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
        });
        return finalEvent != null ? finalEvent.stringifyContent() : "";
    }

    public static void writeClassesToFileSystem(String combinedOutput, String baseDir) {
        Pattern pattern = Pattern.compile(FILE_PATH_MARKER_PREFIX + "([^\\n]+)\\n(.*?)(?=\\n" + FILE_PATH_MARKER_PREFIX + "|\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(combinedOutput);

        while (matcher.find()) {
            String relativePath = matcher.group(1).trim();
            String rawContent = matcher.group(2).trim();

            // The LLM sometimes wraps the code in markdown blocks. This removes them.
            String content = rawContent;
            if (content.startsWith("```java")) {
                content = content.substring("```java".length());
            } else if (content.startsWith("```")) {
                content = content.substring("```".length());
            }

            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - "```".length());
            }
            content = content.trim(); // Clean up any remaining whitespace

            File file = new File(baseDir, relativePath);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                logger.info("✅ Created: {}", file.getPath());
            } catch (IOException e) {
                logger.error("❌ Failed to write: {} - {}", file.getPath(), e.getMessage());
            }
        }
    }

    /**
     * Helper method to run external commands, checking for errors and handling process streams.
     * This centralizes process execution for maintainability and robustness.
     *
     * @param workingDir The directory to run the command in.
     * @param command The command and its arguments.
     * @throws IOException If the command fails with a non-zero exit code.
     * @throws InterruptedException If the thread is interrupted while waiting for the process.
     */
    private static void runCommand(File workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command).directory(workingDir).inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with exit code " + exitCode + ": " + String.join(" ", command));
        }
    }

    private static String runCommandWithOutput(File workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command).directory(workingDir);
        Process process = pb.start();

        // Capture stdout and stderr to prevent blocking and for better error reporting
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Print the error stream from the process for better debugging
            logger.error("Command error output:\n{}", error);
            throw new IOException("Command failed with exit code " + exitCode + ": " + String.join(" ", command));
        }
        // Return standard output on success
        return output;
    }



    private static void openInBrowser(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                logger.error("❌ Failed to open browser: {}", e.getMessage());
            }
        }
    }

    private static String parseSrsForValue(String srsContent, String key) {
        Pattern pattern = Pattern.compile("^" + key + ":\\s*(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(srsContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private static void ensureRepositoryIsReady(String outputDir, String repoUrl, String baseBranch) throws IOException, InterruptedException {
        File dir = new File(outputDir);
        if (dir.exists()) {
            logger.info("Repository directory exists. Ensuring it's on the correct base branch and up-to-date.");
            runCommand(dir, "git", "checkout", baseBranch);
            runCommand(dir, "git", "pull", "origin", baseBranch);
        } else {
            logger.info("Cloning repository from {}", repoUrl);
            // More efficient clone: only get the single-branch history needed for analysis.
            runCommand(new File("."), "git", "clone", "--branch", baseBranch, "--single-branch", repoUrl, outputDir);
        }
    }

    private static String createFeatureBranchAndClean(String outputDir) throws IOException, InterruptedException {
        File dir = new File(outputDir);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(dtf);
        String featureBranch = "feature_" + timestamp;

        logger.info("Creating and checking out new feature branch: {}", featureBranch);
        runCommand(dir, "git", "checkout", "-b", featureBranch);

        logger.info("Cleaning workspace on new feature branch...");
        // List of files/directories to remove before generating new code.
        // This ensures no stale files are left from previous runs.
        String[] itemsToClean = {"src", "pom.xml", ".github"};
        for (String itemName : itemsToClean) {
            Path itemPath = Paths.get(outputDir, itemName);
            if (Files.exists(itemPath)) {
                try {
                    if (Files.isDirectory(itemPath)) {
                        // Recursively delete directory
                        Files.walk(itemPath)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    } else {
                        Files.delete(itemPath);
                    }
                    logger.info("  - Removed: {}", itemName);
                } catch (IOException e) {
                    logger.warn("⚠️  Could not remove '{}'. Please check file permissions. Error: {}", itemPath, e.getMessage());
                }
            }
        }
        return featureBranch;
    }

    private static void commitAndPush(String baseDir, String commitMessage, String branch) {
        try {
            File workingDir = new File(baseDir);
            logger.info("Adding files to Git...");
            runCommand(workingDir, "git", "add", ".");

            logger.info("Committing changes...");
            runCommand(workingDir, "git", "commit", "-m", commitMessage);

            logger.info("Pushing changes to origin/{}", branch);
            runCommand(workingDir, "git", "push", "origin", branch);
            logger.info("🚀 Project pushed to GitHub successfully.");

        } catch (Exception e) {
            logger.error("❌ Git commit or push failed: {}", e.getMessage());
            logger.error("  - Please check repository permissions and ensure the branch exists.");
        }
    }

    private static String createPullRequest(String baseDir, String baseBranch, String featureBranch, String title) {
        logger.info("🤖 Attempting to create a Pull Request...");
        try {
            File workingDir = new File(baseDir);
            String body = "Automated PR created by AI agent. Please review the changes.";
            String prUrl = runCommandWithOutput(workingDir, "gh", "pr", "create", "--base", baseBranch, "--head", featureBranch, "--title", title, "--body", body);
            logger.info("✅ Successfully created Pull Request: {}", prUrl.trim());
            return prUrl.trim();
        } catch (IOException e) {
            if (e.getMessage().toLowerCase().contains("command not found") || e.getMessage().toLowerCase().contains("cannot run program")) {
                logger.error("❌ Critical Error: The 'gh' (GitHub CLI) command is not installed or not in the system's PATH.");
                logger.error("  - Please install it from https://cli.github.com/ to enable automatic Pull Request creation.");
            } else {
                logger.error("❌ Failed to create Pull Request: {}", e.getMessage());
                logger.error("  - Ensure you are authenticated with 'gh auth login'.");
                logger.error("  - Ensure the repository remote is configured correctly and you have permissions.");
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("❌ PR creation was interrupted.");
            return null;
        }
    }

    public static void zipProject(String baseDir, String zipFileName) {
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path basePath = Paths.get(baseDir);
            Files.walk(basePath).filter(Files::isRegularFile).forEach(path -> {
                try {
                    // Don't include the .git directory in the zip
                    if (path.toString().contains(File.separator + ".git" + File.separator)) {
                        return;
                    }
                    ZipEntry zipEntry = new ZipEntry(basePath.relativize(path).toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    logger.error("❌ Error zipping file: {} - {}", path, e.getMessage());
                }
            });
            logger.info("📦 Project zipped to {}", zipFileName);
        } catch (IOException e) {
            logger.error("❌ Failed to zip project: {}", e.getMessage());
        }
    }

    public static void addGithubActionsCiConfig(String baseDir) {
        String ciYml = """
name: Java CI with Maven, Test, and Docker

on:
  push:
    branches: [ "main", "master", "feature/**" ]
  pull_request:
    branches: [ "main", "master", "feature/**" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Build and test with Maven
      run: mvn clean verify

    - name: Upload Test Results
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: junit-results
        path: target/surefire-reports

    - name: Upload Code Coverage Report
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: jacoco-report
        path: target/site/jacoco

    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3

    - name: Build Docker image
      run: docker build -t springboot-app:latest .
""";

        Path workflowDir = Paths.get(baseDir, ".github", "workflows");
        Path ciFile = workflowDir.resolve("ci.yml");

        try {
            Files.createDirectories(workflowDir);
            Files.writeString(ciFile, ciYml);
            logger.info("⚙️  GitHub Actions CI config added at: {}", ciFile);
        } catch (IOException e) {
            logger.error("❌ Failed to write CI config: {}", e.getMessage());
        }
    }

    public static void addPomXml(String baseDir, List<String> dependencies) {
        StringBuilder dependenciesXml = new StringBuilder();
        for (String dep : dependencies) {
            String[] parts = dep.split(":");
            if (parts.length >= 2) {
                String groupId = parts[0].trim();
                String artifactId = parts[1].trim();
                String scope = (parts.length == 3) ? parts[2].trim() : null;

                dependenciesXml.append("        <dependency>\n");
                dependenciesXml.append(String.format("            <groupId>%s</groupId>\n", groupId));
                dependenciesXml.append(String.format("            <artifactId>%s</artifactId>\n", artifactId));

                if ("optional".equalsIgnoreCase(scope)) {
                    dependenciesXml.append("            <optional>true</optional>\n");
                } else if (scope != null && !scope.equalsIgnoreCase("compile")) {
                    dependenciesXml.append(String.format("            <scope>%s</scope>\n", scope));
                }

                dependenciesXml.append("        </dependency>\n");
            }
        }

        String pom = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>3.2.5</version>
                    <relativePath/> <!-- lookup parent from repository -->
                </parent>
                <groupId>com.generated</groupId>
                <artifactId>microservice</artifactId>
                <version>1.0.0</version>
                <packaging>jar</packaging>
                <name>Generated Microservice</name>
                <properties>
                    <java.version>17</java.version>
                </properties>
                <dependencies>
%s
                    <!-- Test dependency is always included for the TestGenAgent -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                    </dependency>
                    <!-- Logging dependencies for SLF4J with Logback -->
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>ch.qos.logback</groupId>
                        <artifactId>logback-classic</artifactId>
                    </dependency>
                </dependencies>
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                            <configuration>
                                <excludes>
                                    <exclude>
                                        <groupId>org.projectlombok</groupId>
                                        <artifactId>lombok</artifactId>
                                    </exclude>
                                </excludes>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """, dependenciesXml.toString());
        try {
            Files.writeString(Paths.get(baseDir, "pom.xml"), pom);
            logger.info("✅ Created: pom.xml");
        } catch (IOException e) {
            logger.error("❌ Failed to write pom.xml: {}", e.getMessage());
        }
    }

    /**
     * Intelligently updates the README.md file.
     * This method is not used for comparing changes, but for injecting the AI-generated
     * project summary into a designated, marked-off section. This preserves any
     * human-made edits outside of the AI-managed block.
     *
     * @param baseDir The project's root directory.
     * @param requirements The high-level requirements summary from the AI.
     * @param dependencies The list of dependencies identified by the AI.
     */
    public static void updateReadme(String baseDir, String requirements, List<String> dependencies) {
        StringBuilder summarySection = new StringBuilder();
        summarySection.append("## 📝 Project Summary\n\n");
        summarySection.append("This microservice was automatically generated based on the following high-level requirements:\n\n");

        // Format requirements as an attractive blockquote
        String quotedRequirements = "> " + requirements.replace("\n", "\n> ");
        summarySection.append(quotedRequirements).append("\n\n");

        if (!dependencies.isEmpty()) {
            summarySection.append("### 🛠️ Core Dependencies\n\n");
            summarySection.append("The following core dependencies were automatically included to support these requirements:\n\n");

            // Format dependencies as a markdown table for better readability
            summarySection.append("| Group ID | Artifact ID | Scope |\n");
            summarySection.append("|---|---|---|\n");
            for (String dep : dependencies) {
                String[] parts = dep.split(":");
                String groupId = parts.length > 0 ? parts[0].trim() : "";
                String artifactId = parts.length > 1 ? parts[1].trim() : "";
                String scope = parts.length > 2 ? parts[2].trim() : "compile"; // Default to compile
                summarySection.append(String.format("| `%s` | `%s` | `%s` |\n", groupId, artifactId, scope));
            }
            summarySection.append("\n");
        }

        String newSummaryBlock = summarySection.toString();
        final String startTag = "<!-- AI-SUMMARY-START -->";
        final String endTag = "<!-- AI-SUMMARY-END -->";

        // This is the default full content if README.md doesn't exist
        String fullFileContent = String.format("""
            # Generated Spring Boot Microservice

            This project was generated using a multi-agent AI system from an SRS document.

%s
            ## ⚙️ Configuration

            Before running the application, you must set the following environment variables for the database connection:

            ```bash
            export DB_USERNAME=your_database_username
            export DB_PASSWORD=your_database_password
            ```

            ## 📦 Build

            ```bash
            mvn clean install
            ```

            ## 🚀 Run

            ```bash
            mvn spring-boot:run
            ```

            ## 🤖 CI/CD

            This project uses GitHub Actions for Maven build automation.

            ## 🧠 High-Level Architecture

            ```mermaid
            graph TD
                A[SRS Document] --> B[AI Agent - Gemini Code Agent]
                B --> C[Spring Boot Code Generator]
                C --> D[Java Source Files + pom.xml]
                D --> E[Git Repo + CI/CD]
                E --> F[Deployable Spring Boot Artifact]
            ```
            """, startTag + "\n\n" + newSummaryBlock + "\n" + endTag);

        Path readmePath = Paths.get(baseDir, "README.md");
        try {
            if (Files.exists(readmePath)) {
                String existingContent = Files.readString(readmePath);
                int startIndex = existingContent.indexOf(startTag);
                int endIndex = existingContent.indexOf(endTag);

                if (startIndex != -1 && endIndex != -1) {
                    // Found tags, replace content between them
                    StringBuilder builder = new StringBuilder();
                    builder.append(existingContent, 0, startIndex + startTag.length());
                    builder.append("\n\n").append(newSummaryBlock).append("\n");
                    builder.append(existingContent, endIndex, existingContent.length());
                    fullFileContent = builder.toString();
                    logger.info("✅ Updated existing README.md with new summary.");
                } else {
                    // No tags found, prepend the new summary block to the existing file
                    fullFileContent = startTag + "\n\n" + newSummaryBlock + "\n" + endTag + "\n\n" + existingContent;
                    logger.info("✅ Prepended AI summary to existing README.md.");
                }
            } else {
                logger.info("✅ Created new README.md.");
            }

            Files.writeString(readmePath, fullFileContent);

        } catch (IOException e) {
            logger.error("❌ Failed to write README.md: {}", e.getMessage());
        }
    }

    public static void addApplicationYml(String baseDir) {
        String content = """
            server:
              port: 8080
            spring:
              application:
                name: generated-microservice
              datasource:
                url: jdbc:postgresql://localhost:5432/mydatabase
                username: ${DB_USERNAME:user}
                password: ${DB_PASSWORD:password}
                driver-class-name: org.postgresql.Driver
              jpa:
                hibernate:
                  ddl-auto: update
                show-sql: true
            """;
        Path resourcesDir = Paths.get(baseDir, "src", "main", "resources");
        try {
            Files.createDirectories(resourcesDir);
            Files.writeString(resourcesDir.resolve("application.yml"), content);
            logger.info("✅ Created: application.yml");
        } catch (IOException e) {
            logger.error("❌ Failed to write application.yml: {}", e.getMessage());
        }
    }

    private static SrsData readSrsData() throws IOException {
        String srsPath;
        try (Scanner s = new Scanner(System.in, StandardCharsets.UTF_8)) {
            logger.info("\nEnter the full path to your SRS document (e.g., /home/user/project/specs/srs.txt):");
            srsPath = s.nextLine().trim();
        }

        logger.info("Reading SRS document from: {}", srsPath);
        String userInput = Files.readString(Paths.get(srsPath), StandardCharsets.UTF_8);

        String repoUrl = parseSrsForValue(userInput, SRS_KEY_GITHUB_URL);
        String baseBranch = parseSrsForValue(userInput, SRS_KEY_CHECKOUT_BRANCH);
        String repoName = parseSrsForValue(userInput, SRS_KEY_REPO_NAME);

        if (repoUrl == null || baseBranch == null || repoName == null) {
            throw new IOException("SRS document must contain 'GitHub-URL', 'checkout_branch', and 'Repository-Name' keys.");
        }

        logger.info("  - Found Repo URL: {}", repoUrl);
        logger.info("  - Found Base Branch: {}", baseBranch);
        logger.info("  - Found Repo Name: {}", repoName);

        GitConfig gitConfig = new GitConfig(repoUrl, baseBranch, repoName);
        return new SrsData(gitConfig, userInput);
    }

    private static WorkflowResult runMainWorkflow(String userInput) {
        final SequentialAgent workflow = buildWorkflow();
        final WorkflowResult workflowResult = new WorkflowResult();

        try {
            retryWithBackoff(() -> {
                // Reset state variables inside the retry loop to ensure a clean slate for each attempt
                workflowResult.commitMessage = "feat: Initial project scaffold by AI agent";
                workflowResult.requirementsSummary = "";
                workflowResult.codeAndTestOutput = "";
                workflowResult.dependencyList.clear();

                logger.info("\n--- Running Main AI Workflow ---");
                InMemoryRunner runner = new InMemoryRunner(workflow);
                Session session = runner.sessionService().createSession(runner.appName(), "user").blockingGet();
                Content userMsg = Content.fromParts(Part.fromText(userInput));

                runner.runAsync(session.userId(), session.id(), userMsg).blockingForEach(ev -> {
                    String response = ev.stringifyContent();
                    if (!response.isBlank()) {
                        logger.info("[{}]\n{}\n", ev.author(), response);

                        if (DEPENDENCY_AGENT_NAME.equals(ev.author())) {
                            String[] parts = response.trim().split("\\s*" + DEPS_SEPARATOR + "\\s*");
                            if (parts.length > 0) {
                                workflowResult.dependencyList.addAll(java.util.Arrays.asList(parts[0].trim().split("\\s*\\r?\\n\\s*")));
                            }
                        } else if (REQUIREMENTS_AGENT_NAME.equals(ev.author())) {
                            String reqResponse = response.trim();
                            String[] lines = reqResponse.split("\\r?\\n", 2);
                            if (lines.length > 0 && lines[0].startsWith(COMMIT_SUMMARY_PREFIX)) {
                                workflowResult.commitMessage = lines[0].substring(COMMIT_SUMMARY_PREFIX.length()).trim();
                                if (lines.length > 1) {
                                    workflowResult.requirementsSummary = lines[1].trim();
                                }
                            } else {
                                workflowResult.requirementsSummary = reqResponse;
                            }
                        }

                        if (CODEGEN_AGENT_NAME.equals(ev.author()) || TESTGEN_AGENT_NAME.equals(ev.author())) {
                            workflowResult.codeAndTestOutput += response + "\n\n";
                        }
                    }
                });
                return null;
            });
        } catch (Exception e) {
            logger.error("❌ The main AI workflow failed after multiple retries. Aborting.", e);
            return null;
        }
        return workflowResult;
    }

    private static void generateProjectFiles(String repoName, WorkflowResult result, String srsContent) {
        writeClassesToFileSystem(result.codeAndTestOutput, repoName);

        if (result.dependencyList.isEmpty()) {
            logger.warn("⚠️ DependencyAgent did not return any dependencies. Falling back to default pom.xml.");
            List<String> defaultDeps = List.of(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-data-jpa",
                "org.postgresql:postgresql:runtime",
                "org.projectlombok:lombok:optional"
            );
            addPomXml(repoName, defaultDeps);
        } else {
            addPomXml(repoName, result.dependencyList);
        }

        updateReadme(repoName, result.requirementsSummary, result.dependencyList);
        addApplicationYml(repoName);
        addGithubActionsCiConfig(repoName);

        try {
            Path aiStateDir = Paths.get(repoName, AI_STATE_DIR);
            Files.createDirectories(aiStateDir);
            Files.writeString(aiStateDir.resolve(SRS_FILE_NAME), srsContent);
            logger.info("✅ Saved current SRS to state file.");
        } catch (IOException e) {
            logger.error("❌ Failed to write analysis or state files: {}", e.getMessage());
        }
    }

    private static void finalizeAndSubmit(GitConfig gitConfig, String featureBranch, String commitMessage) {
        zipProject(gitConfig.repoName, gitConfig.repoName + ".zip");
        commitAndPush(gitConfig.repoName, commitMessage, featureBranch);
        String prUrl = createPullRequest(gitConfig.repoName, gitConfig.baseBranch, featureBranch, commitMessage);
        if (prUrl != null) {
            openInBrowser(prUrl);
        }
    }

    public static void main(String[] args) {
        SrsData srsData;
        try {
            srsData = readSrsData();
        } catch (IOException e) {
            logger.error("❌ Failed to read SRS configuration: {}", e.getMessage());
            return;
        }

        GitConfig gitConfig = srsData.gitConfig;
        String userInput = srsData.srsContent;

        try {
            ensureRepositoryIsReady(gitConfig.repoName, gitConfig.repoUrl, gitConfig.baseBranch);
        } catch (Exception e) {
            logger.error("❌ Failed to prepare the repository for analysis. Aborting. Error: {}", e.getMessage());
            return;
        }

        // Perform change analysis by comparing the new SRS with the last known version.
        String changeAnalysis = performChangeAnalysis(gitConfig.repoName, userInput);

        // If the analysis agent found no changes, skip the rest of the workflow.
        if (changeAnalysis.trim().equals(NO_CHANGES_DETECTED)) {
            logger.info("\n✅ No functional changes detected in SRS. The local repository has been updated to the latest from the base branch, but no feature branch will be created.");
            // The changelog is not written because no feature branch is created.
            return;
        }

        // Since changes were detected, proceed with creating a feature branch and cleaning it.
        String featureBranch;
        try {
            featureBranch = createFeatureBranchAndClean(gitConfig.repoName);
        } catch (Exception e) {
            logger.error("❌ Failed to create feature branch. Aborting. Error: {}", e.getMessage());
            return;
        }

        // Always write the changelog file so the result of the analysis is recorded.
        try {
            Path changelogPath = Paths.get(gitConfig.repoName, CHANGELOG_FILE_NAME);
            Files.writeString(changelogPath, changeAnalysis);
            logger.info("✅ Wrote change analysis to feature branch: {}", changelogPath);
        } catch (IOException e) {
            logger.error("❌ Failed to write analysis file: {}", e.getMessage());
        }

        final WorkflowResult workflowResult = runMainWorkflow(userInput);

        if (workflowResult == null) {
            logger.error("❌ Workflow execution failed. Could not generate project files. Aborting.");
            return;
        }

        generateProjectFiles(gitConfig.repoName, workflowResult, userInput);

        finalizeAndSubmit(gitConfig, featureBranch, workflowResult.commitMessage);
    }

    private static String performChangeAnalysis(String repoDir, String newSrs) {
        try {
            Path oldSrsPath = Paths.get(repoDir, AI_STATE_DIR, SRS_FILE_NAME);
            String oldSrsContent = "";
            if (Files.exists(oldSrsPath)) {
                logger.info("Found previous SRS state file for comparison.");
                oldSrsContent = Files.readString(oldSrsPath);
            } else {
                logger.info("No previous SRS state file found. This will be an initial analysis.");
            }
            return runChangeAnalysisAgent(oldSrsContent, newSrs);
        } catch (RuntimeException e) {
            logger.warn("⚠️ Could not perform change analysis after multiple retries: {}", e.getMessage());
            return "Change analysis failed to run: " + e.getMessage();
        } catch (Exception e) {
            logger.warn("⚠️ Could not perform change analysis: {}", e.getMessage());
            return "Change analysis failed to run: " + e.getMessage();
        }
    }

    private static boolean isCausedByServerException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof com.google.genai.errors.ServerException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static <T> T retryWithBackoff(java.util.function.Supplier<T> action) {
        int maxRetries = 3;
        long delayMillis = 2000L; // Start with 2 seconds
        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                // Recursively check the cause chain for a retriable ServerException.
                if (isCausedByServerException(e)) {
                    if (i < maxRetries - 1) {
                        logger.warn("⚠️  Model request failed (attempt {}/{}) with a server error. Retrying in {} ms...", i + 1, maxRetries, delayMillis);
                        try {
                            Thread.sleep(delayMillis);
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Workflow interrupted during backoff wait.", interruptedException);
                        }
                        delayMillis *= 2; // Exponential backoff
                    }
                } else {
                    // Not a retriable server error, fail fast.
                    throw new RuntimeException("An unrecoverable error occurred", e); // Not a retriable server error, fail fast.
                }
            }
        }
        // If we've exited the loop, it means all retries failed.
        throw new RuntimeException("Model request failed after " + maxRetries + " attempts.", lastException);
    }
}
