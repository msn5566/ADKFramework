package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        employee = new Employee("1", "John Doe", "IT");
    }

    @Test
    void createEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.department").value("IT"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.department").value("IT"));

        verify(employeeService, times(1)).getEmployeeById("1");
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById("2");
    }

    @Test
    void updateEmployee_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));
        when(employeeService.updateEmployee(eq("1"), any(Employee.class))).thenReturn(employee);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.department").value("IT"));

        verify(employeeService, times(1)).updateEmployee(eq("1"), any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/employees/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById("2");
        verify(employeeService, never()).updateEmployee(anyString(), any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee("1");

        mockMvc.perform(delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee("1");
    }

    @Test
    void getAllEmployees_ReturnsOk() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].department").value("IT"));

        verify(employeeService, times(1)).getAllEmployees();
    }
}
```

```java