package com.msn.tools;

import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.msn.agent.MultiToolAgent_2;

import java.util.Map;

public class JavaCodeTool {

    public static Map<String, String> generateJavaCode(

            String description) {
        try{
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

            GenerateContentResponse response = MultiToolAgent_2.client.models.generateContent(
                    "models/gemini-2.0-flash",
                    codePrompt,
                    generationConfig
            );
            String generatedCode = response.text();
            return Map.of("status", "success", "report", "\n" + generatedCode);
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
}
