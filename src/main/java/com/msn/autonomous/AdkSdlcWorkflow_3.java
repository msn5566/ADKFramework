package com.msn.autonomous;

// Google ADK Java Console App: Full SDLC Flow with Spring Boot Microservice Code Generation, File Output, Zip Packaging, and Git Init

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdkSdlcWorkflow_3 {

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
        Pattern pattern = Pattern.compile("// File: ([^\n]+)\n(.*?)(?=\n// File: |\\z)", Pattern.DOTALL);
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
            ProcessBuilder pb = new ProcessBuilder("git", "init");
            pb.directory(new File(baseDir));
            pb.inheritIO().start().waitFor();

            ProcessBuilder addAll = new ProcessBuilder("git", "add", ".");
            addAll.directory(new File(baseDir));
            addAll.inheritIO().start().waitFor();

            ProcessBuilder commit = new ProcessBuilder("git", "commit", "-m", "Initial commit");
            commit.directory(new File(baseDir));
            commit.inheritIO().start().waitFor();

            System.out.println("✅ Git repository initialized and first commit created.");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Git repo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("\nEnter a Spring Boot microservice feature requirement:");
        String userInput = s.nextLine();

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
        initGitRepo(outputDir);
        zipProject(outputDir, "springboot_microservice_project.zip");
    }
}

