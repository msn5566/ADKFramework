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

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

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
        employee.setName("John Doe");
        employee.setDesignation("Software Engineer");
        employee.setEmail("john.doe@example.com");
        employee.setContactNumber("123-456-7890");
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
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById("1");
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById("2");
    }

    @Test
    void getAllEmployees_EmployeesExist_ReturnsOk() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Collections.singletonList(employee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void updateEmployee_ExistingIdAndValidInput_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));
        when(employeeService.updateEmployee("1", employee)).thenReturn(employee);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById("1");
        verify(employeeService, times(1)).updateEmployee("1", employee);
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
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));
        doNothing().when(employeeService).deleteEmployee("1");

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).getEmployeeById("1");
        verify(employeeService, times(1)).deleteEmployee("1");
    }

    @Test
    void deleteEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/employees/2"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployeeById("2");
        verify(employeeService, never()).deleteEmployee(anyString());
    }
}
```

```java