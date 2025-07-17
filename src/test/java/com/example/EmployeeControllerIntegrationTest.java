package com.example;

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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateEmployeeIntegration() throws Exception {
        Employee employee = new Employee(null, "Integration Test", "Tester", "QA");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Integration Test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("Tester"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.department").value("QA"));
    }

     @Test
    public void testGetEmployeeByIdIntegration() throws Exception {
        // First, create an employee
        Employee employeeToCreate = new Employee(null, "Get Test", "Tester", "QA");

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeToCreate)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(responseJson, Employee.class);
        String employeeId = createdEmployee.getId();

        // Then, retrieve the employee by ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(employeeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Get Test"));
    }

    @Test
    public void testUpdateEmployeeIntegration() throws Exception {
        // First, create an employee
        Employee employeeToCreate = new Employee(null, "Update Test", "Tester", "QA");

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeToCreate)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(responseJson, Employee.class);
        String employeeId = createdEmployee.getId();

        // Then, update the employee
        Employee employeeUpdate = new Employee(null, "Updated Name", "Updated Role", "Updated Department");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeUpdate)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(employeeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Name"));
    }


    @Test
    public void testDeleteEmployeeIntegration() throws Exception {
        // First, create an employee
        Employee employeeToCreate = new Employee(null, "Delete Test", "Tester", "QA");

        String responseJson = mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeToCreate)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(responseJson, Employee.class);
        String employeeId = createdEmployee.getId();

        // Then, delete the employee
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify that the employee is deleted (optional)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllEmployeesIntegration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateEmployeeValidation() throws Exception {
        Employee employee = new Employee(null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest()); // Expecting a 400 due to validation errors
    }
}
```