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
import java.util.Map;
import java.util.HashMap;

public class AdkSdlcWorkflow_7 {

    private static final Logger logger = LoggerFactory.getLogger(AdkSdlcWorkflow_7.class);

    // --- Constants for Agent Configuration ---
    private static final String REQUIREMENTS_AGENT_NAME = "RequirementsAgent";
    private static final String DEPENDENCY_AGENT_NAME = "DependencyAgent";
    private static final String CODEGEN_AGENT_NAME = "CodeGenAgent";
    private static final String TESTGEN_AGENT_NAME = "TestGenAgent";
    private static final String CHANGE_ANALYSIS_AGENT_NAME = "ChangeAnalysisAgent";
    private static final String REVIEW_AGENT_NAME = "ReviewAgent";
    private static final String CORRECTOR_AGENT_NAME = "CorrectorAgent";

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
    private static final String SRS_KEY_JAVA_VERSION = "Java-Version";
    private static final String SRS_KEY_SPRING_BOOT_VERSION = "SpringBoot-Version";

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
        String repoPath; // Changed from repoName to reflect it's a path

        GitConfig(String repoUrl, String baseBranch, String repoPath) {
            this.repoUrl = repoUrl;
            this.baseBranch = baseBranch;
            this.repoPath = repoPath;
        }
    }

    /**
     * A simple data class to hold project version configuration.
     */
    private static class ProjectConfig {
        final String javaVersion;
        final String springBootVersion;

        ProjectConfig(String javaVersion, String springBootVersion) {
            this.javaVersion = (javaVersion != null && !javaVersion.isBlank()) ? javaVersion : "17";
            this.springBootVersion = (springBootVersion != null && !springBootVersion.isBlank()) ? springBootVersion : "3.2.5";
        }
    }

    /**
     * A simple data class to hold the SRS content and its parsed Git configuration.
     */
    private static class SrsData {
        final GitConfig gitConfig;
        final ProjectConfig projectConfig;
        final String srsContent;

        SrsData(GitConfig gitConfig, ProjectConfig projectConfig, String srsContent) {
            this.gitConfig = gitConfig;
            this.projectConfig = projectConfig;
            this.srsContent = srsContent;
        }
    }

    private static class CorrectorResult {
        final String failingAgentName;
        final String correctedPrompt;

        CorrectorResult(String failingAgentName, String correctedPrompt) {
            this.failingAgentName = failingAgentName;
            this.correctedPrompt = correctedPrompt;
        }
    }

    public static SequentialAgent buildWorkflow(ProjectConfig projectConfig, Map<String, String> agentPrompts) {
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
                .instruction(String.format("""
Based on the following requirements, identify the necessary Maven dependencies for a project using Java %s and Spring Boot %s.
This version context is CRITICAL for selecting compatible dependency versions.

Provide ONLY a list of `groupId:artifactId[:version][:scope]` tuples, one per line.

**IMPORTANT**: For any dependency NOT managed by the specified Spring Boot parent POM (like `springdoc-openapi` or other third-party libraries), you MUST provide an explicit, recent version number that is compatible with Spring Boot %s. For dependencies managed by Spring Boot, you MUST omit the version so the parent POM can manage it.

Use 'compile' for standard dependencies, 'runtime' for runtime-only, and 'optional' for tools like Lombok. If scope is 'compile', you can omit it.

After the dependency list, you MUST add a separator line containing exactly "---END-DEPS---".
After the separator, you MUST repeat the original requirements text provided below, exactly and without modification.

Example output format:
org.springframework.boot:spring-boot-starter-web
org.springframework.boot:spring-boot-starter-data-jpa
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0
---END-DEPS---
Feature: User Management API
...
Requirements:
{requirements}
""", projectConfig.javaVersion, projectConfig.springBootVersion, projectConfig.springBootVersion))
                .model("gemini-2.0-flash")
                .outputKey(KEY_DEPENDENCIES)
                .build();

        LlmAgent code = LlmAgent.builder()
                .name(CODEGEN_AGENT_NAME)
                .description("Generates a complete Spring Boot microservice skeleton based on structured requirements.")
                .instruction(agentPrompts.get(CODEGEN_AGENT_NAME))
                .model("gemini-2.0-flash")
                .outputKey(KEY_CODE)
                .build();

        LlmAgent test = LlmAgent.builder()
                .name(TESTGEN_AGENT_NAME)
                .description("Generates JUnit 5 test cases for a Spring Boot microservice.")
                .instruction(agentPrompts.get(TESTGEN_AGENT_NAME))
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
        // More flexible regex to tolerate optional newlines and whitespace between the file marker and the code block.
        Pattern pattern = Pattern.compile(FILE_PATH_MARKER_PREFIX + "([^\\n]+)\\s*\\n(.*?)(?=\\n" + FILE_PATH_MARKER_PREFIX + "|\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(combinedOutput);

        while (matcher.find()) {
            String relativePath = matcher.group(1).trim();
            String rawContent = matcher.group(2).trim();
            String content = "";

            // Regex to find content inside ```java ... ``` or ``` ... ``` blocks.
            Pattern codeBlockPattern = Pattern.compile("```(?:java)?\\s*\\n(.*?)\\n```", Pattern.DOTALL);
            Matcher codeMatcher = codeBlockPattern.matcher(rawContent.replace(DEPS_SEPARATOR, ""));

            if (codeMatcher.find()) {
                String extracted = codeMatcher.group(1);
                if (extracted != null && !extracted.trim().isEmpty()) {
                    content = extracted;
                }
            }

            // If content is still empty, it means either the regex didn't match,
            // or it matched an empty block. In either case, we fall back to
            // treating the raw content as the source, just needing cleanup.
            if (content.isEmpty()) {
                content = rawContent.replaceAll("(?m)^```(java)?|```$", "").replace(DEPS_SEPARATOR, "");
            }

            content = content.trim();

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
    private static String runCommand(File workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command).directory(workingDir);
        Process process = pb.start();

        // Capture stdout and stderr to prevent blocking and for better error reporting
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String combinedOutput = "--- STDOUT ---\n" + output + "\n\n--- STDERR ---\n" + error;
            // Print the error stream from the process for better debugging
            logger.error("Command error output:\n{}", combinedOutput);
            throw new IOException("Command failed with exit code " + exitCode + ": " + String.join(" ", command) + "\n\n" + combinedOutput);
        }
        // Return standard output on success, though for inherited IO this will be empty
        return output;
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

    public static void addPomXml(String baseDir, List<String> dependencies, ProjectConfig projectConfig) {
        StringBuilder dependenciesXml = new StringBuilder();
        List<String> managedDependencies = new ArrayList<>(dependencies);

        // --- Resilient Dependency Management ---
        // Ensure required starters are present without creating duplicates.
        addDependencyIfNotExists(managedDependencies, "org.springframework.boot:spring-boot-starter-validation");
        addDependencyIfNotExists(managedDependencies, "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0");


        for (String dep : managedDependencies) {
            String[] parts = dep.split(":");
            if (parts.length < 2) continue; // Skip invalid lines

                String groupId = parts[0].trim();
                String artifactId = parts[1].trim();
            String version = (parts.length > 2 && !parts[2].matches("compile|runtime|test|optional")) ? parts[2].trim() : null;
            String scope = (parts.length > 2 && version == null) ? parts[2].trim() :
                           (parts.length > 3) ? parts[3].trim() : null;

                dependenciesXml.append("        <dependency>\n");
                dependenciesXml.append(String.format("            <groupId>%s</groupId>\n", groupId));
                dependenciesXml.append(String.format("            <artifactId>%s</artifactId>\n", artifactId));

            if (version != null) {
                dependenciesXml.append(String.format("            <version>%s</version>\n", version));
            }

                if ("optional".equalsIgnoreCase(scope)) {
                    dependenciesXml.append("            <optional>true</optional>\n");
                } else if (scope != null && !scope.equalsIgnoreCase("compile")) {
                    dependenciesXml.append(String.format("            <scope>%s</scope>\n", scope));
                }
                dependenciesXml.append("        </dependency>\n");
        }

        String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "    xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <groupId>org.springframework.boot</groupId>\n"
            + "        <artifactId>spring-boot-starter-parent</artifactId>\n"
            + "        <version>" + projectConfig.springBootVersion + "</version>\n"
            + "        <relativePath/> <!-- lookup parent from repository -->\n"
            + "    </parent>\n"
            + "    <groupId>com.generated</groupId>\n"
            + "    <artifactId>microservice</artifactId>\n"
            + "    <version>1.0.0</version>\n"
            + "    <packaging>jar</packaging>\n"
            + "    <name>Generated Microservice</name>\n"
            + "    <properties>\n"
            + "        <java.version>" + projectConfig.javaVersion + "</java.version>\n"
            + "    </properties>\n"
            + "    <dependencies>\n"
            + dependenciesXml.toString()
            // Always add test dependency
            + "        <dependency>\n"
            + "            <groupId>org.springframework.boot</groupId>\n"
            + "            <artifactId>spring-boot-starter-test</artifactId>\n"
            + "            <scope>test</scope>\n"
            + "        </dependency>\n"
            + "        <!-- Logging dependencies for SLF4J with Logback -->\n"
            + "        <dependency>\n"
            + "            <groupId>org.slf4j</groupId>\n"
            + "            <artifactId>slf4j-api</artifactId>\n"
            + "        </dependency>\n"
            + "        <dependency>\n"
            + "            <groupId>ch.qos.logback</groupId>\n"
            + "            <artifactId>logback-classic</artifactId>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <groupId>org.springframework.boot</groupId>\n"
            + "                <artifactId>spring-boot-maven-plugin</artifactId>\n"
            + "                <configuration>\n"
            + "                    <excludes>\n"
            + "                        <exclude>\n"
            + "                            <groupId>org.projectlombok</groupId>\n"
            + "                            <artifactId>lombok</artifactId>\n"
            + "                        </exclude>\n"
            + "                    </excludes>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "            <!-- Static analysis plugin to find bugs and vulnerabilities -->\n"
            + "            <plugin>\n"
            + "                <groupId>com.github.spotbugs</groupId>\n"
            + "                <artifactId>spotbugs-maven-plugin</artifactId>\n"
            + "                <version>4.8.3.1</version>\n"
            + "                <executions>\n"
            + "                    <execution>\n"
            + "                        <goals>\n"
            + "                            <goal>check</goal>\n"
            + "                        </goals>\n"
            + "                    </execution>\n"
            + "                </executions>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>\n";

        try {
            Files.writeString(Paths.get(baseDir, "pom.xml"), pom);
            logger.info("✅ Created: pom.xml");
        } catch (IOException e) {
            logger.error("❌ Failed to write pom.xml: {}", e.getMessage());
        }
    }

    private static void addDependencyIfNotExists(List<String> dependencies, String newDependency) {
        String[] newDepParts = newDependency.split(":");
        String newGroupId = newDepParts[0];
        String newArtifactId = newDepParts[1];

        boolean exists = dependencies.stream().anyMatch(dep -> {
            String[] parts = dep.split(":");
            return parts.length >= 2 && parts[0].equals(newGroupId) && parts[1].equals(newArtifactId);
        });

        if (!exists) {
            dependencies.add(newDependency);
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
        String repoPath = parseSrsForValue(userInput, SRS_KEY_REPO_NAME); // Changed from repoName
        String javaVersion = parseSrsForValue(userInput, SRS_KEY_JAVA_VERSION);
        String springBootVersion = parseSrsForValue(userInput, SRS_KEY_SPRING_BOOT_VERSION);

        if (repoUrl == null || baseBranch == null || repoPath == null) {
            throw new IOException("SRS document must contain 'GitHub-URL', 'checkout_branch', and 'Repository-Name' keys.");
        }

        logger.info("  - Found Repo URL: {}", repoUrl);
        logger.info("  - Found Base Branch: {}", baseBranch);
        logger.info("  - Found Repo Name: {}", repoPath);
        
        GitConfig gitConfig = new GitConfig(repoUrl, baseBranch, repoPath);
        ProjectConfig projectConfig = new ProjectConfig(javaVersion, springBootVersion);
        logger.info("  - Using Java Version: {}", projectConfig.javaVersion);
        logger.info("  - Using Spring Boot Version: {}", projectConfig.springBootVersion);

        return new SrsData(gitConfig, projectConfig, userInput);
    }

    private static WorkflowResult runMainWorkflow(String userInput, ProjectConfig projectConfig, Map<String, String> agentPrompts) {
        final SequentialAgent workflow = buildWorkflow(projectConfig, agentPrompts);
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

    private static void generateProjectFiles(String repoName, WorkflowResult result, String srsContent, String changeAnalysis, ProjectConfig projectConfig) {
        writeClassesToFileSystem(result.codeAndTestOutput, repoName);

        if (result.dependencyList.isEmpty()) {
            logger.warn("⚠️ DependencyAgent did not return any dependencies. Falling back to default pom.xml.");
            List<String> defaultDeps = List.of(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-data-jpa",
                "org.postgresql:postgresql:runtime",
                "org.projectlombok:lombok:optional"
            );
            addPomXml(repoName, defaultDeps, projectConfig);
        } else {
            addPomXml(repoName, result.dependencyList, projectConfig);
        }

        updateReadme(repoName, result.requirementsSummary, result.dependencyList);
        addApplicationYml(repoName);
        addGithubActionsCiConfig(repoName);

        try {
            // Write changelog and state file together after all other code is generated.
            Path changelogPath = Paths.get(repoName, CHANGELOG_FILE_NAME);
            Files.writeString(changelogPath, changeAnalysis);
            logger.info("✅ Wrote change analysis to {}", CHANGELOG_FILE_NAME);

            Path aiStateDir = Paths.get(repoName, AI_STATE_DIR);
            Files.createDirectories(aiStateDir);
            Files.writeString(aiStateDir.resolve(SRS_FILE_NAME), srsContent);
            logger.info("✅ Saved current SRS to state file for next run.");
        } catch (IOException e) {
            logger.error("❌ Failed to write analysis or state files: {}", e.getMessage());
        }
    }

    private static String verifyProjectBuild(String repoName) {
        logger.info("\n--- 🛡️  Running Build & Static Analysis Verification ---");
        logger.info("This will compile the code, run tests, and check for vulnerabilities...");
        try {
            File workingDir = new File(repoName);
            // Using 'verify' phase runs compilation, tests, and the spotbugs:check goal
            runCommand(workingDir, getMavenExecutable(), "clean", "verify");
            logger.info("✅ Build successful. Code compiled, tests passed, and static analysis found no critical issues.");
            return null; // Return null on success
        } catch (IOException | InterruptedException e) {
            logger.error("❌ BUILD FAILED! A critical issue was found.", e);
            logger.error("  - The build failed, tests did not pass, or SpotBugs found a critical vulnerability.");
            logger.error("  - The faulty code will NOT be committed. Please review the logs above for details.");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // --- NEW: Analyze the build failure ---
            String buildLog = e.getMessage(); // The exception message now contains the full log
            String analysis = runReviewAgent(buildLog);
            logger.error("🤖 Review Agent Analysis:\n---\n{}\n---", analysis);
            return buildLog; // Return the log on failure
        }
    }

    private static String runReviewAgent(String buildLog) {
        logger.info("🤖 A build error was detected. Running Review Agent to analyze the failure...");
        LlmAgent reviewAgent = LlmAgent.builder()
                .name(REVIEW_AGENT_NAME)
                .description("Analyzes Maven build logs to find the root cause of a failure.")
                .instruction("""
You are an expert Java build engineer. You will be given the full log output from a failed Maven build (`mvn clean verify`).
Your task is to analyze the log, identify the primary root cause of the failure, and provide a concise, human-readable summary.

Focus on the first critical error you find (e.g., a Compilation Error, a specific test failure).
Explain what the error means and suggest a likely solution. Do not provide full code, just a clear explanation.

Example Analysis:
The build failed due to a compilation error in `EmployeeController.java`.
The error `package javax.validation does not exist` indicates that the code is using the old package name for Java Validation.
The fix is to update the import statements to use the `jakarta.validation` package, which is standard in Spring Boot 3, and to ensure the `spring-boot-starter-validation` dependency is included in the pom.xml.
""")
                .model("gemini-2.0-flash")
                .outputKey("review")
                .build();

        try {
            // Use the simpler, synchronous-style run method that handles session creation internally.
            final InMemoryRunner runner = new InMemoryRunner(reviewAgent);
            final Content userMsg = Content.fromParts(Part.fromText(buildLog));

            Event finalEvent = retryWithBackoff(() -> {
                Session session = runner.sessionService().createSession(runner.appName(), "user-review-analyzer").blockingGet();
                return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
            });
            return finalEvent != null ? finalEvent.stringifyContent() : "Review Agent failed to produce an analysis.";
        } catch (Exception e) {
            logger.error("❌ The Review Agent itself failed to run.", e);
            return "Review Agent execution failed: " + e.getMessage();
        }
    }

    private static CorrectorResult runCorrectorAgent(String buildLog, Map<String, String> currentPrompts) {
        logger.info("🤖 A build error was detected. Running Corrector Agent to generate a fix...");
        LlmAgent correctorAgent = LlmAgent.builder()
                .name(CORRECTOR_AGENT_NAME)
                .description("Analyzes a build log and the failing prompts to suggest a fix.")
                .instruction(String.format("""
You are a super-intelligent AI Workflow Engineer. Your purpose is to fix other AI agents that have failed.
You will be given a Maven build log from a failed build, along with the prompts that were given to the `CodeGenAgent` and `TestGenAgent`.

**Your Task:**
1.  **Analyze the build log** to determine the root cause of the error (e.g., compilation error, test failure).
2.  **Identify the failing agent.** Based on the error, decide whether the `CodeGenAgent` or the `TestGenAgent` is most likely at fault. For example, a test failure with `'void' type not allowed here` is the `TestGenAgent`'s fault. A `package javax.validation does not exist` error is the `CodeGenAgent`'s fault for using the wrong imports.
3.  **Rewrite the failing prompt.** Create a new, improved prompt for *only the failing agent*. This new prompt should be corrected to prevent the error you identified.
4.  **Provide a structured response.** You MUST provide *only* the raw key-value pair structure separated by the specified markers. Do not add any other text or explanation.

**Format:**
Failing-Agent: [The name of the agent to correct, e.g., CodeGenAgent or TestGenAgent]
---PROMPT-START---
[The full, new, corrected prompt for the failing agent]
---PROMPT-END---

**Build Log:**
```
%s
```

**Current CodeGenAgent Prompt:**
```
%s
```

**Current TestGenAgent Prompt:**
```
%s
```
""", buildLog,
  currentPrompts.get(CODEGEN_AGENT_NAME).replaceAll("(?m)^.*\\{requirements\\}.*$\\n?", ""),
  currentPrompts.get(TESTGEN_AGENT_NAME).replaceAll("(?m)^.*\\{code\\}.*$\\n?", "")))
                .model("gemini-2.0-flash")
                .outputKey("correction")
                .build();
        try {
            final InMemoryRunner runner = new InMemoryRunner(correctorAgent);
            final Content userMsg = Content.fromParts(Part.fromText("Analyze the provided logs and prompts and generate a corrected prompt."));
            Event finalEvent = retryWithBackoff(() -> {
                Session session = runner.sessionService().createSession(runner.appName(), "user-corrector-analyzer").blockingGet();
                return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
            });
            String response = finalEvent != null ? finalEvent.stringifyContent() : "";

            // Parse the structured response
            Pattern agentPattern = Pattern.compile("Failing-Agent:\\s*(\\w+)");
            Pattern promptPattern = Pattern.compile("---PROMPT-START---\\n(.*?)\\n---PROMPT-END---", Pattern.DOTALL);

            Matcher agentMatcher = agentPattern.matcher(response);
            Matcher promptMatcher = promptPattern.matcher(response);

            if (agentMatcher.find() && promptMatcher.find()) {
                String agentName = agentMatcher.group(1).trim();
                String newPrompt = promptMatcher.group(1).trim();
                logger.info("✅ Corrector Agent suggests a new prompt for: {}", agentName);
                return new CorrectorResult(agentName, newPrompt);
            } else {
                logger.error("❌ Corrector Agent returned a malformed response:\n{}", response);
                return null;
            }
        } catch (Exception e) {
            logger.error("❌ The Corrector Agent itself failed to run.", e);
            return null;
        }
    }

    private static String getMavenExecutable() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows") ? "mvn.cmd" : "mvn";
    }

    private static void finalizeAndSubmit(GitConfig gitConfig, String featureBranch, String commitMessage) {
        // Use the base name of the path for the zip file, not the full absolute path.
        // String repoBaseName = Paths.get(gitConfig.repoPath).getFileName().toString();
        // zipProject(gitConfig.repoPath, repoBaseName + ".zip");

        commitAndPush(gitConfig.repoPath, commitMessage, featureBranch);
        String prUrl = createPullRequest(gitConfig.repoPath, gitConfig.baseBranch, featureBranch, commitMessage);
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

        // --- NEW: Resolve output directory to a temp folder outside the current project ---
        try {
            Path projectRootPath = Paths.get(".").toRealPath();
            Path parentPath = projectRootPath.getParent();
            if (parentPath == null) {
                logger.error("❌ Cannot determine parent directory of the project. Aborting.");
                return;
            }
            // Define and create the temp directory.
            Path tempDir = parentPath.resolve("temp");
            Files.createDirectories(tempDir);

            // The original repo path from SRS is just the directory name.
            // Resolve it against the temp directory to get the desired absolute path.
            String originalRepoName = gitConfig.repoPath;
            Path absoluteRepoPath = tempDir.resolve(originalRepoName);
            gitConfig.repoPath = absoluteRepoPath.toString();
            logger.info("✅ Generated project will be created in: {}", gitConfig.repoPath);
        } catch (IOException e) {
            logger.error("❌ Could not determine project's real path or create temp directory. Aborting.", e);
            return;
        }
        // --- END NEW LOGIC ---

        try {
            ensureRepositoryIsReady(gitConfig.repoPath, gitConfig.repoUrl, gitConfig.baseBranch);
        } catch (Exception e) {
            logger.error("❌ Failed to prepare the repository for analysis. Aborting. Error: {}", e.getMessage());
            return;
        }

        // Perform change analysis by comparing the new SRS with the last known version.
        String changeAnalysis = performChangeAnalysis(gitConfig.repoPath, userInput);

        // If the analysis agent found no changes, skip the rest of the workflow.
        if (changeAnalysis.trim().equals(NO_CHANGES_DETECTED)) {
            logger.info("\n✅ No functional changes detected in SRS. The local repository has been updated to the latest from the base branch, but no feature branch will be created.");
            // The changelog is not written because no feature branch is created.
            return;
        }

        // Since changes were detected, proceed with creating a feature branch and cleaning it.
        String featureBranch;
        try {
            featureBranch = createFeatureBranchAndClean(gitConfig.repoPath);
        } catch (Exception e) {
            logger.error("❌ Failed to create feature branch. Aborting. Error: {}", e.getMessage());
            return;
        }

        // --- Original Workflow (Self-Healing Disabled) ---
        Map<String, String> agentPrompts = new HashMap<>();
        agentPrompts.put(CODEGEN_AGENT_NAME, """
You will be provided with text that contains a list of dependencies followed by structured requirements, separated by "---END-DEPS---".
You MUST IGNORE the dependency list and generate the core Java source code based ONLY on the structured requirements that appear AFTER the separator.

Generate the core Java source code for a Spring Boot microservice. The generated code MUST use the base package `com.generated.microservice`.

This includes:
- A main `Application` class in `com.generated.microservice`.
- Entity classes in `com.generated.microservice.entity`.
- Repository interfaces in `com.generated.microservice.repository`.
- Service classes in `com.generated.microservice.service`.
- Controller classes in `com.generated.microservice.controller`.

Also include:
- REST endpoints with proper annotations.
- OpenAPI documentation annotations (`@Operation`, `@ApiResponse`, etc.).
- Validation using the `jakarta.validation` package (e.g., `@Valid`, `@NotBlank`).
- PostgreSQL integration using Spring Data JPA.
- Lombok annotations for boilerplate code.

The structured requirements are:
{requirements}

Wrap each file with proper syntax and include a comment at the top indicating the file path, matching the package structure. For example:
// File: src/main/java/com/generated/microservice/service/UserService.java
""");
        agentPrompts.put(TESTGEN_AGENT_NAME, """
You are a senior test engineer. Your task is to write high-quality JUnit 5 test cases for the provided Spring Boot source code.
The source code consists of multiple concatenated Java files. Analyze it carefully.

**Instructions:**
1.  **Identify Services and Controllers:** Find all `@Service` and `@RestController` classes in the code below.
2.  **Test the Service Layer:**
    -   For each service class, create a corresponding test class (e.g., `UserServiceTest`).
    -   Use `@ExtendWith(MockitoExtension.class)` to enable Mockito.
    -   Use `@Mock` to create a mock of the repository dependency (e.g., `UserRepository`).
    -   Use `@InjectMocks` to inject the mock repository into the service instance.
    -   Write tests for each public method in the service, using Mockito's `when(...).thenReturn(...)` to define mock behavior.
3.  **Test the Controller Layer:**
    -   For each controller class, create a corresponding test class (e.g., `UserControllerTest`).
    -   Use `@WebMvcTest(ControllerClassName.class)` to test the web layer without starting a full application context.
    -   Use `@MockBean` to provide a mock of the service dependency (e.g., `UserService`).
    -   Use `MockMvc` to perform requests and assert responses.
4.  **Testing Void Methods:**
    -   For methods that return `void` (like a `delete` method), you CANNOT use `when(...).thenReturn(...)`.
    -   Instead, use `doNothing().when(mockedService).voidMethod(any());` to configure the mock.
    -   Use `verify(mockedService, times(1)).voidMethod(any());` in your test to confirm the method was called.
5.  **Pay Close Attention to Packages:** Ensure all `import` statements in your test files are correct and match the package structure of the provided source code.
    - The main source code will be in packages like `com.generated.microservice.controller`, `com.generated.microservice.service`, etc.
    - Your test source code MUST mirror this structure, e.g., `com.generated.microservice.controller` for controller tests.

**Input Code:**
{code}

Wrap each test class in Java syntax and include a comment at the top indicating the file path, for example:
// File: src/test/java/com/generated/microservice/service/UserServiceTest.java
""");

        final WorkflowResult workflowResult = runMainWorkflow(userInput, srsData.projectConfig, agentPrompts);

        if (workflowResult == null) {
            logger.error("Workflow execution failed. Could not generate project files. Aborting.");
            return;
        }

        generateProjectFiles(gitConfig.repoPath, workflowResult, userInput, changeAnalysis, srsData.projectConfig);

        // --- Quality Gate: Verify the build before committing ---
        String buildResult = verifyProjectBuild(gitConfig.repoPath);

        if (buildResult == null) {
            // --- HAPPY PATH: Build Succeeded ---
            logger.info("\n\n✅✅✅ Build Succeeded! Proceeding to commit and create Pull Request...");
            finalizeAndSubmit(gitConfig, featureBranch, workflowResult.commitMessage);
        } else {
            // --- FAILURE PATH: Build Failed ---
            logger.error("\n\n❌❌❌ Build Failed. Committing generated code with failure analysis...");
            String analysis = runReviewAgent(buildResult);
            try {
                Path analysisFile = Paths.get(gitConfig.repoPath, "BUILD_FAILURE_ANALYSIS.md");
                String fileContent = "# AI Build Failure Analysis\n\n"
                    + "The AI-generated code failed the build verification step. Here is the analysis from the Review Agent:\n\n"
                    + "---\n\n"
                    + analysis;
                Files.writeString(analysisFile, fileContent);
                logger.info("✅ Wrote build failure analysis to {}", analysisFile.getFileName());
            } catch (IOException e) {
                logger.error("❌ Failed to write build failure analysis file.", e);
            }
            // Commit and push the broken code and the analysis file, but do not create a PR.
            String failedCommitMessage = "fix(ai): [BUILD FAILED] " + workflowResult.commitMessage;
            commitAndPush(gitConfig.repoPath, failedCommitMessage, featureBranch);
        }
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
            logger.warn("Could not perform change analysis after multiple retries: {}", e.getMessage());
            return "Change analysis failed to run: " + e.getMessage();
        } catch (Exception e) {
            logger.warn("Could not perform change analysis: {}", e.getMessage());
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
                        logger.warn("Model request failed (attempt {}/{}) with a server error. Retrying in {} ms...", i + 1, maxRetries, delayMillis);
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
