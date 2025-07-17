package com.example;

import com.example.controller.EmployeeController;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    @Test
    public void testGetEmployeeById() throws Exception {
        // Arrange
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", "john.doe@example.com", "IT");
        Mockito.when(employeeService.getEmployeeById(employeeId)).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(employeeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.department").value("IT"));
    }

    @Test
    public void testCreateEmployee() throws Exception {
        // Arrange
        Employee employee = new Employee(null, "Jane Smith", "jane.smith@example.com", "HR");
        Employee savedEmployee = new Employee("456", "Jane Smith", "jane.smith@example.com", "HR"); // Simulate ID assignment
        Mockito.when(employeeService.createEmployee(Mockito.any(Employee.class))).thenReturn(savedEmployee);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("456")) // Verify ID in response
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Jane Smith"));
    }

     @Test
    public void testGetAllEmployees() throws Exception {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("1", "John Doe", "john.doe@example.com", "IT"));
        employees.add(new Employee("2", "Jane Smith", "jane.smith@example.com", "HR"));
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    public void testUpdateEmployee() throws Exception {
        // Arrange
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "Updated Name", "updated.email@example.com", "Finance");
        when(employeeService.updateEmployee(Mockito.eq(employeeId), any(Employee.class))).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated.email@example.com"))
                .andExpect(jsonPath("$.department").value("Finance"));
    }

    @Test
    public void testUpdateEmployee_NotFound() throws Exception {
        // Arrange
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "Updated Name", "updated.email@example.com", "Finance");
        when(employeeService.updateEmployee(Mockito.eq(employeeId), any(Employee.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployee() throws Exception {
        // Arrange
        String employeeId = "123";
        when(employeeService.deleteEmployee(employeeId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployee_NotFound() throws Exception {
        // Arrange
        String employeeId = "123";
        when(employeeService.deleteEmployee(employeeId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEmployeeById_NotFound() throws Exception {
        // Arrange
        String employeeId = "123";
        Mockito.when(employeeService.getEmployeeById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
```