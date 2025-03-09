package com.eatsadvisor.eatsadvisor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MenuService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode extractTextFromImage(String imagePath) {
        try {
            File file = ResourceUtils.getFile(imagePath);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64Image = Base64.getEncoder().encodeToString(fileContent);
            return callExtractionApi(base64Image);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return objectMapper.valueToTree(errorMap);
        }
    }

    public JsonNode extractTextFromImage(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileContent);
            return callExtractionApi(base64Image);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return objectMapper.valueToTree(errorMap);
        }
    }

    private JsonNode callExtractionApi(String base64Image) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");

        List<Map<String, Object>> messageContent = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text",
                "Extract the text from the provided image and return only a valid JSON object containing the menu items and their prices. "
                        + "Ensure the extracted text is in English. If no menu-relevant text is found, if the text is in another language, or if there is no text in the image, "
                        + "return a JSON object in the format {\"error\": \"error message\"} specifying the issue without additional text, labels, markdown formatting, or extra structures.");
        messageContent.add(textContent);

        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, String> imageUrlMap = new HashMap<>();
        imageUrlMap.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.put("image_url", imageUrlMap);
        messageContent.add(imageContent);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", messageContent);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0);
        requestBody.put("top_p", 1);
        requestBody.put("max_tokens", 1000);
        requestBody.put("n", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
        System.out.println(response);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            String extractedText = contentNode.asText().trim();

            // Remove potential markdown formatting (```json)
            extractedText = extractedText.replaceAll("^```json", "").replaceAll("```$", "").trim();

            // Handle cases where extraction failed
            if (extractedText.isEmpty()) {
                return objectMapper.valueToTree(Map.of("error", "There was no text in the in the image you uploaded"));
            }

            // Parse JSON response
            JsonNode extractedJson;
            try {
                extractedJson = objectMapper.readTree(extractedText);
            } catch (Exception e) {
                return objectMapper.valueToTree(Map.of("error", "Invalid JSON response."));
            }

            // Check for error message in extracted JSON
            if (extractedJson.has("error")) {
                return extractedJson;
            }

            // Validate if extracted text is English and menu-relevant
            if (!isTextEnglish(extractedJson.toString())) {
                return objectMapper.valueToTree(Map.of("error", "Currently we only work with English text."));
            }

            if (!isMenuRelevant(extractedJson.toString())) {
                return objectMapper.valueToTree(
                        Map.of("error", "The text in the image does not contain menu-relevant information."));
            }

            return extractedJson;
        } else {
            return objectMapper.valueToTree(Map.of("error", "Non-OK HTTP status: " + response.getStatusCode()));
        }
    }

    private boolean isTextEnglish(String text) {
        return text.matches(".*[a-zA-Z].*"); // Simple check for English letters
    }

    private boolean isMenuRelevant(String text) {
        return text.toLowerCase().matches(".*(menu|dish|price|meal|drink|food).*");
    }

    public JsonNode classifyDishes(JsonNode extractedMenu, Map<String, Object> userPreferences) {
        try {
            // Extract relevant user preferences
            Map<String, Integer> flavorProfile = (Map<String, Integer>) userPreferences.get("flavorProfile");
            List<String> allergies = (List<String>) userPreferences.get("allergies");
            List<String> constraints = (List<String>) userPreferences.get("constraints");

            // Provide default values if user preferences are not available
            if (flavorProfile == null || flavorProfile.isEmpty()) {
                flavorProfile = new HashMap<>();
                flavorProfile.put("sweet", 7);
                flavorProfile.put("salty", 5);
                flavorProfile.put("sour", 6);
                flavorProfile.put("bitter", 3);
                flavorProfile.put("umami", 8);
                flavorProfile.put("spicy", 4);
                flavorProfile.put("savory", 9);
            }

            String prompt = String.format(
                    "Using the provided **flavor profile**, **allergies**, and **dietary constraints**, classify the extracted menu items into categories (e.g., beverages, main courses, side dishes, desserts). "
                            +
                            "Within **each category**, apply a traffic light system to indicate the probability of liking each item:\n\n"
                            +
                            "- **Green**: High probability of being liked.\n" +
                            "- **Orange**: Medium probability.\n" +
                            "- **Red**: Low probability (likely contains allergens or violates dietary constraints).\n\n"
                            +
                            "Your response must be a valid JSON object with **category names** as top-level keys. Under each category, include **green**, **orange**, and **red** arrays listing the corresponding items. "
                            +
                            "Do not add any extra text, labels, or formatting.\n\n" +
                            "**Flavor Profile:** %s\n\n" +
                            "**Allergies:** %s\n\n" +
                            "**Dietary Constraints:** %s\n\n" +
                            "**Extracted Menu Items:** %s",
                    objectMapper.writeValueAsString(flavorProfile),
                    objectMapper.writeValueAsString(allergies),
                    objectMapper.writeValueAsString(constraints),
                    extractedMenu.toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");

            Map<String, Object> textMessage = new HashMap<>();
            textMessage.put("type", "text");
            textMessage.put("text", prompt);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", Collections.singletonList(textMessage));

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                String categorizedText = contentNode.asText().trim();

                categorizedText = categorizedText.replaceAll("^```json", "")
                        .replaceAll("```$", "")
                        .trim();

                return objectMapper.readTree(categorizedText);
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Non-OK HTTP status: " + response.getStatusCode());
                return objectMapper.valueToTree(errorMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return objectMapper.valueToTree(errorMap);
        }
    }
}
