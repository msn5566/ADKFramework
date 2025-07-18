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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdkSdlcWorkflow_7_latest {

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

        LlmAgent deps = LlmAgent.builder()
                .name("DependencyAgent")
                .description("Determines required dependency features from the requirements.")
                .instruction("""
Based on the following requirements, identify the necessary Maven dependencies.
Provide ONLY a list of `groupId:artifactId:scope` tuples, one per line.
Use 'compile' for standard dependencies, 'runtime' for runtime-only, and 'optional' for tools like Lombok.
If scope is 'compile', you can omit it.

Example:
org.springframework.boot:spring-boot-starter-web
org.springframework.boot:spring-boot-starter-data-jpa
org.postgresql:postgresql:runtime
org.projectlombok:lombok:optional

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
Given the structured requirements below, generate the core Java source code for a Spring Boot microservice. Include:
- Controller, Service, Repository, and Entity classes
- REST endpoints with proper annotations
- Validation annotations on fields
- PostgreSQL integration using Spring Data JPA
- Lombok annotations

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
            File workingDir = new File(baseDir);

            runCommand(workingDir, "git", "init");
            runCommand(workingDir, "git", "checkout", "-b", uniqueBranch);
            runCommand(workingDir, "git", "add", ".");
            runCommand(workingDir, "git", "commit", "-m", "Initial commit from AI agent on " + uniqueBranch);

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
            File workingDir = new File(baseDir);

            // This robustly handles adding the remote, even if it already exists.
            try {
                runCommand(workingDir, "git", "remote", "add", "origin", repoUrl);
            } catch (IOException e) {
                System.out.println("⚠️  Remote 'origin' may already exist. Setting URL instead.");
                runCommand(workingDir, "git", "remote", "set-url", "origin", repoUrl);
            }
            runCommand(workingDir, "git", "push", "-u", "origin", branchName);

            System.out.println("🚀 Project pushed to GitHub on '" + branchName + "': " + repoUrl);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(repoUrl.replace(".git", "/tree/" + branchName)));
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to push to GitHub: " + e.getMessage());
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

    public static void addReadme(String baseDir, String requirements, List<String> dependencies) {
        StringBuilder summarySection = new StringBuilder();
        summarySection.append("## 📝 Project Summary\n\n");
        summarySection.append("This microservice was automatically generated based on the following high-level requirements:\n\n");
        summarySection.append("```\n").append(requirements).append("\n```\n\n");

        if (!dependencies.isEmpty()) {
            summarySection.append("### Core Dependencies\n\n");
            summarySection.append("The following core dependencies were automatically included to support these requirements:\n\n");
            for (String dep : dependencies) {
                summarySection.append("- `").append(dep.replace(":", "`:`")).append("`\n");
            }
            summarySection.append("\n");
        }

        String content = String.format("""
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
            """, summarySection.toString());

        try {
            Files.writeString(Paths.get(baseDir, "README.md"), content.strip());
            System.out.println("✅ Created: README.md");
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
        final List<String> dependencyList = new ArrayList<>();
        final StringBuilder requirementsSummary = new StringBuilder();

        System.out.println("\n--- Agent Responses ---");
        events.blockingForEach(ev -> {
            String response = ev.stringifyContent();
            if (!response.isBlank()) {
                System.out.println("[" + ev.author() + "]\n" + response + "\n");
                // Capture dependency output specifically
                if ("DependencyAgent".equals(ev.author())) {
                    // Split the response by newlines to get individual dependency strings
                    dependencyList.addAll(java.util.Arrays.asList(response.trim().split("\\s*\\r?\\n\\s*")));
                }
                // Capture requirements output
                if ("RequirementsAgent".equals(ev.author())) {
                    requirementsSummary.append(response.trim());
                }
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
        
        // Use the dynamic output from the DependencyAgent, with a sensible default if it's empty.
        if (dependencyList.isEmpty()) {
            System.out.println("⚠️ DependencyAgent did not return any dependencies. Falling back to default pom.xml.");
            List<String> defaultDeps = List.of(
                "org.springframework.boot:spring-boot-starter-web",
                "org.springframework.boot:spring-boot-starter-data-jpa",
                "org.postgresql:postgresql:runtime",
                "org.projectlombok:lombok:optional"
            );
            addPomXml(outputDir, defaultDeps);
        } else {
            addPomXml(outputDir, dependencyList);
        }
        addReadme(outputDir, requirementsSummary.toString(), dependencyList);
        addApplicationYml(outputDir);
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
