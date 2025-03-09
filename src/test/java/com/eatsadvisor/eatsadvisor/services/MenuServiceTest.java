package com.eatsadvisor.eatsadvisor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        // Use reflection to set the private fields
        ReflectionTestUtils.setField(menuService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(menuService, "restTemplate", restTemplate);
    }

    @Test
    void extractTextFromImage_WithValidMultipartFile_ShouldReturnJsonNode() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("test-image-data".getBytes());
        
        // Create a mock response from the OpenAI API
        ObjectNode contentNode = objectMapper.createObjectNode();
        contentNode.put("item1", "Burger");
        contentNode.put("price1", "$10.99");

        ObjectNode responseNode = objectMapper.createObjectNode();
        com.fasterxml.jackson.databind.node.ArrayNode choicesArray = objectMapper.createArrayNode();
        ObjectNode choiceNode = objectMapper.createObjectNode();
        ObjectNode messageNode = objectMapper.createObjectNode();

        messageNode.set("content", contentNode);
        choiceNode.set("message", messageNode);
        choicesArray.add(choiceNode);
        responseNode.set("choices", choicesArray);

        String responseJson = objectMapper.writeValueAsString(responseNode);
		ResponseEntity<String> mockResponse = new ResponseEntity<>(responseJson, HttpStatus.OK);

		when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
				.thenReturn(mockResponse);

		// Act
		JsonNode result = menuService.extractTextFromImage(mockFile);

		// Assert
		assertNotNull(result);
		assertTrue(result instanceof ObjectNode);
		ObjectNode extractedObject = (ObjectNode) result;
		assertNotNull(extractedObject.get("item1"), "item1 should not be null");
		assertNotNull(extractedObject.get("price1"), "price1 should not be null");
		assertEquals("Burger", extractedObject.get("item1").asText());
		assertEquals("$10.99", extractedObject.get("price1").asText());
		verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void extractTextFromImage_WithApiError_ShouldReturnErrorJson() throws Exception {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("test-image-data".getBytes());
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RuntimeException("API Error"));
        
        // Act
        JsonNode result = menuService.extractTextFromImage(mockFile);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.has("error"));
        assertEquals("API Error", result.get("error").asText());
    }

    @Test
    void classifyDishes_WithValidInput_ShouldReturnClassifiedDishes() throws Exception {
        // Arrange
        ObjectNode menuNode = objectMapper.createObjectNode();
        menuNode.put("item1", "Burger");
        menuNode.put("price1", "$10.99");
        menuNode.put("item2", "Salad");
        menuNode.put("price2", "$8.99");
        
        Map<String, Object> userPreferences = new HashMap<>();
        Map<String, Integer> flavorProfile = new HashMap<>();
        flavorProfile.put("sweet", 7);
        flavorProfile.put("salty", 5);
        userPreferences.put("flavorProfile", flavorProfile);
        userPreferences.put("allergies", Arrays.asList("Peanuts"));
        userPreferences.put("constraints", Arrays.asList("Vegetarian"));

        // Create a mock response for the classification
        ObjectNode mainCoursesNode = objectMapper.createObjectNode();
        mainCoursesNode.putArray("green").add("Salad");
        mainCoursesNode.putArray("orange").add("Burger");
        mainCoursesNode.putArray("red").add("Peanut Butter Sandwich");
        
        ObjectNode classifiedNode = objectMapper.createObjectNode();
        classifiedNode.set("Main Courses", mainCoursesNode);
        
        String responseJson = createOpenAIResponse(classifiedNode.toString());
        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseJson, HttpStatus.OK);
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockResponse);
        
        // Act
        JsonNode result = menuService.classifyDishes(menuNode, userPreferences);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.has("Main Courses"));
        assertTrue(result.path("Main Courses").has("green"));
        assertEquals("Salad", result.path("Main Courses").path("green").get(0).asText());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void classifyDishes_WithApiError_ShouldReturnErrorJson() throws Exception {
        // Arrange
        ObjectNode menuNode = objectMapper.createObjectNode();
        menuNode.put("item1", "Burger");
        
        Map<String, Object> userPreferences = new HashMap<>();
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RuntimeException("API Error"));
        
        // Act
        JsonNode result = menuService.classifyDishes(menuNode, userPreferences);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.has("error"));
        assertEquals("API Error", result.get("error").asText());
    }

    // Helper method to create a mock OpenAI API response
    private String createOpenAIResponse(String content) {
        return "{\n" +
               "  \"choices\": [\n" +
               "    {\n" +
               "      \"message\": {\n" +
               "        \"content\": \"" + content + "\"\n" +
               "      }\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
}
