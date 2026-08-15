package com.ariscend.backend.security;

import com.ariscend.backend.repository.FinanceSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FinanceSettingsRepository financeSettingsRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void sessionProtectsResourcesAndEnforcesOwnership() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana",
                                  "email": "ana.integration@example.com",
                                  "password": "a-secure-password-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana.integration@example.com"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);
        Number userId = com.jayway.jsonpath.JsonPath.read(
                registration.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.longValue()));

        mockMvc.perform(get("/api/users/{userId}/tasks", userId.longValue()).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{userId}/tasks", userId.longValue() + 1).session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedResourceRequestReturnsJsonUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/1/notes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticación requerida."));
    }

    @Test
    void newlyRegisteredUserCanLoadEveryInitialPage() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Fresh User",
                                  "email": "fresh.integration@example.com",
                                  "password": "a-secure-password-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);
        Number userId = com.jayway.jsonpath.JsonPath.read(
                registration.getResponse().getContentAsString(), "$.id");
        long id = userId.longValue();

        mockMvc.perform(get("/api/users/{userId}/habits", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/users/{userId}/habits/completions", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/users/{userId}/tasks", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/users/{userId}/notes", id)
                        .param("archived", "false")
                        .param("page", "0")
                        .param("size", "30")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/users/{userId}/finance/summary", id)
                        .param("dateFrom", "2026-08-01")
                        .param("dateTo", "2026-08-31")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("MXN"))
                .andExpect(jsonPath("$.totalIncome").value(0))
                .andExpect(jsonPath("$.totalExpenses").value(0))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.incomeByCategory").isEmpty())
                .andExpect(jsonPath("$.expensesByCategory").isEmpty())
                .andExpect(jsonPath("$.originalTotalsByCurrency").isEmpty());
        mockMvc.perform(get("/api/users/{userId}/finance/transactions", id)
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "40")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        mockMvc.perform(get("/api/users/{userId}/finance/categories", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(16));
        mockMvc.perform(get("/api/users/{userId}/finance/cards", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        for (String goalStatus : java.util.List.of("ACTIVE", "COMPLETED", "CANCELLED")) {
            mockMvc.perform(get("/api/users/{userId}/finance/goals", id)
                            .param("status", goalStatus)
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
        mockMvc.perform(get("/api/users/{userId}/finance/settings", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("MXN"))
                .andExpect(jsonPath("$.paymentAlertDays").value(3));

        org.junit.jupiter.api.Assertions.assertTrue(financeSettingsRepository.findByUserId(id).isPresent());
    }

    @Test
    void changedPasswordReplacesOldCredentials() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Password User",
                                  "email": "password.integration@example.com",
                                  "password": "old-secure-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);

        mockMvc.perform(post("/api/auth/password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-secure-password",
                                  "newPassword": "new-secure-password"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "password.integration@example.com",
                                  "password": "old-secure-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "password.integration@example.com",
                                  "password": "new-secure-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("password.integration@example.com"));
    }
}
