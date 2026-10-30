package com.maurodc90.hauntedstacktrace.controller;

import com.maurodc90.hauntedstacktrace.model.Haunting;
import com.maurodc90.hauntedstacktrace.service.GhostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the séance endpoint without a running model: GhostService is replaced by a Mockito mock,
 * so the tests also run in CI where Ollama is not installed.
 */
@WebMvcTest(SeanceController.class)
class SeanceControllerTest {

    private static final String STACK_TRACE = """
            java.lang.ArithmeticException: / by zero
            \tat com.example.Budget.averagePerDay(Budget.java:18)
            \tat com.example.Budget.main(Budget.java:9)
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GhostService ghostService;

    /**
     * Text without a stack frame must be refused before it reaches the model.
     */
    @Test
    void rejectsTextThatIsNotAStackTrace() throws Exception {
        mockMvc.perform(post("/seance")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("give me a new error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ghost").value("Applet"));

        verifyNoInteractions(ghostService);
    }

    /**
     * A real stack trace is passed to the ghost and its answer is returned as JSON.
     */
    @Test
    void explainsARealStackTrace() throws Exception {
        Haunting haunting = new Haunting(
                "Vector",
                "java.lang.ArithmeticException",
                "com.example.Budget.averagePerDay(Budget.java:18)",
                "Division by zero.",
                "Check the divisor before dividing.",
                "Do not divide my soul by nothingness.");
        when(ghostService.summon(anyString())).thenReturn(haunting);

        mockMvc.perform(post("/seance")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(STACK_TRACE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.culprit").value("com.example.Budget.averagePerDay(Budget.java:18)"));
    }
}