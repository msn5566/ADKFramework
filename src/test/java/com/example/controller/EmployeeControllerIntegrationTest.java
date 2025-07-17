package com.example.controller;

import com.example.entity.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_ValidInput_ReturnsCreated() throws Exception {
        Employee employee = new Employee(null, "Integration", "Test", "integration.test@example.com", 70000.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Integration"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("integration.test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(70000.0));
    }

     @Test
    void createEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        Employee employee = new Employee(null, "", "", "invalid-email", -100.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        // First, create an employee to ensure it exists
        Employee employee = new Employee(null, "Integration", "Test", "integration.test@example.com", 70000.0);
        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(responseJson, Employee.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + createdEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Integration"));
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEmployees_ReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateEmployee_ExistingId_ReturnsOk() throws Exception {
        // First, create an employee to ensure it exists
        Employee employee = new Employee(null, "Integration", "Test", "integration.test@example.com", 70000.0);
        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(responseJson, Employee.class);

        Employee updatedEmployee = new Employee(null, "UpdatedIntegration", "UpdatedTest", "updated.integration.test@example.com", 75000.0);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/" + createdEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("UpdatedIntegration"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("UpdatedTest"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("updated.integration.test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.salary").value(75000.0));
    }

    @Test
    void updateEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        Employee updatedEmployee = new Employee(null, "UpdatedIntegration", "UpdatedTest", "updated.integration.test@example.com", 75000.0);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        // First, create an employee to ensure it exists
        Employee employee = new Employee(null, "Integration", "Test", "integration.test@example.com", 70000.0);
        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(responseJson, Employee.class);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/" + createdEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
```