package com.example;

import com.example.controller.EmployeeController;
import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
        Employee employee = new Employee(null, "John Doe", "Developer", "john.doe@example.com", "1234567890");
        Employee createdEmployee = new Employee(1L, "John Doe", "Developer", "john.doe@example.com", "1234567890");

        when(employeeService.createEmployee(ArgumentMatchers.any(Employee.class))).thenReturn(createdEmployee);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsOk() throws Exception {
        Employee employee = new Employee(1L, "John Doe", "Developer", "john.doe@example.com", "1234567890");

        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getAllEmployees_ReturnsOk() throws Exception {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "John Doe", "Developer", "john.doe@example.com", "1234567890"),
                new Employee(2L, "Jane Smith", "Manager", "jane.smith@example.com", "0987654321")
        );

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void updateEmployee_ExistingId_ReturnsOk() throws Exception {
        Employee employee = new Employee(1L, "Updated Name", "Updated Job", "updated@example.com", "1122334455");

        when(employeeService.updateEmployee(ArgumentMatchers.eq(1L), ArgumentMatchers.any(Employee.class))).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}
```


```java