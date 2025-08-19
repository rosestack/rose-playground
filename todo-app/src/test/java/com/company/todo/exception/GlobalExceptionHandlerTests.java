package com.company.todo.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTests {

    MockMvc mockMvc;

    @RestController
    @RequestMapping("/api/dummy")
    @Validated
    static class DummyController {
        @GetMapping("/cve")
        public String cve() {
            throw new jakarta.validation.ConstraintViolationException("bad", java.util.Collections.emptySet());
        }

        @GetMapping("/err")
        public String err() {
            throw new RuntimeException("boom");
        }
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ConstraintViolationException → 400")
    void should_handle_constraint_violation() throws Exception {
        mockMvc.perform(get("/api/dummy/cve"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("constraint")));
    }

    @Test
    @DisplayName("兜底异常 → 500")
    void should_handle_other_exception_as_500() throws Exception {
        mockMvc.perform(get("/api/dummy/err"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("internal error"));
    }
}
