package com.msn.tools;

import com.google.adk.tools.Annotations;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.msn.agent.MultiToolAgent_2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SpringBootTool {

    /**
     *
     *
     * @param description A detailed natural language description of the Java code to be generated.
     * @return A map containing the generated Java code.
     */
    public static Map<String, String> generateSpringBootProjectCode(
            @Annotations.Schema(
                    description = """
                        Generates a complete, multi-file Spring Boot project from a natural language description.
                        Use this tool for requests like "create a Spring Boot REST API for a to-do list", "build a microservice for user management", or "generate a Spring Boot project".
                        The tool creates all necessary files, including controllers, services, repositories, pom.xml, and JUnit tests.
                        """)
            String description) {
        try{
//            String codePrompt =
//                    "You are an expert Java programmer. Generate a complete, well-formatted, and"
//                            + " documented Java code snippet for the following request. The code should be"
//                            + " production-quality and include necessary imports. Wrap the code in a"
//                            + " markdown block.\n\nREQUEST: "
//                            + description;
            // This improved prompt structure clearly defines the persona, task, user request, and output format.
            String codePrompt = String.format("""
                **Persona:**
                You are an expert Java developer specializing in creating production-ready Spring Boot applications.

                **Core Task:**
                Based on the user's request below, generate a complete, well-structured, and documented Spring Boot project or component.

                **User Request:**
                "%s"

                **Mandatory Guidelines & Best Practices:**

                1. **Code Style and Structure:**
                - Write clean, efficient Java code following Spring Boot best practices.
                - Structure the application logically: `*.controller`, `*.service`, `*.repository`, `*.model`, etc.
                - Use Java 17+ features where appropriate.
                - Adhere to standard Java naming conventions and use constructor injection.

                2. **Project Generation:**
                - You MUST include all the required files to run a complete Spring Boot project.
                - This includes, at a minimum, a `pom.xml`, the main application class (`@SpringBootApplication`), and an `application.properties` file.
                - **Crucially, only generate the business logic components (controllers, services, entities) that the user asks for.** If the request is a simple REST API, do not generate JPA entities unless specified.

                3. **Testing:**
                - For every generated component (Controller, Service), you MUST provide a corresponding JUnit 5 test class using Mockito.

                **Output Format:**
                - You MUST NOT include any conversational text or explanations. Your response must be ONLY code.
                - Each file MUST be preceded by a special marker comment with its full file path.
                - The marker format is ALWAYS: `// --- File: [full_path_to_file] ---`
                - After the marker, provide the file content inside a markdown code block.
                - Example:
                  `// --- File: src/main/java/com/example/demo/controller/MyController.java ---`
                  ```java
                  package com.example.demo.controller;
                  // ... rest of the file content
                  ```
                  `// --- File: pom.xml ---`
                  ```xml
                  <project>
                      <!-- pom.xml content -->
                  </project>
                  ```
                """, description);

            GenerateContentConfig generationConfig = GenerateContentConfig.builder()
                    .temperature(0.2f)
                    .maxOutputTokens(8192)
                    .build();

            GenerateContentResponse response = MultiToolAgent_2.client.models.generateContent(
                    "models/gemini-2.0-flash",
                    codePrompt,
                    generationConfig
            );
            String rawResponse = response.text();

            // Parse the structured response into a map of file paths to file content
            Map<String, String> projectFiles = parseProjectFiles(rawResponse);

            // Build a human-readable, pretty-printed report from the parsed files.
            String lineSeparator = System.lineSeparator();
            StringBuilder reportBuilder = new StringBuilder("✅ Successfully generated the following project files:" + lineSeparator + lineSeparator);

            for (Map.Entry<String, String> fileEntry : projectFiles.entrySet()) {
                String filePath = fileEntry.getKey();
                String fileContent = fileEntry.getValue();

                // Determine language for markdown syntax highlighting
                String language = "";
                if (filePath.endsWith(".java")) {
                    language = "java";
                } else if (filePath.endsWith(".xml")) {
                    language = "xml";
                } else if (filePath.endsWith(".properties")) {
                    language = "properties";
                }

                reportBuilder.append(String.format("// --- File: %s ---", filePath))
                        .append(lineSeparator)
                        .append(String.format("```%s", language))
                        .append(lineSeparator)
                        .append(fileContent)
                        .append(lineSeparator)
                        .append("```")
                        .append(lineSeparator)
                        .append(lineSeparator); // Add extra space for separation
            }

            return Map.of("status", "success", "report", reportBuilder.toString());
        }catch (IllegalArgumentException e) {
            return Map.of(
                    "status", "error",
                    "report", "Invalid input: " + e.getMessage()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "report", "Failed to generate code: " + e.getMessage()
            );
        }

    }

    private static Map<String, String> parseProjectFiles(String rawResponse) {
        Map<String, String> files = new LinkedHashMap<>();
        // Regex to find the file path marker and the content in the following markdown block.
        // It uses DOTALL to allow the content to span multiple lines.
        final Pattern pattern =
                Pattern.compile(
                        "// --- File: (.*?) ---\\s*```(?:[a-zA-Z]*)?\\n(.*?)\\s*```", Pattern.DOTALL);
        var matcher = pattern.matcher(rawResponse);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            // The second group captures the content inside the markdown block.
            String fileContent = matcher.group(2).trim();
            files.put(filePath, fileContent);
        }
        return files;
    }
}
