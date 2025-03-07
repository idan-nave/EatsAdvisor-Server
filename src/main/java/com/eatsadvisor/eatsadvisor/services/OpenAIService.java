package com.eatsadvisor.eatsadvisor.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.Base64;

@Service
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl = "https://api.openai.com/v1/chat/completions";

    public OpenAIService(@Value("${openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Extracts text from an image using OpenAI's GPT-4 Vision API
     * @param file Uploaded menu image
     * @return Extracted menu text
     */
    public String extractTextFromImage(MultipartFile file) {
        try {
            // Convert image to Base64
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Create request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4-vision-preview");

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "Extract all menu items with their descriptions and prices from this image. Format the output as a structured list.");

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");

            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);

            imageContent.put("image_url", imageUrl);

            content.add(textContent);
            content.add(imageContent);

            message.put("content", content);
            messages.add(message);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);

            // Make API request
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // Extract and return the response text
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody == null) {
                    return "Failed to extract text from image: Response body is null";
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                
                if (choices == null || choices.isEmpty()) {
                    return "Failed to extract text from image: Choices are empty";
                }

                Map<String, Object> choice = choices.get(0);
                
                if (choice == null) {
                    return "Failed to extract text from image: Choice is null";
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
                
                if (messageResponse == null || messageResponse.get("content") == null) {
                    return "Failed to extract text from image: Message content is null";
                }

                return (String) messageResponse.get("content");
            } else {
                return "Failed to extract text from image: Status code is not OK";
            }
        } catch (IOException e) {
            return "Error processing image: " + e.getMessage();
        } catch (Exception e) {
            return "Error calling OpenAI API: " + e.getMessage();
        }
    }

    /**
     * Generates personalized food recommendations based on menu text and user preferences
     * @param menuText The menu text extracted from image
     * @param userPreferences Map containing user preferences (allergies, flavors, constraints)
     * @return Map containing recommended dishes with explanations
     */
    public Map<String, Object> getRecommendations(String menuText, Map<String, Object> userPreferences) {
        try {
            // Create request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");

            List<Map<String, Object>> messages = new ArrayList<>();

            // System message to set the context
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful AI food recommendation assistant. Your task is to analyze a menu and provide personalized recommendations based on user preferences, allergies, and dietary constraints.");
            messages.add(systemMessage);

            // User message with menu and preferences
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");

            StringBuilder prompt = new StringBuilder();
            prompt.append("Here is a menu:\\n\\n").append(menuText).append("\\n\\n");
            prompt.append("Here are my preferences:\\n");

            // Add allergies
            if (userPreferences.containsKey("allergies")) {
                @SuppressWarnings("unchecked")
				List<String> allergies = (List<String>) userPreferences.get("allergies");
                if (allergies != null && !allergies.isEmpty()) {
                    prompt.append("Allergies: ").append(String.join(", ", allergies)).append("\\n");
                }
            }

            // Add flavor preferences
            if (userPreferences.containsKey("flavorPreferences")) {
                @SuppressWarnings("unchecked")
				Map<String, Integer> flavorPreferences = (Map<String, Integer>) userPreferences.get("flavorPreferences");
                if (flavorPreferences != null && !flavorPreferences.isEmpty()) {
                    prompt.append("Flavor Preferences (1-10 scale):\\n");
                    for (Map.Entry<String, Integer> entry : flavorPreferences.entrySet()) {
                        prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\\n");
                    }
                }
            }

            // Add dietary constraints
            if (userPreferences.containsKey("dietaryConstraints")) {
                @SuppressWarnings("unchecked")
				List<String> constraints = (List<String>) userPreferences.get("dietaryConstraints");
                if (constraints != null && !constraints.isEmpty()) {
                    prompt.append("Dietary Constraints: ").append(String.join(", ", constraints)).append("\\n");
                }
            }

            // Add special preferences
            if (userPreferences.containsKey("specialPreferences")) {
                @SuppressWarnings("unchecked")
				List<String> specialPrefs = (List<String>) userPreferences.get("specialPreferences");
                if (specialPrefs != null && !specialPrefs.isEmpty()) {
                    prompt.append("Special Preferences:\\n");
                    for (String pref : specialPrefs) {
                        prompt.append("- ").append(pref).append("\\n");
                    }
                }
            }

            // Add dish history
            if (userPreferences.containsKey("dishHistory")) {
                @SuppressWarnings("unchecked")
				Map<String, Integer> dishHistory = (Map<String, Integer>) userPreferences.get("dishHistory");
                if (dishHistory != null && !dishHistory.isEmpty()) {
                    prompt.append("Previous Dishes I've Rated:\\n");
                    for (Map.Entry<String, Integer> entry : dishHistory.entrySet()) {
                        prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("/5\\n");
                    }
                }
            }

            prompt.append("\\nBased on this information, please recommend 3-5 dishes from the menu that I might enjoy. For each recommendation, explain why you think I would like it based on my preferences. Format your response as a JSON object with the following structure:\\n");
            prompt.append("{\\n");
            prompt.append("  \"recommendations\": [\\n");
            prompt.append("    {\\n");
            prompt.append("      \"dishName\": \"Name of the dish\",\\n");
            prompt.append("      \"explanation\": \"Why this dish is recommended\",\\n");
            prompt.append("      \"matchScore\": 85 // A score from 0-100 indicating how well this matches my preferences\\n");
            prompt.append("    },\\n");
            prompt.append("    // More recommendations...\\n");
            prompt.append("  ]\\n");
            prompt.append("}\\n");

            userMessage.put("content", prompt.toString());
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("response_format", Map.of("type", "json_object"));

            // Make API request
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // Extract and return the response
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                 if (responseBody == null) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("success", false);
                    errorMap.put("error", "Failed to get recommendations from OpenAI: Response body is null");
                    return errorMap;
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

                if (choices == null || choices.isEmpty()) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("success", false);
                    errorMap.put("error", "Failed to get recommendations from OpenAI: Choices are empty");
                    return errorMap;
                }

                Map<String, Object> choice = choices.get(0);

                if (choice == null) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("success", false);
                    errorMap.put("error", "Failed to get recommendations from OpenAI: Choice is null");
                    return errorMap;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");

                if (messageResponse == null || messageResponse.get("content") == null) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("success", false);
                    errorMap.put("error", "Failed to get recommendations from OpenAI: Message content is null");
                    return errorMap;
                }

                String content = (String) messageResponse.get("content");

                // Parse the JSON response
                try {
                    // For simplicity, we're returning the raw JSON string
                    // In a real application, you would parse this into a proper object
                    Map<String, Object> recommendationMap = new HashMap<>();
                    recommendationMap.put("success", true);
                    recommendationMap.put("recommendations", content);
                    return recommendationMap;
                } catch (Exception e) {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("success", false);
                    errorMap.put("error", "Failed to parse recommendations: " + e.getMessage());
                    errorMap.put("rawContent", content);
                    return errorMap;
                }
            } else {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("success", false);
                errorMap.put("error", "Failed to get recommendations from OpenAI: Status code is not OK");
                return errorMap;
            }
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("error", "Error calling OpenAI API: " + e.getMessage());
            return errorMap;
        }
    }

    /**
     * Fallback method for testing when OpenAI API is not available
     * @param menuText The menu text
     * @return List of mock recommendations
     */
    public List<String> getMockRecommendations(String menuText) {
        return List.of("Spaghetti Carbonara", "Grilled Salmon", "Vegetarian Pizza");
    }
}
