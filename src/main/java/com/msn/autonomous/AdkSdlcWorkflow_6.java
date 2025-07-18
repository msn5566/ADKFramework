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
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdkSdlcWorkflow_6 {

    public static SequentialAgent buildWorkflow() {
        LlmAgent req = LlmAgent.builder()
                .name("RequirementsAgent")
                .description("Extracts structured functional requirements from a feature prompt.")
                .instruction("""
Extract structured requirements from the following input.
Format:
Feature:
Input:
Output:
Constraints:
Logic:
""")
                .model("gemini-2.0-flash")
                .outputKey("requirements")
                .build();

        LlmAgent code = LlmAgent.builder()
                .name("CodeGenAgent")
                .description("Generates a complete Spring Boot microservice skeleton based on structured requirements.")
                .instruction("""
Given the structured requirements below, generate a full Spring Boot microservice skeleton. Include:
- Controller, Service, Repository, and Entity classes
- REST endpoints with proper annotations
- Validation annotations on fields
- PostgreSQL integration using Spring Data JPA
- Lombok annotations
- application.yml configuration
- README.md
- Dockerfile and docker-compose.yml

Requirements:
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
                .subAgents(req, code, test)
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

    public static void zipProject(String baseDir, String zipFileName) {
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path basePath = Paths.get(baseDir);
            Files.walk(basePath).filter(Files::isRegularFile).forEach(path -> {
                try {
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

    public static void initGitRepo(String baseDir) {
        try {
            String uniqueBranch = "feature/generated-" + System.currentTimeMillis();

            ProcessBuilder init = new ProcessBuilder("git", "init");
            init.directory(new File(baseDir));
            init.inheritIO().start().waitFor();

            ProcessBuilder checkout = new ProcessBuilder("git", "checkout", "-b", uniqueBranch);
            checkout.directory(new File(baseDir));
            checkout.inheritIO().start().waitFor();

            ProcessBuilder addAll = new ProcessBuilder("git", "add", ".");
            addAll.directory(new File(baseDir));
            addAll.inheritIO().start().waitFor();

            ProcessBuilder commit = new ProcessBuilder("git", "commit", "-m", "Initial commit on " + uniqueBranch);
            commit.directory(new File(baseDir));
            commit.inheritIO().start().waitFor();

            Files.writeString(Paths.get(baseDir, ".generated_branch"), uniqueBranch);

            // Append branch info to README
            Path readme = Paths.get(baseDir, "README.md");
            if (Files.exists(readme)) {
                Files.writeString(readme, "\n\nGenerated on branch: " + uniqueBranch, StandardOpenOption.APPEND);
            }

            System.out.println("✅ Git repo initialized on '" + uniqueBranch + "' with initial commit.");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Git repo: " + e.getMessage());
        }
    }

    public static void pushToGitHub(String baseDir, String repoUrl) {
        try {
            String branchName = Files.readString(Paths.get(baseDir, ".generated_branch")).trim();

            ProcessBuilder remoteAdd = new ProcessBuilder("git", "remote", "add", "origin", repoUrl);
            remoteAdd.directory(new File(baseDir));
            remoteAdd.inheritIO().start().waitFor();

            ProcessBuilder push = new ProcessBuilder("git", "push", "-u", "origin", branchName);
            push.directory(new File(baseDir));
            push.inheritIO().start().waitFor();

            System.out.println("🚀 Project pushed to GitHub on '" + branchName + "': " + repoUrl);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(repoUrl.replace(".git", "/tree/" + branchName)));
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to push to GitHub: " + e.getMessage());
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

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("\nEnter the full path to your SRS document (e.g., /home/user/project/specs/srs.txt):");
        String srsPath = s.nextLine().trim();
        String userInput;
        try {
            userInput = Files.readString(Paths.get(srsPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("❌ Failed to read SRS file: " + e.getMessage());
            return;
        }

        System.out.println("\nEnter the output directory (absolute path):");
        String outputDir = s.nextLine().trim();

        SequentialAgent workflow = buildWorkflow();
        InMemoryRunner runner = new InMemoryRunner(workflow);
        Session session = runner.sessionService().createSession(runner.appName(), "user").blockingGet();

        Content userMsg = Content.fromParts(Part.fromText(userInput));
        Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg);

        StringBuilder fullOutput = new StringBuilder();

        System.out.println("\n--- Agent Responses ---");
        events.blockingForEach(ev -> {
            String response = ev.stringifyContent();
            if (!response.isBlank()) {
                System.out.println("[" + ev.author() + "]\n" + response + "\n");
                fullOutput.append(response).append("\n\n");
            }
        });

        try (FileWriter fw = new FileWriter("springboot_microservice_output.txt")) {
            fw.write(fullOutput.toString());
            System.out.println("\n📄 Combined output saved to springboot_microservice_output.txt");
        } catch (IOException e) {
            System.err.println("❌ Failed to save combined output: " + e.getMessage());
        }

        writeClassesToFileSystem(fullOutput.toString(), outputDir);
        addGithubActionsCiConfig(outputDir);
        initGitRepo(outputDir);
        zipProject(outputDir, "springboot_microservice_project.zip");

        System.out.println("\nEnter GitHub repo URL to push to (or press Enter to skip):");
        String repoUrl = s.nextLine().trim();
        if (!repoUrl.isBlank()) {
            pushToGitHub(outputDir, repoUrl);
        }
    }
}
