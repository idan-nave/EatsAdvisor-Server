package com.eatsadvisor.eatsadvisor.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WebMvcTest(DishController.class)
@ActiveProfiles("test")
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDishController() {
        // TODO: Implement tests
    }
}
