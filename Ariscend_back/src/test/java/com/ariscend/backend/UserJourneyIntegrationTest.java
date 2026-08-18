package com.ariscend.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class UserJourneyIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void authenticatedUserCompletesCoreCrudJourney() throws Exception {
        String email = "journey-" + UUID.randomUUID() + "@example.com";
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Journey User",
                                  "email": "%s",
                                  "password": "journey-secure-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);
        long userId = ((Number) JsonPath.read(
                registration.getResponse().getContentAsString(), "$.id")).longValue();

        exerciseHabits(userId, session);
        exerciseTasks(userId, session);
        exerciseNotes(userId, session);
        exerciseFinance(userId, session);
    }

    private void exerciseHabits(long userId, MockHttpSession session) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/users/{userId}/habits", userId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Caminar",
                                  "description": "Treinta minutos",
                                  "category": "Salud",
                                  "frequency": "WEEKLY",
                                  "targetDaysPerWeek": 5,
                                  "color": "#22C55E",
                                  "icon": "walking"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        long habitId = id(created);
        String today = LocalDate.now().toString();

        mockMvc.perform(post("/api/users/{userId}/habits/{habitId}/complete", userId, habitId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completedDate":"%s","notes":"Antes del desayuno"}
                                """.formatted(today)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.habitId").value(habitId))
                .andExpect(jsonPath("$.completedDate").value(today));
        mockMvc.perform(post("/api/users/{userId}/habits/{habitId}/complete", userId, habitId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completedDate\":\"%s\"}".formatted(today)))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/users/{userId}/habits/{habitId}/completions", userId, habitId)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(delete("/api/users/{userId}/habits/{habitId}", userId, habitId)
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{userId}/habits", userId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private void exerciseTasks(long userId, MockHttpSession session) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/users/{userId}/tasks", userId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Preparar revisión",
                                  "description": "Revisar el progreso",
                                  "dueDate": "2026-08-31",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn();
        long taskId = id(created);

        mockMvc.perform(put("/api/users/{userId}/tasks/{taskId}", userId, taskId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Terminar revisión",
                                  "description": "Incluir finanzas",
                                  "dueDate": "2026-09-01",
                                  "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Terminar revisión"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
        mockMvc.perform(patch("/api/users/{userId}/tasks/{taskId}/completed", userId, taskId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
        mockMvc.perform(delete("/api/users/{userId}/tasks/{taskId}", userId, taskId)
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{userId}/tasks/{taskId}", userId, taskId).session(session))
                .andExpect(status().isNotFound());
    }

    private void exerciseNotes(long userId, MockHttpSession session) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/users/{userId}/notes", userId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(""))
                .andReturn();
        long noteId = id(created);

        mockMvc.perform(put("/api/users/{userId}/notes/{noteId}", userId, noteId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Revisión mensual","content":"Resumen de gastos en MXN"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Revisión mensual"));
        mockMvc.perform(patch("/api/users/{userId}/notes/{noteId}/pinned", userId, noteId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pinned\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));
        mockMvc.perform(get("/api/users/{userId}/notes", userId)
                        .param("query", "mxn")
                        .param("pinned", "true")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(noteId));
        mockMvc.perform(delete("/api/users/{userId}/notes/{noteId}", userId, noteId)
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    private void exerciseFinance(long userId, MockHttpSession session) throws Exception {
        MvcResult categories = mockMvc.perform(get("/api/users/{userId}/finance/categories", userId)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
        List<Number> salaryIds = JsonPath.read(
                categories.getResponse().getContentAsString(), "$[?(@.systemKey == 'INCOME_SALARY')].id");
        long salaryCategoryId = salaryIds.getFirst().longValue();

        MvcResult card = mockMvc.perform(post("/api/users/{userId}/finance/cards", userId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "alias": "Débito principal",
                                  "issuer": "Banco de prueba",
                                  "type": "DEBIT",
                                  "lastFourDigits": "1234",
                                  "currency": "MXN",
                                  "openingBalance": 5000.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long cardId = id(card);

        MvcResult income = mockMvc.perform(post("/api/users/{userId}/finance/transactions", userId)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "INCOME",
                                  "categoryId": %d,
                                  "cardId": %d,
                                  "amount": 1000.00,
                                  "currency": "MXN",
                                  "description": "Ingreso de prueba",
                                  "transactionDate": "2026-08-05"
                                }
                                """.formatted(salaryCategoryId, cardId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.baseAmount").value(1000.00))
                .andReturn();
        long transactionId = id(income);

        mockMvc.perform(get("/api/users/{userId}/finance/summary", userId)
                        .param("dateFrom", "2026-08-01")
                        .param("dateTo", "2026-08-31")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000.00))
                .andExpect(jsonPath("$.totalExpenses").value(0))
                .andExpect(jsonPath("$.balance").value(1000.00));
        mockMvc.perform(delete("/api/users/{userId}/finance/transactions/{transactionId}", userId, transactionId)
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{userId}/finance/transactions", userId)
                        .param("status", "CANCELLED")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId));
    }

    private long id(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
