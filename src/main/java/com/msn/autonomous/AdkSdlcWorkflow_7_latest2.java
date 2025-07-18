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

import java.awt.*;
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

public class AdkSdlcWorkflow_7_latest2 {

    public static SequentialAgent buildWorkflow() {
        LlmAgent req = LlmAgent.builder()
                .name("RequirementsAgent")
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
                .outputKey("requirements")
                .build();

        LlmAgent deps = LlmAgent.builder()
                .name("DependencyAgent")
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
                .outputKey("dependencies")
                .build();

        LlmAgent code = LlmAgent.builder()
                .name("CodeGenAgent")
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
                .outputKey("code")
                .build();

        LlmAgent test = LlmAgent.builder()
                .name("TestGenAgent")
                .description("Generates JUnit 5 test cases for a Spring Boot microservice.")
                .instruction("""
Write appropriate JUnit 5 test cases for the Spring Boot controller and service layer based on this code:
{code}

Wrap each test class in Java syntax and include a comment at the top indicating the file path, for example:
// File: src/test/java/com/example/controller/UserControllerTest.java
""")
                .model("gemini-2.0-flash")
                .outputKey("test")
                .build();

        return SequentialAgent.builder()
                .name("FullSpringBootMicroserviceWorkflow")
                .subAgents(req, deps, code, test)
                .build();
    }

