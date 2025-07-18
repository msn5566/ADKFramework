package com.msn.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.msn.tools.JavaCodeTool;
import com.msn.tools.JavaJUNIT5TestCaseTool;
import com.msn.tools.SpringBootTool;
import io.reactivex.rxjava3.core.Flowable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;


public class MultiToolAgent_2 {

    private static final String USER_ID = "student";
    private static final String NAME = "multi_tool_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static final Client client = Client.builder()
            .apiKey(System.getenv("GOOGLE_API_KEY"))
            .build();

    public static BaseAgent initAgent() {
        // Agent initialization remains the same
        return LlmAgent.builder()
                .name(NAME)
                .model("gemini-2.0-flash") // Switched to a more recent model
                .description("Agent to answer questions, generate Java code, and handle user interactions.")
                .instruction(
                        "You are a helpful and friendly agent. Your capabilities include: "
                                + "1. Answering questions about time and weather. "
                                + "2. Greeting users and acknowledging their login. "
                                + "3. Generating Java code snippets. "
                                + "4. Generating JUnit test cases for Java code. "
                                + "5. Generating both code and tests together. "
                                + "Carefully select the correct tool based on the user's explicit request. "
                                + "If the user asks for 'code', use the 'generateJavaCode' tool. "
                                + "If they ask for 'tests', use the 'generateTestCasesOnly' tool. "
                                + "If they ask for 'both' or 'code and tests', use 'generateCodeAndTests'.")
                .tools(
                        // Conversational Tools
                        FunctionTool.create(MultiToolAgent_2.class, "handleGreeting"),
                        FunctionTool.create(MultiToolAgent_2.class, "processLoginInfo"),

                        // Granular Code Generation Tools
                        FunctionTool.create(JavaCodeTool.class, "generateJavaCode"),
                        FunctionTool.create(JavaJUNIT5TestCaseTool.class, "generateTestCase"),
                        FunctionTool.create(SpringBootTool.class, "generateSpringBootProjectCode")
                )
                .build();
    }


    public static Map<String, String> handleGreeting(
            @Schema(description = "The name of the user. Use this to personalize the greeting.")
            String name) {

        String greeting;
        if (name != null && !name.isEmpty()) {
            greeting = "Hello, " + name + "! It's nice to meet you. How can I help you today?";
        } else {
            greeting = "Hello! How can I assist you?";
        }

        return Map.of(
                "status", "success",
                "report", greeting
        );
    }

    public static Map<String, String> processLoginInfo(
            @Schema(description = "The username provided by the user after a login action.")
            String username) {

        if (username == null || username.trim().isEmpty()) {
            return Map.of(
                    "status", "error",
                    "report", "It looks like you mentioned logging in, but I didn't catch a username. Could you please provide it?"
            );
        }
        String report = "Thanks for logging in, " + username + "! I've noted that. How can I help you now?";

        return Map.of(
                "status", "success",
                "report", report
        );
    }
    /**
     * The "Project Manager" Tool.
     * This public tool orchestrates the multi-agent workflow. It calls the specialist
     * methods in the correct order and assembles the final report.
     */
//    public static Map<String, String> generateCodeAndTests(
//            @Schema(
//                    description =
//                            "Use this tool ONLY when the user explicitly asks for BOTH Java code AND its"
//                                    + " corresponding test cases in the same request.")
//            String description) {
//
//        // 1. Delegate to the CodeGenerator specialist
//        Map<String, String> codeResult = generateJavaCode(description);
//        if (!"success".equals(codeResult.get("status"))) {
//            return codeResult; // Return early if code generation failed
//        }
//        String generatedCode = codeResult.get("report");
//
//        // 2. Delegate to the TestGenerator specialist
//        String generatedTests = generateTestCase(generatedCode);
//
//        // 3. Assemble the final report for the user
//        String finalReport = new StringBuilder()
//                .append("✅ Here is the code and JUnit 5 test case you requested:\n")
//                .append("\n--- Generated Code ---\n")
//                .append(generatedCode)
//                .append("\n\n--- Generated Test Case ---\n")
//                .append(generatedTests)
//                .toString();
//
//        return Map.of("status", "success", "report", finalReport);
//    }


    public static void main(String[] args) throws Exception {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

        Session session =
                runner
                        .sessionService()
                        .createSession(NAME, USER_ID)
                        .blockingGet();

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();

                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

                System.out.print("\nAgent > ");
                // Using a lambda with a block to handle potential multiline outputs better
                events.blockingForEach(event -> {
                    System.out.print(event.stringifyContent());
                });
                //System.out.println(); // Add a newline for cleaner separation
            }
        }
    }
}
