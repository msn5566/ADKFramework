package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_Success() throws Exception {
        Employee employee = new Employee(null, "John Doe", "IT", 60000.0, "john.doe@example.com");
        Employee createdEmployee = new Employee(1L, "John Doe", "IT", 60000.0, "john.doe@example.com");

        when(employeeService.createEmployee(any(Employee.class))).thenReturn(createdEmployee);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.department", is("IT")))
                .andExpect(jsonPath("$.salary", is(60000.0)));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        Employee employee = new Employee(1L, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Jane Doe")))
                .andExpect(jsonPath("$.department", is("HR")));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenThrow(new EntityNotFoundException("Employee not found with id: 1"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "John Doe", "IT", 60000.0, "john.doe@example.com"),
                new Employee(2L, "Jane Doe", "HR", 50000.0, "jane.doe@example.com")
        );
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Doe")));
    }

    @Test
    void updateEmployee_Success() throws Exception {
        Employee employee = new Employee(1L, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
        when(employeeService.updateEmployee(1L, employee)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Jane Doe")))
                .andExpect(jsonPath("$.department", is("HR")));
    }

    @Test
    void updateEmployee_NotFound() throws Exception {
        Employee employee = new Employee(1L, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
        when(employeeService.updateEmployee(1L, employee)).thenThrow(new EntityNotFoundException("Employee not found with id: 1"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_Success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_NotFound() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Employee not found with id: 1")).when(employeeService).deleteEmployee(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
```