    public static void writeClassesToFileSystem(String combinedOutput, String baseDir) {
        Pattern pattern = Pattern.compile("// File: ([^\\n]+)\\n(.*?)(?=\\n// File: |\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(combinedOutput);

        while (matcher.find()) {
            String relativePath = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            File file = new File(baseDir, relativePath);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                System.out.println("✅ Created: " + file.getPath());
            } catch (IOException e) {
                System.err.println("❌ Failed to write: " + file.getPath() + " - " + e.getMessage());
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
            System.err.println("Command error output:\n" + error);
            throw new IOException("Command failed with exit code " + exitCode + ": " + String.join(" ", command));
        }
        // Return standard output on success
        return output;
    }

    private static String runChangeAnalysisAgent(String oldSrs, String newSrs) {
        System.out.println("🤖 Running Change Analysis Agent...");
        LlmAgent changeAgent = LlmAgent.builder()
            .name("ChangeAnalysisAgent")
            .description("Compares old and new SRS to generate a changelog.")
            .instruction("""
You will be given an old and a new version of a Software Requirements Specification (SRS), separated by markers.
Analyze the differences and generate a concise, human-readable changelog in Markdown format.
Focus on added, removed, and modified features. If the old SRS is empty, state that this is the initial version of the project.
If there are no functional changes between the two versions, respond with ONLY the text "No changes detected.".
""")
            .model("gemini-2.0-flash")
            .outputKey("change_analysis")
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

    private static void openInBrowser(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println("❌ Failed to open browser: " + e.getMessage());
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
            System.out.println("Repository directory exists. Ensuring it's on the correct base branch and up-to-date.");
            runCommand(dir, "git", "checkout", baseBranch);
            runCommand(dir, "git", "pull", "origin", baseBranch);
        } else {
            System.out.println("Cloning repository from " + repoUrl);
            // More efficient clone: only get the single-branch history needed for analysis.
            runCommand(new File("."), "git", "clone", "--branch", baseBranch, "--single-branch", repoUrl, outputDir);
        }
    }

    private static String createFeatureBranchAndClean(String outputDir) throws IOException, InterruptedException {
        File dir = new File(outputDir);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(dtf);
        String featureBranch = "feature_" + timestamp;

        System.out.println("Creating and checking out new feature branch: " + featureBranch);
        runCommand(dir, "git", "checkout", "-b", featureBranch);

        System.out.println("Cleaning workspace on new feature branch...");
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
                    System.out.println("  - Removed: " + itemName);
                } catch (IOException e) {
                    System.err.printf("⚠️  Could not remove '%s'. Please check file permissions. Error: %s%n", itemPath, e.getMessage());
                }
            }
        }
        return featureBranch;
    }

    private static void commitAndPush(String baseDir, String commitMessage, String branch) {
        try {
            File workingDir = new File(baseDir);
            System.out.println("Adding files to Git...");
            runCommand(workingDir, "git", "add", ".");

            System.out.println("Committing changes...");
            runCommand(workingDir, "git", "commit", "-m", commitMessage);

            System.out.println("Pushing changes to origin/" + branch);
            runCommand(workingDir, "git", "push", "origin", branch);
            System.out.println("🚀 Project pushed to GitHub successfully.");

        } catch (Exception e) {
            System.err.println("❌ Git commit or push failed: " + e.getMessage());
            System.err.println("  - Please check repository permissions and ensure the branch exists.");
        }
    }

    private static String createPullRequest(String baseDir, String baseBranch, String featureBranch, String title) {
        System.out.println("🤖 Attempting to create a Pull Request...");
        try {
            File workingDir = new File(baseDir);
            String body = "Automated PR created by AI agent. Please review the changes.";
            String prUrl = runCommandWithOutput(workingDir, "gh", "pr", "create", "--base", baseBranch, "--head", featureBranch, "--title", title, "--body", body);
            System.out.println("✅ Successfully created Pull Request: " + prUrl.trim());
            return prUrl.trim();
        } catch (IOException e) {
            if (e.getMessage().toLowerCase().contains("command not found") || e.getMessage().toLowerCase().contains("cannot run program")) {
                System.err.println("❌ Critical Error: The 'gh' (GitHub CLI) command is not installed or not in the system's PATH.");
                System.err.println("  - Please install it from https://cli.github.com/ to enable automatic Pull Request creation.");
            } else {
                System.err.println("❌ Failed to create Pull Request: " + e.getMessage());
                System.err.println("  - Ensure you are authenticated with 'gh auth login'.");
                System.err.println("  - Ensure the repository remote is configured correctly and you have permissions.");
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ PR creation was interrupted.");
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
                    System.err.println("❌ Error zipping file: " + path + " - " + e.getMessage());
                }
            });
            System.out.println("📦 Project zipped to " + zipFileName);
        } catch (IOException e) {
            System.err.println("❌ Failed to zip project: " + e.getMessage());
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
            System.out.println("⚙️  GitHub Actions CI config added at: " + ciFile);
        } catch (IOException e) {
            System.err.println("❌ Failed to write CI config: " + e.getMessage());
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
            System.out.println("✅ Created: pom.xml");
        } catch (IOException e) {
            System.err.println("❌ Failed to write pom.xml: " + e.getMessage());
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
                    System.out.println("✅ Updated existing README.md with new summary.");
                } else {
                    // No tags found, prepend the new summary block to the existing file
                    fullFileContent = startTag + "\n\n" + newSummaryBlock + "\n" + endTag + "\n\n" + existingContent;
                    System.out.println("✅ Prepended AI summary to existing README.md.");
                }
            } else {
                System.out.println("✅ Created new README.md.");
            }

            Files.writeString(readmePath, fullFileContent);

        } catch (IOException e) {
            System.err.println("❌ Failed to write README.md: " + e.getMessage());
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
                username: user
                password: password
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
            System.out.println("✅ Created: application.yml");
        } catch (IOException e) {
            System.err.println("❌ Failed to write application.yml: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String srsPath;
        try (Scanner s = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("\nEnter the full path to your SRS document (e.g., /home/user/project/specs/srs.txt):");
            srsPath = s.nextLine().trim();
        }

        String userInput, repoUrl, baseBranch, repoName;
        try {
            System.out.println("Reading SRS document from: " + srsPath);
            userInput = Files.readString(Paths.get(srsPath), StandardCharsets.UTF_8);

            // Parse Git information directly from the SRS document
            repoUrl = parseSrsForValue(userInput, "GitHub-URL");
            baseBranch = parseSrsForValue(userInput, "checkout_branch");
            repoName = parseSrsForValue(userInput, "Repository-Name");

            if (repoUrl == null || baseBranch == null || repoName == null) {
                System.err.println("❌ SRS document must contain 'GitHub-URL', 'checkout_branch', and 'Repository-Name' keys.");
                return;
            }
            System.out.println("  - Found Repo URL: " + repoUrl);
            System.out.println("  - Found Base Branch: " + baseBranch);
            System.out.println("  - Found Repo Name: " + repoName);

        } catch (IOException e) {
            System.err.println("❌ Failed to read SRS file: " + e.getMessage());
            return;
        }

        try {
            ensureRepositoryIsReady(repoName, repoUrl, baseBranch);
        } catch (Exception e) {
            System.err.println("❌ Failed to prepare the repository for analysis. Aborting. Error: " + e.getMessage());
            return;
        }

        // Perform change analysis by comparing the new SRS with the last known version.
        String changeAnalysis = performChangeAnalysis(repoName, userInput);

        // If the analysis agent found no changes, skip the rest of the workflow.
        if (changeAnalysis.trim().equals("No changes detected.")) {
            System.out.println("\n✅ No functional changes detected in SRS. The local repository has been updated to the latest from the base branch, but no feature branch will be created.");
            // The changelog is not written because no feature branch is created.
            return;
        }

        // Since changes were detected, proceed with creating a feature branch and cleaning it.
        String featureBranch;
        try {
            featureBranch = createFeatureBranchAndClean(repoName);
        } catch (Exception e) {
            System.err.println("❌ Failed to create feature branch. Aborting. Error: " + e.getMessage());
            return;
        }

        // Always write the changelog file so the result of the analysis is recorded.
        try {
            Path changelogPath = Paths.get(repoName, "AI_CHANGELOG.md");
            Files.writeString(changelogPath, changeAnalysis);
            System.out.println("✅ Wrote change analysis to feature branch: " + changelogPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write analysis file: " + e.getMessage());
        }

        final SequentialAgent workflow = buildWorkflow();
        final StringBuilder codeAndTestOutput = new StringBuilder();
        final List<String> dependencyList = new ArrayList<>();
        final StringBuilder requirementsSummary = new StringBuilder();
        final StringBuilder commitMessage = new StringBuilder("feat: Initial project scaffold by AI agent");

        try {
            retryWithBackoff(() -> {
                // Reset state variables inside the retry loop to ensure a clean slate for each attempt
                codeAndTestOutput.setLength(0);
                dependencyList.clear();
                requirementsSummary.setLength(0);
                commitMessage.setLength(0);
                commitMessage.append("feat: Initial project scaffold by AI agent");

                System.out.println("\n--- Running Main AI Workflow ---");
                InMemoryRunner runner = new InMemoryRunner(workflow);
                Session session = runner.sessionService().createSession(runner.appName(), "user").blockingGet();
                Content userMsg = Content.fromParts(Part.fromText(userInput));

                runner.runAsync(session.userId(), session.id(), userMsg).blockingForEach(ev -> {
                    String response = ev.stringifyContent();
                    if (!response.isBlank()) {
                        System.out.println("[" + ev.author() + "]\n" + response + "\n");

                        if ("DependencyAgent".equals(ev.author())) {
                            // The DependencyAgent now outputs deps and requirements, separated by a marker.
                            // We only need the dependency list part.
                            String[] parts = response.trim().split("\\s*---END-DEPS---\\s*");
                            if (parts.length > 0) {
                                dependencyList.addAll(java.util.Arrays.asList(parts[0].trim().split("\\s*\\r?\\n\\s*")));
                            }
                        } else if ("RequirementsAgent".equals(ev.author())) {
                            // The RequirementsAgent now produces the commit summary AND the requirements.
                            // We need to parse them apart.
                            String reqResponse = response.trim();
                            String[] lines = reqResponse.split("\\r?\\n", 2);
                            if (lines.length > 0 && lines[0].startsWith("Commit-Summary: ")) {
                                commitMessage.setLength(0); // Clear default
                                commitMessage.append(lines[0].substring("Commit-Summary: ".length()).trim());
                                if (lines.length > 1) {
                                    requirementsSummary.append(lines[1].trim());
                                }
                            } else {
                                // Fallback if the agent didn't follow the new prompt format
                                requirementsSummary.append(reqResponse);
                            }
                        }

                        // Capture output only from code generation agents for file writing
                        if ("CodeGenAgent".equals(ev.author()) || "TestGenAgent".equals(ev.author())) {
                            codeAndTestOutput.append(response).append("\n\n");
                        }
                    }
                });
                return null; // Return null for Void action
            });
        } catch (Exception e) {
            System.err.println("❌ The main AI workflow failed after multiple retries. Aborting.");
            e.printStackTrace();
            return; // Exit
        }

        writeClassesToFileSystem(codeAndTestOutput.toString(), repoName);
        // Use the dynamic output from the DependencyAgent, with a sensible default if it's empty.
        if (dependencyList.isEmpty()) {
            System.out.println("⚠️ DependencyAgent did not return any dependencies. Falling back to default pom.xml.");
            List<String> defaultDeps = List.of(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-data-jpa",
                "org.postgresql:postgresql:runtime",
                "org.projectlombok:lombok:optional"
            );
            addPomXml(repoName, defaultDeps);
        } else {
            addPomXml(repoName, dependencyList);
        }
        updateReadme(repoName, requirementsSummary.toString(), dependencyList);
        addApplicationYml(repoName);
        addGithubActionsCiConfig(repoName);

        // Write the analysis and new state files
        try {
            Path aiStateDir = Paths.get(repoName, ".ai-state");
            Files.createDirectories(aiStateDir);
            Files.writeString(aiStateDir.resolve("srs.txt"), userInput);
            System.out.println("✅ Saved current SRS to state file.");
        } catch (IOException e) {
            System.err.println("❌ Failed to write analysis or state files: " + e.getMessage());
        }

        zipProject(repoName, repoName + ".zip");

        commitAndPush(repoName, commitMessage.toString(), featureBranch);

        String prUrl = createPullRequest(repoName, baseBranch, featureBranch, commitMessage.toString());
        if (prUrl != null) {
            openInBrowser(prUrl);
        }
    }

    private static String performChangeAnalysis(String repoDir, String newSrs) {
        try {
            Path oldSrsPath = Paths.get(repoDir, ".ai-state", "srs.txt");
            String oldSrsContent = "";
            if (Files.exists(oldSrsPath)) {
                System.out.println("Found previous SRS state file for comparison.");
                oldSrsContent = Files.readString(oldSrsPath);
            } else {
                System.out.println("No previous SRS state file found. This will be an initial analysis.");
            }
            return runChangeAnalysisAgent(oldSrsContent, newSrs);
        } catch (RuntimeException e) {
            System.err.println("⚠️ Could not perform change analysis after multiple retries: " + e.getMessage());
            return "Change analysis failed to run: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("⚠️ Could not perform change analysis: " + e.getMessage());
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
                        System.out.printf("⚠️  Model request failed (attempt %d/%d) with a server error. Retrying in %d ms...%n", i + 1, maxRetries, delayMillis);
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
