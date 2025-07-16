package com.msn.tools;

import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.msn.agent.MultiToolAgent_2;
import com.google.adk.tools.Annotations.Schema;

import java.util.Map;

public class JavaJUNIT5TestCaseTool {

    public static Map<String, String> generateTestCase(@Schema(
            description =
                    "Use this tool to generate ONLY JUnit 5 test cases. The user should provide either the"
                            + " Java code itself or a description of the code they want to test.")
                                                       String javaCode) {
        try{
            String prompt = """
                - You are a Expert Java QA engineer. For each provided Java source class, generate a corresponding JUnit 5 test class using Mockito for mocking and Spring Boot test annotations where appropriate.
                
                - Add the correct class-level annotation: use @SpringBootTest, @WebMvcTest, or @ExtendWith(MockitoExtension.class) depending on the type (e.g., service, controller, etc.).
                - Include method-level annotations such as @Test, @BeforeEach.
                - For each public method, write one or more unit tests covering:
                  - Normal behavior
                  - Edge cases
                  - Exception scenarios
                - Use @Mock and @InjectMocks or constructor injection to set up dependencies.
                - Follow best practices for test naming, structure, assertions, and mocking.
                - Ensure the test class mirrors the structure and naming of the original class.
                - Add minimal JavaDoc or comments to improve clarity.
                
                Output only valid Java code. Do not include explanations or markdown. Each test class should be self-contained.
                
                METHOD:
                %s
                """.formatted(javaCode);
//            String prompt = """
//                You are a Expert Java QA engineer.
//                Given the following Java method, write a proper JUnit 5 test class for it.
//                Include import statements and wrap everything in a markdown block.
//                Include required annotation in class level and method level also.
//                Generate that many
//
//
//                METHOD:
//                %s
//                """.formatted(javaCode);

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.2f)
                    .maxOutputTokens(2048)
                    .build();

            GenerateContentResponse response = MultiToolAgent_2.client.models.generateContent(
                    "models/gemini-2.0-flash", prompt, config);

            String result = response.text();
           // return (result == null || result.isBlank()) ? "Test generation failed." : result;
            return Map.of("status", "success", "report", "\n" + result);

        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "report", "Error generating test cases: " + e.getMessage()
        );
    }
    }
}
