package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllEmployees() throws Exception {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890"));
        employees.add(new Employee(2L, "Jane", "Smith", "jane.smith@example.com", 60000.0, "987-654-3210"));

        Mockito.when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        Employee employee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");
        Mockito.when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"));
    }

     @Test
    public void testGetEmployeeById_NotFound() throws Exception {
        Mockito.when(employeeService.getEmployeeById(1L)).thenThrow(new EntityNotFoundException("Employee not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testCreateEmployee() throws Exception {
        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");
        Employee savedEmployee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");

        Mockito.when(employeeService.createEmployee(Mockito.any(Employee.class))).thenReturn(savedEmployee);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    public void testUpdateEmployee() throws Exception {
        Employee employee = new Employee(1L, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0, "000-000-0000");

        Mockito.when(employeeService.updateEmployee(Mockito.eq(1L), Mockito.any(Employee.class))).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("UpdatedJohn"));
    }

    @Test
    public void testUpdateEmployee_NotFound() throws Exception {
        Employee employee = new Employee(1L, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0, "000-000-0000");

        Mockito.when(employeeService.updateEmployee(Mockito.eq(1L), Mockito.any(Employee.class)))
                .thenThrow(new EntityNotFoundException("Employee not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testDeleteEmployee() throws Exception {
        Mockito.doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testDeleteEmployee_NotFound() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Employee not found")).when(employeeService).deleteEmployee(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testCreateEmployee_InvalidInput() throws Exception {
        Employee invalidEmployee = new Employee(null, "", "", "invalid-email", -100.0, "1234567890");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testUpdateEmployee_InvalidInput() throws Exception {
         Employee invalidEmployee = new Employee(1L, "", "", "invalid-email", -100.0, "1234567890");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
```

```java