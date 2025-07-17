package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_ValidInput_ReturnsCreated() throws Exception {
        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com", 50000.0);
        Employee savedEmployee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0);

        when(employeeService.createEmployee(any(Employee.class))).thenReturn(savedEmployee);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        Employee employee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0);

        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"));
    }

    @Test
    void getAllEmployees_ReturnsOk() throws Exception {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0));
        employees.add(new Employee(2L, "Jane", "Smith", "jane.smith@example.com", 60000.0));

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void updateEmployee_ExistingId_ReturnsOk() throws Exception {
        Employee employeeDetails = new Employee(null, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0);
        Employee updatedEmployee = new Employee(1L, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0);

        when(employeeService.updateEmployee(any(Long.class), any(Employee.class))).thenReturn(updatedEmployee);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDetails)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("UpdatedJohn"));
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
```


```java