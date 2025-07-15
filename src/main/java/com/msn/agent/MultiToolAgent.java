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
import io.reactivex.rxjava3.core.Flowable;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

public class MultiToolAgent {

    private static String USER_ID = "student";
    private static String NAME = "multi_tool_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {

        return LlmAgent.builder()
                .name(NAME)
                .model("gemini-2.0-flash")
                .description("Agent to answer questions about the time and weather in a city.")
                .instruction(
                        "You are a helpful agent who can answer user questions about the time and weather"
                                + " in a city.")
                .tools(
                        FunctionTool.create(MultiToolAgent.class, "getCurrentTime"),
                        FunctionTool.create(MultiToolAgent.class, "getWeather"),
                        FunctionTool.create(MultiToolAgent.class, "handleGreeting"),
                        FunctionTool.create(MultiToolAgent.class, "processLoginInfo"),
                        FunctionTool.create(MultiToolAgent.class, "generateJavaCode")
                )
                .build();
    }

    public static Map<String, String> getCurrentTime(
            @Schema(description = "The name of the city for which to retrieve the current time")
            String city) {
        String normalizedCity =
                Normalizer.normalize(city, Normalizer.Form.NFD)
                        .trim()
                        .toLowerCase()
                        .replaceAll("(\\p{IsM}+|\\p{IsP}+)", "")
                        .replaceAll("\\s+", "_");

        return ZoneId.getAvailableZoneIds().stream()
                .filter(zid -> zid.toLowerCase().endsWith("/" + normalizedCity))
                .findFirst()
                .map(
                        zid ->
                                Map.of(
                                        "status",
                                        "success",
                                        "report",
                                        "The current time in "
                                                + city
                                                + " is "
                                                + ZonedDateTime.now(ZoneId.of(zid))
                                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                                                + "."))
                .orElse(
                        Map.of(
                                "status",
                                "error",
                                "report",
                                "Sorry, I don't have timezone information for " + city + "."));
    }

    public static Map<String, String> getWeather(
            @Schema(description = "The name of the city for which to retrieve the weather report")
            String city) {
        if (city.toLowerCase().equals("kolkata")) {
            return Map.of(
                    "status",
                    "success",
                    "report",
                    "The weather in "+city+" is sunny with a temperature of 25 degrees Celsius (77 degrees"
                            + " Fahrenheit).");

        } else {
            return Map.of(
                    "status", "error", "report", "Weather information for " + city + " is not available.");
        }
    }



    /**
     * A tool to handle greetings from the user.
     * @param name The name of the user, if they provided it. Can be null.
     * @return A friendly greeting message.
     */
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


    /**
     * A tool to acknowledge a user's login and extract their username.
     * @param username The username the user provides.
     * @return A confirmation message.
     */
    public static Map<String, String> processLoginInfo(
            @Schema(description = "The username provided by the user after a login action.")
            String username) {

        if (username == null || username.trim().isEmpty()) {
            return Map.of(
                    "status", "error",
                    "report", "It looks like you mentioned logging in, but I didn't catch a username. Could you please provide it?"
            );
        }

        // In a real application, you might use this username to update a session state
        // or fetch user-specific data. For now, we'll just confirm it.
        String report = "Thanks for logging in, " + username + "! I've noted that. How can I help you now?";

        return Map.of(
                "status", "success",
                "report", report
        );
    }

    public static Map<String, String> generateJavaCode(
            @Schema(
                    description =
                            "A detailed description of the Java code to be generated, e.g., 'a function to"
                                    + " sort a list of strings alphabetically'.")
            String description) {
        try(Client client = Client.builder()
                .apiKey(System.getenv("GOOGLE_API_KEY"))
                .build()){
            String codePrompt =
                    "You are an expert Java programmer. Generate a complete, well-formatted, and"
                            + " documented Java code snippet for the following request. The code should be"
                            + " production-quality and include necessary imports. Wrap the code in a"
                            + " markdown block.\n\nREQUEST: "
                            + description;



            GenerateContentConfig generationConfig = GenerateContentConfig.builder()
                    .temperature(0.2f)
                    .maxOutputTokens(8192)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "models/gemini-2.0-flash",
                    codePrompt,
                    generationConfig
            );
            String generatedCode = response.text();
            return Map.of("status", "success", "report", "\n" + generatedCode);
        }catch (IllegalArgumentException e) {
            return Map.of(
                    "status", "error",
                    "report", "❌ Invalid input: " + e.getMessage()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "report", "❌ Failed to generate code: " + e.getMessage()
            );
        }

    }


    public static void main(String[] args) throws Exception {
        System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "FALSE");
        System.setProperty("GOOGLE_API_KEY", "AIzaSyBrv26CX1xFAWn3zZCtcuziigj6rHf3inY");

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
                }else if("exit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

                System.out.print("\nAgent > ");
                events.blockingForEach(event -> System.out.println(event.stringifyContent()));
            }
        }
    }
}