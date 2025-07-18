package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId("1");
        employee.setName("Test Employee");
        employee.setDepartment("IT");
    }

    @Test
    void createEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Employee"))
                .andExpect(jsonPath("$.department").value("IT"));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Employee"))
                .andExpect(jsonPath("$.department").value("IT"));
    }

    @Test
    void updateEmployee_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.updateEmployee("1", employee)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Employee"))
                .andExpect(jsonPath("$.department").value("IT"));
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllEmployees_ReturnsOk() throws Exception {
        List<Employee> employees = Arrays.asList(employee);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Test Employee"))
                .andExpect(jsonPath("$[0].department").value("IT"));
    }
}
```

```java