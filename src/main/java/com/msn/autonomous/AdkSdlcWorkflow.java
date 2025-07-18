package com.msn.autonomous;

// Google ADK Java Console App: Full SDLC Flow with Logging and File Output

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AdkSdlcWorkflow {

    public static SequentialAgent buildWorkflow() {
        LlmAgent req = LlmAgent.builder()
                .name("RequirementsAgent")
                .instruction("""
Extract structured requirements.
Format:
Feature:
Input:
Output:
Constraints:
Logic:
""")
                .model("gemini-2.0-flash")
                .outputKey("requirements")
                .description("Extracts structured functional requirements from a natural language prompt.")
                .build();

        LlmAgent code = LlmAgent.builder()
                .name("CodeGenAgent")
                .instruction("Generate a Java method from:\n{requirements}")
                .description("Generates Java code based on structured requirements.")
                .model("gemini-2.0-flash")
                .outputKey("code")
                .build();

        LlmAgent test = LlmAgent.builder()
                .name("TestGenAgent")
                .instruction("Write a JUnit 5 test for:\n{code}")
                .description("Generates JUnit test cases for the given Java method.")
                .model("gemini-2.0-flash")
                .outputKey("test")
                .build();

        return SequentialAgent.builder()
                .name("FullSDLCWorkflow")
                .subAgents(req, code, test)
                .build();
    }

    public static void main(String[] args) {
        SequentialAgent workflow = buildWorkflow();
        InMemoryRunner runner = new InMemoryRunner(workflow);
        Session session = runner.sessionService().createSession(runner.appName(), "user").blockingGet();

        Scanner s = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.print("Enter feature description: ");
        String userInput = s.nextLine();

        Content userMsg = Content.fromParts(Part.fromText(userInput));
        Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg);

        StringBuilder fullOutput = new StringBuilder();

        System.out.println("\n--- Agent Responses ---");
        events.blockingForEach(ev -> {
            String response = ev.stringifyContent();
            if (!response.isBlank()) {
                System.out.println("[" + ev.author() + "]\n" + response + "\n");
                fullOutput.append("[" + ev.author() + "]\n").append(response).append("\n\n");
            }
        });

        try (FileWriter fw = new FileWriter("sdlc_output_2.txt")) {
            fw.write(fullOutput.toString());
            System.out.println("\n✅ Output saved to sdlc_output_2.txt");
        } catch (IOException e) {
            System.err.println("❌ Failed to save output: " + e.getMessage());
        }
    }
}

