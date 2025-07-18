package com.msn.autonomous;
// Google ADK Java Console App: Full SDLC Flow with Spring Boot Microservice Code Generation and File Structure Output

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdkSdlcWorkflow_2 {

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

Requirements:
{requirements}

Wrap each class in proper Java class syntax and include a comment at the top indicating the file path, for example:
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

    public static void writeClassesToFileSystem(String combinedOutput) {
        Pattern pattern = Pattern.compile("// File: ([^\n]+)\\n(.*?)(?=\\n// File: |\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(combinedOutput);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                System.out.println("✅ Created: " + filePath);
            } catch (IOException e) {
                System.err.println("❌ Failed to write: " + filePath + " - " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SequentialAgent workflow = buildWorkflow();
        InMemoryRunner runner = new InMemoryRunner(workflow);
        Session session = runner.sessionService().createSession(runner.appName(), "user").blockingGet();

        Scanner s = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("\nEnter a Spring Boot microservice feature requirement:");
        String userInput = s.nextLine();

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

        writeClassesToFileSystem(fullOutput.toString());
    }
}
