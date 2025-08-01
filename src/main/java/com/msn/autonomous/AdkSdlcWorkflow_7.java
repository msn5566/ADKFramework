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
import java.nio.file.StandardOpenOption;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.json.JSONObject;


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
    private static final String CODE_MERGE_AGENT_NAME = "CodeMergeAgent";
    private static final String CONTEXT_EXTRACTION_AGENT_NAME = "ContextExtractionAgent";
    private static final String BUILD_CORRECTOR_AGENT_NAME = "BuildCorrectorAgent";

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
    private static final String SRS_KEY_PACKAGE_NAME = "Package-Name";

    // --- Constants for File System and Git ---
    private static final String AI_STATE_DIR = ".ai-state";
    private static final String JIRA_STATE_FILE_NAME = "jira_issue.txt";
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
        final String packageName;

        ProjectConfig(String javaVersion, String springBootVersion, String packageName) {
            this.javaVersion = (javaVersion != null && !javaVersion.isBlank()) ? javaVersion : "17";
            this.springBootVersion = (springBootVersion != null && !springBootVersion.isBlank()) ? springBootVersion : "3.5.3";
            this.packageName = (packageName != null && !packageName.isBlank()) ? packageName : "com.generated.microservice";
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

    private static class JiraConfig {
        final String jiraUrl;
        final String username;
        final String apiToken;
        final String issueKey;

        private JiraConfig(String jiraUrl, String username, String apiToken, String issueKey) {
            this.jiraUrl = jiraUrl;
            this.username = username;
            this.apiToken = apiToken;
            this.issueKey = issueKey;
        }

        private static JiraConfig fromEnvAndUserInput() throws IOException {
            String url = System.getenv("JIRA_URL");
            String email = System.getenv("JIRA_EMAIL");
            String token = System.getenv("JIRA_API_TOKEN");

            List<String> missingVars = new ArrayList<>();
            if (url == null || url.isBlank()) missingVars.add("JIRA_URL");
            if (email == null || email.isBlank()) missingVars.add("JIRA_EMAIL");
            if (token == null || token.isBlank()) missingVars.add("JIRA_API_TOKEN");

            if (!missingVars.isEmpty()) {
                throw new IOException("Missing required environment variables: " + String.join(", ", missingVars));
            }

            String issue;
            try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
                logger.info("Enter the Jira Issue Key (e.g., PROJ-123):");
                issue = scanner.nextLine().trim();
            }
            return new JiraConfig(url, email, token, issue);
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

    public static SequentialAgent buildWorkflow(ProjectConfig projectConfig, Map<String, String> agentPrompts, List<String> existingPomDependencies) {
        LlmAgent req = LlmAgent.builder()
                .name(REQUIREMENTS_AGENT_NAME)
                .description("Extracts structured functional requirements from a Jira user story.")
                .instruction("""
First, create a one-line summary of the following Jira user story, formatted as a conventional Git commit message. Prefix it with "Commit-Summary: ".
Then, on new lines, extract the structured requirements from the same user story.

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

**EXISTING DEPENDENCIES:**
%s

You are an expert at merging Maven dependencies. Your task is to produce a FINAL, MERGED list of dependencies.
Provide ONLY a list of `groupId:artifactId[:version][:scope]` tuples, one per line.
You MUST:
- **INCLUDE ALL** dependencies from the `EXISTING DEPENDENCIES` section, unless a newer version is explicitly required by the new feature.
- **ADD ONLY** new dependencies that are strictly necessary to implement the `NEW FEATURE REQUIREMENTS`.
- **UPDATE** the version of an existing dependency ONLY if the `NEW FEATURE REQUIREMENTS` necessitate a newer, compatible version.
- For any dependency NOT managed by the specified Spring Boot parent POM (like `springdoc-openapi` or other third-party libraries), you MUST provide an explicit, recent version number that is compatible with Spring Boot %s. For dependencies managed by Spring Boot, you MUST omit the version so the parent POM can manage it.

Use 'compile' for standard dependencies, 'runtime' for runtime-only, and 'optional' for tools like Lombok. If scope is 'compile', you can omit it.

After the dependency list, you MUST add a separator line containing exactly "---END-DEPS---".
After the separator, you MUST repeat the original requirements text provided below, exactly and without modification.

Example output format (assuming 'existing-dep' is from EXISTING DEPENDENCIES and 'new-dep' is a new requirement):
org.springframework.boot:spring-boot-starter-web
com.example:existing-dep
com.new.feature:new-dep:1.0.0
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0
---END-DEPS---
Feature: User Management API
...
Requirements:
{requirements}
""", projectConfig.javaVersion, projectConfig.springBootVersion, String.join("\n", existingPomDependencies), projectConfig.springBootVersion))
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
        logger.info("--- 🤖 Starting Change Analysis Agent ---");
        LlmAgent changeAgent = LlmAgent.builder()
                .name(CHANGE_ANALYSIS_AGENT_NAME)
                .description("Compares old and new Jira stories to generate a changelog.")
                .instruction("""
You will be given an old and a new version of a Jira user story, separated by markers.
Analyze the differences and generate a concise, human-readable changelog in Markdown format.
Focus on added, removed, and modified features. If the old story is empty, state that this is the initial version of the project.
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
        logger.info("--- ✅ Finished Change Analysis Agent ---");
        return finalEvent != null ? finalEvent.stringifyContent() : "";
    }

    private static String runCodeMergeAgent(String existingCode, String newFullFile) {
        logger.info("--- 🤖 Starting Code Merge Agent ---");
        LlmAgent mergeAgent = LlmAgent.builder()
            .name(CODE_MERGE_AGENT_NAME)
            .description("Intelligently merges a new full Java file into an existing Java file.")
            .instruction("""
You are an expert Java developer specializing in code merging. You will be given two versions of the same Java file: "EXISTING FILE CONTENT" and "NEW FILE CONTENT".

Your task is to perform a careful, intelligent merge. Follow these steps precisely:

1.  **Identify the differences.** Compare the two files to find what has been added, modified, or removed in the "NEW FILE CONTENT". The "NEW FILE CONTENT" represents the desired state for a new feature, while the "EXISTING FILE CONTENT" contains other, unrelated features that MUST be preserved.

2.  **Preserve existing code.** All methods, fields, imports, and annotations from the "EXISTING FILE CONTENT" MUST be kept unless they are explicitly replaced by an updated version in the "NEW FILE CONTENT".

3.  **Integrate new code.** Add all new methods, fields, and necessary imports from the "NEW FILE CONTENT" into the "EXISTING FILE CONTENT".

4.  **Handle conflicts.** If a method or field exists in both files but has been modified, use the version from the "NEW FILE CONTENT".

5.  **Combine imports.** The final code must contain a clean, de-duplicated list of all necessary imports from both files.

The final output MUST be a single, complete, and compilable Java file that contains **ALL** features from both the existing and new versions. Do not add any explanation, markers, or ```java ... ``` code blocks. Your response must be only the raw, merged Java code.
            """)
            .model("gemini-2.0-flash")
            .outputKey("merged_code")
            .build();

        final InMemoryRunner runner = new InMemoryRunner(mergeAgent);
        
        // Pass dynamic content in the user message, not the instruction prompt.
        String combinedInput = String.format("""
            --- EXISTING FILE CONTENT ---
            %s
            --- END EXISTING FILE CONTENT ---

            --- NEW FILE CONTENT ---
            %s
            --- END NEW FILE CONTENT ---
            """, existingCode, newFullFile);

        final Content userMsg = Content.fromParts(Part.fromText(combinedInput));


        try {
            Event finalEvent = retryWithBackoff(() -> {
                Session session = runner.sessionService().createSession(runner.appName(), "user-code-merger").blockingGet();
                return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
            });
            String mergedCode = finalEvent != null ? finalEvent.stringifyContent().trim() : "";

            // --- NEW: Add detailed logging ---
            if (mergedCode.isEmpty()) {
                logger.warn("⚠️ CodeMergeAgent returned an empty response. Falling back to original code.");
            } else {
                logger.info("✅ CodeMergeAgent returned merged code. Content length: {}", mergedCode.length());
                // For debugging, log a snippet of the merged code
                logger.debug("Merged code snippet:\n---\n{}\n---", mergedCode.substring(0, Math.min(mergedCode.length(), 200)));
            }
            // --- END NEW LOGIC ---

            System.out.println("existingCode: " + existingCode);
            System.out.println("newFullFile: " + newFullFile);
            // If the agent returns an empty response, it's safer to return the original code.
            String result = mergedCode.isEmpty() ? existingCode : mergedCode;
            logger.info("--- ✅ Finished Code Merge Agent ---");
            return result;
        } catch (Exception e) {
            logger.error("❌ The CodeMergeAgent failed to run. Returning original code. Error: {}", e.getMessage(), e);
            logger.info("--- ❌ Finished Code Merge Agent with error ---");
            return existingCode; // Fallback to old code on any failure
        }
    }


    public static void writeClassesToFileSystem(String combinedOutput, String baseDir) {
        // Regex to capture the action (Create/Modify), file path, and the code block.
        // It looks for a marker like "// Create File: " or "// Modify File: "
        Pattern pattern = Pattern.compile("// (Create File|Modify File|Refactored File): ([^\\n]+)\\s*\\n(.*?)(?=\\n// (?:Create|Modify|Refactored) File:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(combinedOutput);
        System.out.println("==========combinedOutput start: =================");
        System.out.println(combinedOutput);
        System.out.println("============combinedOutput end=================");

        while (matcher.find()) {
            String action = matcher.group(1).trim();
            String relativePath = matcher.group(2).trim();
            String rawContent = matcher.group(3).trim();

            System.out.println("action: " + action);
            System.out.println("relativePath: " + relativePath);
            System.out.println("rawContent: " + rawContent);

            if (!rawContent.startsWith("```")) {
                rawContent = "```java\n" + rawContent ;
            }

            String content = filteredContent(rawContent);

            

            if (content.isEmpty()) {
                logger.warn("⚠️ Skipping empty code block for {}", relativePath);
                continue;
            }

            Path filePath = Paths.get(baseDir, relativePath);

            if ("Create File".equals(action)) {
                try {
                    Files.createDirectories(filePath.getParent());
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    logger.info("✅ Created: {}", filePath);
                } catch (IOException e) {
                    logger.error("❌ Failed to write new file: {} - {}", filePath, e.getMessage());
                }
            } else if ("Modify File".equals(action)) {
                if (!Files.exists(filePath)) {
                    logger.info("❌ Cannot modify file that does not exist: {}. Treating as a new file.", filePath);
                     try {
                        Files.createDirectories(filePath.getParent());
                        Files.writeString(filePath, content, StandardCharsets.UTF_8);
                        logger.info("✅ Created (as fallback): {}", filePath);
                    } catch (IOException e) {
                        logger.error("❌ Failed to write fallback file: {} - {}", filePath, e.getMessage());
                    }
                    continue;
                }

                try {
                    String existingCode = Files.readString(filePath, StandardCharsets.UTF_8);
                    String newJavaCode = content;               

                    // Run the merge agent to combine existing code with the new snippet.
                    String mergedCode = filteredContent(runCodeMergeAgent(existingCode, newJavaCode));
                    System.out.println("mergedCode: " + mergedCode);
                    Files.writeString(filePath, mergedCode, StandardCharsets.UTF_8); // Overwrite with merged content
                    logger.info("✅ Merged and updated: {}", filePath);

            } catch (IOException e) {
                    logger.error("❌ Failed to read or write modified file: {} - {}", filePath, e.getMessage());
                }
            } else if ("Refactored File".equals(action)) {
                try {
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        logger.info("🗑️ Deleted existing file for refactoring: {}", filePath);
                    }
                    Files.createDirectories(filePath.getParent());
                    Files.writeString(filePath, content, StandardCharsets.UTF_8);
                    logger.info("✅ Refactored and Created New File: {}", filePath);
                } catch (IOException e) {
                    logger.error("❌ Failed to refactor/write file: {} - {}", filePath, e.getMessage());
                }
            }
        }
    }

    private static String filteredContent(String rawContent) {
        // Always wrap rawContent in ```java ... ``` if not already present
        String content = "";

        // Extract content from markdown code blocks (e.g., ```java ... ```) if they exist.
        Pattern codeBlockPattern = Pattern.compile("```(?:java)?\\s*\\n(.*?)\\n```", Pattern.DOTALL);
        Matcher codeMatcher = codeBlockPattern.matcher(rawContent);

        if (codeMatcher.find()) {
            content = codeMatcher.group(1).trim();
        } else {
            content = rawContent; // Use raw content if no markdown block is found
        }
        return content.replace("```java", "").replace("```", "");
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
        Pattern pattern = Pattern.compile("^" + key + ":\\s*(.+)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(srsContent);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private static void ensureRepositoryIsReady(String outputDir, String repoUrl, String baseBranch) throws IOException, InterruptedException {
        File dir = new File(outputDir);
        if (dir.exists()) {
            logger.info("Repository directory exists. Resetting to a clean state from origin/{}.", baseBranch);
            runCommand(dir, "git", "fetch", "origin"); // Make sure remote refs are up-to-date
            runCommand(dir, "git", "checkout", baseBranch); // Switch to the branch
            runCommand(dir, "git", "reset", "--hard", "origin/" + baseBranch); // Hard reset to match remote
            runCommand(dir, "git", "clean", "-fdx"); // Remove all untracked files and directories
            logger.info("✅ Repository is now in a pristine state matching origin/{}.", baseBranch);
        } else {
            logger.info("Cloning repository from {}", repoUrl);
            // More efficient clone: only get the single-branch history needed for analysis.
            runCommand(new File("."), "git", "clone", "--branch", baseBranch, "--single-branch", repoUrl, outputDir);
        }
    }

    private static String createFeatureBranch(String outputDir, String issueKey) throws IOException, InterruptedException {
        File dir = new File(outputDir);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(dtf);
        String featureBranch = "feature/" + issueKey + "_" + timestamp;

        logger.info("Creating and checking out new feature branch: {}", featureBranch);
        runCommand(dir, "git", "checkout", "-b", featureBranch);

        // logger.info("Cleaning workspace on new feature branch...");
        // // List of files/directories to remove before generating new code.
        // // This ensures no stale files are left from previous runs.
        // String[] itemsToClean = {"src", "pom.xml", ".github"};
        // for (String itemName : itemsToClean) {
        //     Path itemPath = Paths.get(outputDir, itemName);
        //     if (Files.exists(itemPath)) {
        //         try {
        //             if (Files.isDirectory(itemPath)) {
        //                 // Recursively delete directory
        //                 Files.walk(itemPath)
        //                     .sorted(Comparator.reverseOrder())
        //                     .map(Path::toFile)
        //                     .forEach(File::delete);
        //             } else {
        //                 Files.delete(itemPath);
        //             }
        //             logger.info("  - Removed: {}", itemName);
        //         } catch (IOException e) {
        //             logger.warn("⚠️  Could not remove '{}'. Please check file permissions. Error: {}", itemPath, e.getMessage());
        //         }
        //     }
        // }
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
        // For springdoc, we ENFORCE the version to avoid agent hallucinations.
        enforceDependency(managedDependencies, "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0");
        // Always ensure the test starter is present.
        addDependencyIfNotExists(managedDependencies, "org.springframework.boot:spring-boot-starter-test:test");


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
            + "        </plugins>\n"
            + "    </build>\n"
            + "\n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>maven-central</id>\n"
            + "            <url>https://repo.maven.apache.org/maven2</url>\n"
            + "        </repository>\n"
            + "        <repository>\n"
            + "            <id>atlassian-public</id>\n"
            + "            <url>https://packages.atlassian.com/maven/repository/public</url>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "\n"
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

    private static void enforceDependency(List<String> dependencies, String dependencyToEnforce) {
        String[] parts = dependencyToEnforce.split(":");
        String groupId = parts[0];
        String artifactId = parts[1];

        // Remove any existing dependency with the same groupId and artifactId, regardless of version
        dependencies.removeIf(dep -> {
            String[] depParts = dep.split(":");
            return depParts.length >= 2 && depParts[0].equals(groupId) && depParts[1].equals(artifactId);
        });

        // Add the dependency with the correct, enforced version
        dependencies.add(dependencyToEnforce);
        logger.info("🤖 Enforced known-good version for dependency: {}", dependencyToEnforce);
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

    private static List<String> parseExistingDependenciesFromPom(String pomContent) {
        logger.info("--- 🤖 Starting parseExistingDependenciesFromPom ---");
        logger.debug("Parsing pomContent (first 500 chars):\n{}", pomContent.substring(0, Math.min(pomContent.length(), 500)));

        List<String> dependencies = new ArrayList<>();
        Pattern dependencyBlockPattern = Pattern.compile("<dependency>(.*?)</dependency>", Pattern.DOTALL);
        Matcher blockMatcher = dependencyBlockPattern.matcher(pomContent);

        Pattern groupIdPattern = Pattern.compile("<groupId>\\s*(.*?)\\s*</groupId>", Pattern.DOTALL);
        Pattern artifactIdPattern = Pattern.compile("<artifactId>\\s*(.*?)\\s*</artifactId>", Pattern.DOTALL);
        Pattern versionPattern = Pattern.compile("<version>\\s*(.*?)\\s*</version>", Pattern.DOTALL);
        Pattern scopePattern = Pattern.compile("<scope>\\s*(.*?)\\s*</scope>", Pattern.DOTALL);
        // Make optional pattern more flexible to just check for presence of the tag, not its content
        Pattern optionalPattern = Pattern.compile("<optional(?:\\s*[^>]*?)?>.*?</optional>|<optional\\s*/>", Pattern.DOTALL);

        while (blockMatcher.find()) {
            String dependencyXml = blockMatcher.group(1); // Content within <dependency>...</dependency>
            logger.debug("Found dependency block:\n{}", dependencyXml);

            String groupId = null;
            String artifactId = null;
            String version = null;
            String scope = null;
            boolean optional = false;

            Matcher m;

            m = groupIdPattern.matcher(dependencyXml);
            if (m.find()) groupId = m.group(1).trim();
            logger.debug("  - groupId: {}", groupId);

            m = artifactIdPattern.matcher(dependencyXml);
            if (m.find()) artifactId = m.group(1).trim();
            logger.debug("  - artifactId: {}", artifactId);

            m = versionPattern.matcher(dependencyXml);
            if (m.find()) version = m.group(1).trim();
            logger.debug("  - version: {}", version);

            m = scopePattern.matcher(dependencyXml);
            if (m.find()) scope = m.group(1).trim();
            logger.debug("  - scope: {}", scope);

            // Check for optional tag presence
            optional = optionalPattern.matcher(dependencyXml).find();
            logger.debug("  - optional: {}", optional);

            if (groupId != null && artifactId != null) {
                StringBuilder dep = new StringBuilder();
                dep.append(groupId).append(":").append(artifactId);
                if (version != null) {
                    dep.append(":").append(version);
                }
                if (scope != null && !scope.equalsIgnoreCase("compile")) {
                    dep.append(":").append(scope);
                }
                if (optional) {
                    dep.append(":optional");
                }
                dependencies.add(dep.toString());
                logger.debug("  - Added dependency: {}", dep.toString());
            } else {
                logger.warn("  - Skipping dependency block due to missing groupId or artifactId: {}", dependencyXml);
            }
        }
        logger.info("--- ✅ Finished parseExistingDependenciesFromPom. Found {} dependencies. ---", dependencies.size());
        return dependencies;
    }

    private static String getJiraIssueContent(JiraConfig jiraConfig) throws Exception {
        logger.info("Connecting to Jira to fetch issue: {}", jiraConfig.issueKey);

        HttpClient client = HttpClient.newHttpClient();
        String url = jiraConfig.jiraUrl + "/rest/api/2/issue/" + jiraConfig.issueKey;

        String auth = jiraConfig.username + ":" + jiraConfig.apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(url))
            .header("Authorization", "Basic " + encodedAuth)
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch Jira issue. Status code: " + response.statusCode() + " - " + response.body());
        }

        JSONObject issueJson = new JSONObject(response.body());
        JSONObject fields = issueJson.getJSONObject("fields");

        String summary = "Feature: " + fields.getString("summary");
        String description = fields.optString("description", "");

        logger.info("✅ Successfully fetched Jira issue: {}", jiraConfig.issueKey);
        logger.debug("  - Summary: {}", summary);
        logger.debug("  - Description: {}", description);

        return summary + "\n\n" + description;
    }

    /**
     * A simple data class to hold configuration extracted by the ConfigAgent.
     */
    private static class ExtractedConfig {
        final GitConfig gitConfig;
        final ProjectConfig projectConfig;

        ExtractedConfig(GitConfig gitConfig, ProjectConfig projectConfig) {
            this.gitConfig = gitConfig;
            this.projectConfig = projectConfig;
        }
    }

    private static ExtractedConfig runConfigAgent(String srsContent) throws IOException {
        logger.info("--- 🤖 Starting Config Agent ---");
        LlmAgent configAgent = LlmAgent.builder()
                .name("ConfigAgent")
                .description("Extracts all key project configurations from a Jira user story.")
                .instruction("""
                    You are an expert configuration parser. Analyze the following Jira user story text.
                    Your task is to intelligently extract values for a predefined set of configuration keys.

                    **Be flexible with the input format.** The keys in the story text might be phrased differently, have different casing, or lack hyphens. You must map them to the canonical keys below.
                    - **Handle Partial Versions**: If the `SpringBoot-Version` in the text is a partial or wildcard version (e.g., `3.2.x`, `3.2.*`, or just `3.2`), you MUST ignore it and use the default version instead. Only use a version from the text if it is a complete, concrete version (e.g., `3.5.3`).
                    For example:
                    - "java 21", "java-version: 21", or "Java Version 21" should all map to `Java-Version: 21`.
                    - "spring boot 3.5.3" should be mapped to `SpringBoot-Version: 3.5.3`.
                    - "spring boot 3.2.x" or "springboot-version: 3.2" MUST be ignored, and you should use the default.
                    - "Repo Name my-project" or "Repository-Name: my-project" should map to `Repository-Name: my-project`.

                    **Output Format:**
                    You MUST respond with ONLY the canonical key-value pairs, one per line. Do not include any other text or explanation.

                    **Canonical Keys to Extract:**
                    - `GitHub-URL`
                    - `checkout_branch`
                    - `Repository-Name`
                    - `Java-Version`
                    - `SpringBoot-Version`
                    - `Package-Name`

                    **Default Values:**
                    If a value is not specified for `Java-Version`, `SpringBoot-Version`, or if the `SpringBoot-Version` is partial/wildcard, you MUST use the following default values:
                    - `Java-Version: 17`
                    - `SpringBoot-Version: 3.5.3`
                    If `Package-Name` is not specified, default to `com.generated.microservice`.

                    **Mandatory Keys:**
                    The keys `GitHub-URL`, `checkout_branch`, and `Repository-Name` are mandatory. If you cannot find them in the text, respond with an empty value for that key.
                    """)
                .model("gemini-2.0-flash")
                .outputKey("config")
                .build();

        try {
            final InMemoryRunner runner = new InMemoryRunner(configAgent);
            final Content userMsg = Content.fromParts(Part.fromText(srsContent));

            Event finalEvent = retryWithBackoff(() -> {
                Session session = runner.sessionService().createSession(runner.appName(), "user-config-analyzer").blockingGet();
                return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
            });

            String response = finalEvent != null ? finalEvent.stringifyContent() : "";
            logger.debug("ConfigAgent Response:\\n{}", response);

            String repoUrl = parseSrsForValue(response, SRS_KEY_GITHUB_URL);
            String baseBranch = parseSrsForValue(response, SRS_KEY_CHECKOUT_BRANCH);
            String repoPath = parseSrsForValue(response, SRS_KEY_REPO_NAME);
            String javaVersion = parseSrsForValue(response, SRS_KEY_JAVA_VERSION);
            String springBootVersion = parseSrsForValue(response, SRS_KEY_SPRING_BOOT_VERSION);
            String packageName = parseSrsForValue(response, SRS_KEY_PACKAGE_NAME);

            // --- NEW: Sanitize the repository URL to handle formatting issues from Jira ---
            if (repoUrl != null && repoUrl.contains("|")) {
                logger.warn("Malformed repository URL detected: '{}'. Sanitizing...", repoUrl);
                repoUrl = repoUrl.split("\\|")[0].trim();
                logger.info("Sanitized URL: '{}'", repoUrl);
            }
            // --- END NEW LOGIC ---

            // --- NEW: Validate mandatory fields and fail fast ---
            List<String> missingKeys = new ArrayList<>();
            if (repoUrl == null || repoUrl.isBlank()) missingKeys.add(SRS_KEY_GITHUB_URL);
            if (baseBranch == null || baseBranch.isBlank()) missingKeys.add(SRS_KEY_CHECKOUT_BRANCH);
            if (repoPath == null || repoPath.isBlank()) missingKeys.add(SRS_KEY_REPO_NAME);

            if (!missingKeys.isEmpty()) {
                String errorMessage = "ConfigAgent failed to extract mandatory keys: " + String.join(", ", missingKeys)
                    + ". Please ensure they are present and have values in the Jira user story description.";
                throw new IOException(errorMessage);
            }
            // --- END NEW LOGIC ---

            GitConfig gitConfig = new GitConfig(repoUrl, baseBranch, repoPath);
            ProjectConfig projectConfig = new ProjectConfig(javaVersion, springBootVersion, packageName);

            logger.info("--- ✅ Finished Config Agent ---");
            return new ExtractedConfig(gitConfig, projectConfig);
        } catch (IOException e) {
            // Re-throw IOExceptions (from our validation) directly
            logger.info("--- ❌ Finished Config Agent with error ---");
            throw e;
        } catch (Exception e) {
            String errorMessage = "The Config Agent failed to execute due to an internal error.";
            logger.error("❌ " + errorMessage, e);
            // Wrap other exceptions in IOException to signal a configuration failure
            logger.info("--- ❌ Finished Config Agent with error ---");
            throw new IOException(errorMessage, e);
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

        // The runConfigAgent method now handles its own validation and throws on failure.
        ExtractedConfig config = runConfigAgent(userInput);

        logger.info("  - Found Repo URL: {}", config.gitConfig.repoUrl);
        logger.info("  - Found Base Branch: {}", config.gitConfig.baseBranch);
        logger.info("  - Found Repo Name: {}", config.gitConfig.repoPath);
        logger.info("  - Using Java Version: {}", config.projectConfig.javaVersion);
        logger.info("  - Using Spring Boot Version: {}", config.projectConfig.springBootVersion);
        logger.info("  - Using Package Name: {}", config.projectConfig.packageName);

        return new SrsData(config.gitConfig, config.projectConfig, userInput);
    }

    private static WorkflowResult runMainWorkflow(String userInput, ProjectConfig projectConfig, Map<String, String> agentPrompts, List<String> existingPomDependencies) {
        final SequentialAgent workflow = buildWorkflow(projectConfig, agentPrompts, existingPomDependencies);
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

    private static void generateProjectFiles(String repoName, WorkflowResult result, String srsContent, String changeAnalysis, ProjectConfig projectConfig, String featureBranch) {
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

        // For README, we'll create the full summary content first.
        StringBuilder readmeContent = new StringBuilder();
        readmeContent.append("## 📝 Project Summary\n\n")
            .append(result.requirementsSummary)
            .append("\n\n### 🛠️ Core Dependencies\n\n");
        for(String dep : result.dependencyList) {
            readmeContent.append("- `").append(dep).append("`\n");
        }

        // Append to changelog, state file, and README to keep a running history.
        appendContentWithMetadata(Paths.get(repoName, CHANGELOG_FILE_NAME), changeAnalysis, featureBranch);
        appendContentWithMetadata(Paths.get(repoName, AI_STATE_DIR, JIRA_STATE_FILE_NAME), srsContent, featureBranch);
        appendContentWithMetadata(Paths.get(repoName, "README.md"), readmeContent.toString(), featureBranch);

        addApplicationYml(repoName);
        addGithubActionsCiConfig(repoName);
    }

    private static String verifyProjectBuild(String repoName) {
        logger.info("\n--- 🛡️  Running Build & Static Analysis Verification ---");
        logger.info("Wait .... Manven Build is running ...");
        try {
            File workingDir = new File(repoName);
            // Using 'verify' phase runs compilation, tests
            runCommand(workingDir, getMavenExecutable(), "clean", "verify");
            logger.info("✅ Build successful. Code compiled, tests passed, and static analysis found no critical issues.");
            return null; // Return null on success
        } catch (IOException | InterruptedException e) {
            logger.error("❌ BUILD FAILED! A critical issue was found.", e);
            logger.error("  - The build failed, tests did not pass.");
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
        logger.info("--- 🤖 Starting Review Agent ---");
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
            logger.info("--- ✅ Finished Review Agent ---");
            return finalEvent != null ? finalEvent.stringifyContent() : "Review Agent failed to produce an analysis.";
        } catch (Exception e) {
            logger.error("❌ The Review Agent itself failed to run.", e);
            logger.info("--- ❌ Finished Review Agent with error ---");
            return "Review Agent execution failed: " + e.getMessage();
        }
    }

    private static CorrectorResult runCorrectorAgent(String buildLog, Map<String, String> currentPrompts) {
        logger.info("--- 🤖 A build error was detected. Starting Corrector Agent ---");
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
                logger.info("--- ✅ Finished Corrector Agent ---");
                return new CorrectorResult(agentName, newPrompt);
            } else {
                logger.error("❌ Corrector Agent returned a malformed response:\n{}", response);
                logger.info("--- ❌ Finished Corrector Agent with error ---");
                return null;
            }
        } catch (Exception e) {
            logger.error("❌ The Corrector Agent itself failed to run.", e);
            logger.info("--- ❌ Finished Corrector Agent with error ---");
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

    private static String getCurrentProjectFiles(String repoPath) {
        StringBuilder fileList = new StringBuilder();
        Path startPath = Paths.get(repoPath);
        try {
            Files.walk(startPath)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    // Make path relative to the repo root for clarity
                    Path relativePath = startPath.relativize(path);
                    fileList.append(relativePath.toString().replace('\\', '/')).append("\n");
                });
        } catch (IOException e) {
            logger.error("❌ Could not read existing project files: {}", e.getMessage());
            return "Could not read project file structure.";
        }
        if (fileList.length() == 0) {
            return "No existing .java files found. This appears to be a new project.";
        }
        return fileList.toString();
    }

    public static void main(String[] args) {
        JiraConfig jiraConfig;
        try {
            jiraConfig = JiraConfig.fromEnvAndUserInput();
        } catch (IOException e) {
            logger.error("❌ Configuration error: {}", e.getMessage());
            logger.error("  - Please set JIRA_URL, JIRA_EMAIL, and JIRA_API_TOKEN environment variables.");
            return;
        }

        String userInput;
        try {
            userInput = getJiraIssueContent(jiraConfig);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch Jira issue: {}. Please check your credentials, URL, and issue key.", e.getMessage());
            return;
        }

        ExtractedConfig extractedConfig;
        try {
            extractedConfig = runConfigAgent(userInput);
        } catch (IOException e) {
            logger.error("❌ Failed to read configuration from Jira issue description: {}", e.getMessage());
            return;
        }

        GitConfig gitConfig = extractedConfig.gitConfig;
        ProjectConfig projectConfig = extractedConfig.projectConfig;
        SrsData srsData = new SrsData(gitConfig, projectConfig, userInput);


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

        // Since changes were detected, proceed with creating a feature branch.
        String featureBranch;
        try {
            featureBranch = createFeatureBranch(gitConfig.repoPath, jiraConfig.issueKey);
        } catch (Exception e) {
            logger.error("❌ Failed to create feature branch. Aborting. Error: {}", e.getMessage());
            return;
        }

        // Get the list of existing files to provide context to the agent.
        String existingFiles = getCurrentProjectFiles(gitConfig.repoPath);

        // --- NEW: Context Extraction for ALL existing Java files ---
        StringBuilder allContextSummaries = new StringBuilder();
        try {
            Path srcPath = Paths.get(gitConfig.repoPath, "src", "main", "java");
            if (Files.exists(srcPath)) {
                Files.walk(srcPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                          //  if(path.toString().contains("Controller") || path.toString().contains("entity") || path.toString().contains("model")) {
                                String fileContent = Files.readString(path);
                                String contextSummary = runContextExtractionAgent(fileContent);
                                allContextSummaries.append("--- File: ").append(srcPath.relativize(path)).append(" ---\n");
                                allContextSummaries.append(contextSummary).append("\n\n");
                            //}
                        } catch (IOException e) {
                            logger.warn("Could not read or process file for context: {}", path);
                        }
                    });
            }
        } catch (IOException e) {
            logger.error("❌ Could not walk source tree for context extraction: {}", e.getMessage());
        }
        String combinedContext = allContextSummaries.toString();
        // --- END Context Extraction ---

        // --- NEW: Read existing pom.xml and parse dependencies for DependencyAgent ---
        List<String> existingPomDependencies = new ArrayList<>();
        Path pomFilePath = Paths.get(gitConfig.repoPath, "pom.xml");
        if (Files.exists(pomFilePath)) {
            try {
                String pomContent = Files.readString(pomFilePath);
                existingPomDependencies = parseExistingDependenciesFromPom(pomContent);
                logger.info("Existing pom.xml content: {}", existingPomDependencies);
                logger.info("Found existing pom.xml with {} dependencies.", existingPomDependencies.size());
            } catch (IOException e) {
                logger.warn("Could not read or parse existing pom.xml for dependencies: {}", e.getMessage());
            }
        } else {
            logger.info("No existing pom.xml found. DependencyAgent will start from a clean slate.");
        }
        // --- END NEW LOGIC ---

        // --- Original Workflow (Self-Healing Disabled) ---
        Map<String, String> agentPrompts = new HashMap<>();
        agentPrompts.put(CODEGEN_AGENT_NAME, String.format("""
You are a specialist Java developer. Your task is to implement features in a Spring Boot project.

**Primary Objective:** First, analyze the `EXISTING PROJECT FILES` list. If you do not see a main application class (one annotated with `@SpringBootApplication`), you MUST create it in the base package (`%s`). After ensuring the main class exists, proceed to implement the new feature.

**EXISTING FILE CONTEXT:**
%s

**MASTER DIRECTIVE: Principle of Least Functionality**
This is your most important instruction. You are FORBIDDEN from generating any code, methods, or endpoints that are not EXPLICITLY required by the new feature description.
- **Example:** If the requirement is to "find an employee by name," you will ONLY generate the controller endpoint, service method, and repository method for that search. You are FORBIDDEN from creating `getAllEmployees`, `getEmployeeById`, `addEmployee`, `updateEmployee`, or `deleteEmployee`.
- You must write the minimum amount of code to satisfy the requirement.
- You MUST NOT generate any test classes (files ending in Test.java). Test generation is handled by a separate agent.

**SERVICE LAYER DIRECTIVE:**
- For any service class you create, you MUST define an interface (e.g., `EmployeeService`) and a corresponding implementation class (e.g., `EmployeeServiceImpl`).
- All dependent classes (like controllers) MUST inject and use the interface, not the concrete implementation.

**CRITICAL INSTRUCTIONS:**
1.  **Analyze Existing Structure:** Review the list of existing files and the `EXISTING FILE CONTEXT` to understand the current project state.
2.  **Dependency Method Generation:** When your new code needs to call a method on a dependency (like a Service or Repository), you MUST first check the `EXISTING FILE CONTEXT` to see if that method already exists.
     - If the method **already exists**, simply call it.
     - If the method **does not exist**, you MUST generate the new method in the appropriate file using a `// Modify File:` block, in addition to generating the code that calls it. This is critical for ensuring the project compiles. For example, if you need `employeeRepository.findByName(name)`, but it doesn't exist, you must add the method to the `EmployeeRepository` interface/class.
3.  **Generate Code Snippets:**
    - For **new files**, provide the complete content.
    - For **existing files**, you MUST ONLY generate the new code snippet (e.g., a new method, a new DTO class within a file, a new field, a new endpoint). DO NOT output the entire file.
4.  **Output Format:**
    - For a **NEW file**, use the format with a full path from the project root. Example: `// Create File: src/main/java/com/yourcompany/service/UserService.java`
    - For **MODIFYING an existing file**, use the format with a full path from the project root. Example: `// Modify File: src/main/java/com/yourcompany/service/UserService.java`
5.  **Adhere to Project Standards:**
    - Use the existing base package: `%s`.
    - Follow the existing coding style and patterns (e.g., constructor injection).
    - Use Java `%s`.
    - All generated code MUST use `jakarta.validation` for validation, not `javax.validation`.
    - For all injected dependencies (like Services and Repositories), declare the fields as `private final` and use constructor injection. Lombok's `@RequiredArgsConstructor` is preferred.
    - If an update method is requested, you MUST first fetch the existing entity, update its fields, and then save the modified entity.

**EXISTING PROJECT FILES:**
%s

**NEW FEATURE REQUIREMENTS:**
{requirements}
""",
  srsData.projectConfig.packageName,
  combinedContext,
  srsData.projectConfig.packageName,
  srsData.projectConfig.javaVersion,
  existingFiles
));
        agentPrompts.put(DEPENDENCY_AGENT_NAME, String.format("""
Based on the following requirements, identify the necessary Maven dependencies for a project using Java %s and Spring Boot %s.
This version context is CRITICAL for selecting compatible dependency versions.

**EXISTING DEPENDENCIES:**
%s

You are an expert at merging Maven dependencies. Your task is to produce a FINAL, MERGED list of dependencies.
Provide ONLY a list of `groupId:artifactId[:version][:scope]` tuples, one per line.
You MUST:
- **INCLUDE ALL** dependencies from the `EXISTING DEPENDENCIES` section, unless a newer version is explicitly required by the new feature.
- **ADD ONLY** new dependencies that are strictly necessary to implement the `NEW FEATURE REQUIREMENTS`.
- **UPDATE** the version of an existing dependency ONLY if the `NEW FEATURE REQUIREMENTS` necessitate a newer, compatible version.
- For any dependency NOT managed by the specified Spring Boot parent POM (like `springdoc-openapi` or other third-party libraries), you MUST provide an explicit, recent version number that is compatible with Spring Boot %s. For dependencies managed by Spring Boot, you MUST omit the version so the parent POM can manage it.

Use 'compile' for standard dependencies, 'runtime' for runtime-only, and 'optional' for tools like Lombok. If scope is 'compile', you can omit it.

After the dependency list, you MUST add a separator line containing exactly "---END-DEPS---".
After the separator, you MUST repeat the original requirements text provided below, exactly and without modification.

Example output format (assuming 'existing-dep' is from EXISTING DEPENDENCIES and 'new-dep' is a new requirement):
org.springframework.boot:spring-boot-starter-web
com.example:existing-dep
com.new.feature:new-dep:1.0.0
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0
---END-DEPS---
Feature: User Management API
...
Requirements:
{requirements}
""",
                projectConfig.javaVersion,
                projectConfig.springBootVersion,
                String.join("\n", existingPomDependencies),
                projectConfig.springBootVersion
        ));
        agentPrompts.put(TESTGEN_AGENT_NAME, String.format("""
You are a senior test engineer. Your task is to write high-quality JUnit 5 unit tests to verify that the provided Java code correctly implements the given feature requirements.

**CRITICAL INSTRUCTIONS:**
1.  **Analyze Existing Structure:** Review the list of existing files to understand the current project structure and conventions.
2.  **Generate Code Snippets:**
    - For **new files**, provide the complete content.
    - For **existing files**, you MUST ONLY generate the new code snippet (e.g., a new test method). DO NOT output the entire file.
3.  **Output Format:**
    - For a **NEW file**, use the format with a full path from the project root. Example: `// Create File: src/test/java/com/yourcompany/service/UserServiceTest.java`
    - For **MODIFYING an existing file**, use the format with a full path from the project root. Example: `// Modify File: src/test/java/com/yourcompany/service/UserServiceTest.java`

**STRICT TESTING DIRECTIVES:**
- For ALL test classes, you MUST use ONLY classic Mockito-based unit tests.
- DO NOT use `@Autowired`, `@WebMvcTest`, `@DataMongoTest`, or `@SpringBootTest`.
- All test classes MUST be annotated with `@ExtendWith(MockitoExtension.class)`.
- All test classes SHOULD be annotated with `@MockitoSettings(strictness = Strictness.LENIENT)` to avoid unnecessary stubbing errors.
- All dependencies MUST be mocked with `@Mock`.
- The class under test MUST be instantiated with `@InjectMocks`.
- If you need to test a controller with `MockMvc`, set it up manually in a `@BeforeEach` method using `MockMvcBuilders.standaloneSetup(...)`.

**DO NOT USE THE FOLLOWING ANNOTATIONS IN ANY TEST CLASS:**
- `@Autowired`
- `@WebMvcTest`
- `@DataMongoTest`
- `@SpringBootTest`
- `@MockBean`

**EXISTING PROJECT FILES:**
%s

**FEATURE REQUIREMENTS:**
{requirements}

**CODE TO TEST:**
{code}
""",
                existingFiles
        ));

        final WorkflowResult workflowResult = runMainWorkflow(userInput, srsData.projectConfig, agentPrompts, existingPomDependencies);

        if (workflowResult == null) {
            logger.error("Workflow execution failed. Could not generate project files. Aborting.");
            return;
        }

        generateProjectFiles(gitConfig.repoPath, workflowResult, userInput, changeAnalysis, srsData.projectConfig, featureBranch);

        // --- Quality Gate: Verify the build before committing ---
        String buildResult = verifyProjectBuild(gitConfig.repoPath);

        if (buildResult == null) {
            // --- HAPPY PATH: Build Succeeded ---
            logger.info("\n\n✅✅✅ Build Succeeded! Proceeding to commit and create Pull Request...");

            // --- NEW: Delete target directory before commit ---
            try {
                Path targetDir = Paths.get(gitConfig.repoPath, "target");
                if (Files.exists(targetDir)) {
                    logger.info("Deleting target directory before commit: {}", targetDir);
                    Files.walk(targetDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                }
            } catch (IOException e) {
                logger.warn("Could not delete target directory: {}", e.getMessage());
            }
            // --- END DELETE ---

            // --- NEW: Add target/ to .gitignore to prevent pushing build artifacts ---
            addGitignoreEntry(gitConfig.repoPath, "target/");
            // --- END NEW LOGIC ---

            finalizeAndSubmit(gitConfig, featureBranch, workflowResult.commitMessage);
        } else {
            // --- FAILURE PATH: Build Failed, attempting self-healing ---
            boolean buildSuccess = false;
            for (int i = 0; i < 3; i++) { // Max 3 retries
                logger.error("\n\n❌❌❌ Build Failed on attempt {}. Starting self-healing process...", i + 1);
                String reviewAnalysis = runReviewAgent(buildResult);
               // String faultyFilePath = findFaultyFile(reviewAnalysis, gitConfig.repoPath);

                // --- NEW: Get all source code for the agent to analyze ---
                String allSourceCode = getAllSourceCodeForCorrection(gitConfig.repoPath);
                if (allSourceCode.isEmpty()) {
                    logger.error("Could not find any source code to analyze for self-healing. Aborting.");
                    break;
                }

//                if (faultyFilePath == null) {
//                    logger.error("Could not identify the faulty file from the review agent's analysis. Aborting self-healing.");
//                    break;
//                }

                //                    String faultyFileContent = Files.readString(Paths.get(faultyFilePath));
//                    String correctedCode = runBuildCorrectorAgent(buildResult, reviewAnalysis, faultyFileContent, faultyFilePath);
                String correctedCode = runBuildCorrectorAgent(buildResult, reviewAnalysis, allSourceCode);

                if (correctedCode != null && !correctedCode.isBlank()) {
//                        logger.info("🤖 BuildCorrectorAgent provided a fix. Overwriting file: {}", faultyFilePath);
//                        Files.writeString(Paths.get(faultyFilePath), correctedCode);
                    logger.info("🤖 BuildCorrectorAgent provided a fix. Applying changes...");
                    // The writeClassesToFileSystem can handle create/modify based on the markers
                    writeClassesToFileSystem(correctedCode, gitConfig.repoPath);

                    // Retry the build
                    buildResult = verifyProjectBuild(gitConfig.repoPath);
                    if (buildResult == null) {
                        buildSuccess = true;
                        logger.info("\n\n✅✅✅ Build Succeeded after self-healing! Proceeding to commit...");
                        finalizeAndSubmit(gitConfig, featureBranch, workflowResult.commitMessage);
                        break;
                    }
                } else {
                    logger.error("BuildCorrectorAgent failed to provide a fix. Aborting self-healing.");
                    break;
                }
            }

            if (!buildSuccess) {
                logger.error("\n\n❌❌❌ Self-healing failed. Committing generated code with final failure analysis...");
                String analysis = runReviewAgent(buildResult); // Final analysis
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
            String failedCommitMessage = "fix(ai): [BUILD FAILED] " + workflowResult.commitMessage;
            commitAndPush(gitConfig.repoPath, failedCommitMessage, featureBranch);
            }
        }
    }

    private static void appendContentWithMetadata(Path filePath, String content, String branchName) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(dtf);

            String header = String.format(
                "\n\n---\n**Date:** %s\n**Branch:** %s\n---\n\n",
                timestamp,
                branchName
            );

            String fullContent = header + content + "\n--- END ---\n";

            // Ensure parent directories exist
            Files.createDirectories(filePath.getParent());

            // Create file if it doesn't exist, then append.
            Files.writeString(filePath, fullContent, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("✅ Appended content with metadata to {}", filePath.getFileName());
        } catch (IOException e) {
            logger.error("❌ Failed to append content to {}: {}", filePath.getFileName(), e.getMessage());
        }
    }

    private static String performChangeAnalysis(String repoDir, String newSrs) {
        try {
            Path oldSrsPath = Paths.get(repoDir, AI_STATE_DIR, JIRA_STATE_FILE_NAME);
            String oldSrsContent = "";
            if (Files.exists(oldSrsPath)) {
                logger.info("Found previous Jira issue state file for comparison.");
                oldSrsContent = Files.readString(oldSrsPath);
            } else {
                logger.info("No previous Jira issue state file found. This will be an initial analysis.");
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

    private static String runContextExtractionAgent(String existingFileContent) {
        logger.info("--- 🤖 Starting Context Extraction Agent ---");
        LlmAgent contextAgent = LlmAgent.builder()
            .name(CONTEXT_EXTRACTION_AGENT_NAME)
            .description("Extracts class-level context and conventions from an existing Java file for use in code generation.")
            .instruction("""
You are an expert Java code analyst. Given the full content of a Java class, extract the following information as a structured summary for use in code generation:
- The class name and its type (e.g., Controller, Service, Repository, Entity)
- All class-level annotations (e.g., @RestController, @RequestMapping, @Service)
- The value of any base @RequestMapping or similar annotation
- All static variables/constants (names and values)
- All field declarations (names, types, and annotations)
- The names of injected dependencies (e.g., services, repositories)
- Any naming conventions for objects or references

Output the information as a structured summary, e.g.:
Class: EmployeeController
Type: Controller
Class-level Annotations: @RestController, @RequestMapping("/employees")
Base RequestMapping: /employees
Static Variables: [String API_VERSION = "v1"]
Fields: [private final EmployeeService employeeService]
Injected Dependencies: [employeeService]
Naming Conventions: [employeeService for EmployeeService]

Do not include any code, only the structured summary.
""")
            .model("gemini-2.0-flash")
            .outputKey("context")
            .build();

        final InMemoryRunner runner = new InMemoryRunner(contextAgent);
        final Content userMsg = Content.fromParts(Part.fromText(existingFileContent));

        try {
            Event finalEvent = retryWithBackoff(() -> {
                Session session = runner.sessionService().createSession(runner.appName(), "user-context-extractor").blockingGet();
                return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
            });
            String contextSummary = finalEvent != null ? finalEvent.stringifyContent().trim() : "";
            logger.info("✅ ContextExtractionAgent summary:\n{}", contextSummary);
            logger.info("--- ✅ Finished Context Extraction Agent ---");
            return contextSummary;
        } catch (Exception e) {
            logger.error("❌ The ContextExtractionAgent failed to run. Returning empty context. Error: {}", e.getMessage(), e);
            logger.info("--- ❌ Finished Context Extraction Agent with error ---");
            return "";
        }
    }

    private static String runBuildCorrectorAgent(String buildLog, String reviewAnalysis, String allSourceFiles) {
        logger.info("--- 🤖 Starting Build Corrector Agent ---");
        LlmAgent correctorAgent = LlmAgent.builder()
                .name(BUILD_CORRECTOR_AGENT_NAME)
                .description("Analyzes build failures and corrects the faulty Java code across the entire project.")
                .instruction("""
You are a Senior Software Engineer specializing in debugging and fixing build failures. You will be given a Maven build log, an analysis of the failure, and the full content of ALL source files in the project.

Your task is to identify the root cause of the build failure and provide the corrected code for ALL files that need to be changed to fix the error.

**CRITICAL INSTRUCTIONS:**
1.  **Analyze the `BUILD LOG` and `REVIEW ANALYSIS`** to understand the root cause. The error may be in a different file than where the compiler reports it. For example, a missing method in a Repository will cause a compilation error in a Service that calls it. The fix is to add the method to the Repository.
2.  **Examine ALL `PROJECT SOURCE FILES`** to understand the full context.
3.  **Generate Corrected Code:** For each file that needs to be modified, you MUST provide its full and complete corrected content.
4.  **Output Format:** You MUST format your response as one or more code blocks.
    - For a file that needs to be **REFACTORED**, start the block with `// Refactored File: [full/path/to/file.java]`.
    - For a file that needs to be **CREATED** (less common for a fix, but possible), start the block with `// Create File: [full/path/to/file.java]`.
    - Follow the marker with the complete, corrected code for that file.
5.  **Do not add any other explanation or text.** Your entire response must be only the file markers and their corresponding code blocks.
""")
                .model("gemini-2.0-flash")
                .outputKey("corrected_code")
                .build();

        final InMemoryRunner runner = new InMemoryRunner(correctorAgent);
        final Content userMsg = Content.fromParts(
            Part.fromText("**BUILD LOG:**\n" + buildLog),
            Part.fromText("\n**REVIEW ANALYSIS:**\n" + reviewAnalysis),
            Part.fromText("\n**PROJECT SOURCE FILES:**\n" + allSourceFiles)
        );

        try {
            Event finalEvent = retryWithBackoff(() -> {
                Session session = runner.sessionService().createSession(runner.appName(), "user-build-corrector").blockingGet();
                return runner.runAsync(session.userId(), session.id(), userMsg).blockingLast();
            });
            String response = finalEvent != null ? finalEvent.stringifyContent().trim() : "";
            logger.info("Full raw response from BuildCorrectorAgent:\n---\n{}\n---", response);
            // The response can be directly passed to writeClassesToFileSystem, so we just return it.
            if (!response.isBlank()) {
                logger.info("--- ✅ Finished Build Corrector Agent ---");
                return response;
            } else {
                logger.warn("BuildCorrectorAgent returned an empty response.");
                logger.info("--- ❌ Finished Build Corrector Agent with empty response ---");
                return null;
            }
        } catch (Exception e) {
            logger.error("❌ The BuildCorrectorAgent failed to run. Error: {}", e.getMessage(), e);
            logger.info("--- ❌ Finished Build Corrector Agent with error ---");
            return null;
        }
    }


    private static String getAllSourceCodeForCorrection(String repoPath) {
        StringBuilder allCode = new StringBuilder();
        Path srcRoot = Paths.get(repoPath, "src");
        if (!Files.exists(srcRoot)) {
            logger.warn("Source directory does not exist in {}. Cannot get code for correction.", repoPath);
            return "";
        }
        try {
            Files.walk(srcRoot)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            // Use a relative path from the repo root for the marker
                            String relativePath = Paths.get(repoPath).relativize(path).toString().replace('\\', '/');
                            allCode.append(String.format("--- FILE START: %s ---\n", relativePath));
                            allCode.append(content).append("\n");
                            allCode.append(String.format("--- FILE END: %s ---\n\n", relativePath));
                        } catch (IOException e) {
                            logger.warn("Could not read source file {}: {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            logger.error("Error walking source tree for self-healing: {}", e.getMessage());
        }
        return allCode.toString();
    }


    private static void addGitignoreEntry(String repoPath, String entry) {
        Path gitignorePath = Paths.get(repoPath, ".gitignore");
        try {
            if (!Files.exists(gitignorePath)) {
                Files.createFile(gitignorePath);
                logger.info("✅ Created .gitignore at: {}", gitignorePath);
            }
            List<String> lines = Files.readAllLines(gitignorePath);
            if (!lines.contains(entry)) {
                Files.write(gitignorePath, (entry + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                logger.info("✅ Added '{}' to .gitignore in: {}", entry, gitignorePath);
            } else {
                logger.info("Skipped '{}' as it already exists in .gitignore.", entry);
            }
        } catch (IOException e) {
            logger.error("❌ Failed to add '{}' to .gitignore: {}", entry, e.getMessage());
        }
    }
}