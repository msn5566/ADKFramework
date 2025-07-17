package com.example;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
    }

    @Test
    void createEmployee_Success() throws Exception {
        Employee employee = new Employee(null, "John Doe", "IT", 60000.0, "john.doe@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.department", is("IT")))
                .andExpect(jsonPath("$.salary", is(60000.0)));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        Employee employee = new Employee(null, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
        employee = employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")))
                .andExpect(jsonPath("$.department", is("HR")));
    }

    @Test
    void updateEmployee_Success() throws Exception {
        Employee employee = new Employee(null, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
        employee = employeeRepository.save(employee);

        Employee updatedEmployee = new Employee(null, "Updated Name", "Finance", 70000.0, "updated.doe@example.com");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.department", is("Finance")))
                .andExpect(jsonPath("$.salary", is(70000.0)));
    }

    @Test
    void deleteEmployee_Success() throws Exception {
        Employee employee = new Employee(null, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
        employee = employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/" + employee.getId()))
                .andExpect(status().isNoContent());
    }
}
```


```java