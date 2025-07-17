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
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

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
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment("IT");
    }

    @Test
    void createEmployee_ValidInput_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.department", is("IT")));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.department", is("IT")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    void updateEmployee_ExistingIdAndValidInput_ReturnsOk() throws Exception {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setFirstName("UpdatedJohn");
        updatedEmployee.setLastName("UpdatedDoe");
        updatedEmployee.setEmail("updated.john.doe@example.com");
        updatedEmployee.setDepartment("UpdatedIT");

        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(updatedEmployee);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("UpdatedJohn")))
                .andExpect(jsonPath("$.lastName", is("UpdatedDoe")))
                .andExpect(jsonPath("$.email", is("updated.john.doe@example.com")))
                .andExpect(jsonPath("$.department", is("UpdatedIT")));

        verify(employeeService, times(1)).updateEmployee(eq(1L), any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    void getAllEmployees_ReturnsOk() throws Exception {
        List<Employee> employees = new ArrayList<>();
        employees.add(employee);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void createEmployee_InvalidInput_ReturnsBadRequest() throws Exception {
        Employee invalidEmployee = new Employee();
        invalidEmployee.setFirstName(""); // Invalid: Empty first name
        invalidEmployee.setLastName("Doe");
        invalidEmployee.setEmail("invalid-email"); // Invalid: Invalid email format
        invalidEmployee.setDepartment("IT");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class)); // Ensure service method is not called
    }

}
```

```java