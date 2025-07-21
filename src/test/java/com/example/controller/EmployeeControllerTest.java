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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
        employee.setName("John Doe");
        employee.setDepartment("IT");
        employee.setSalary(50000.0);
    }

    @Test
    void createEmployeeTest() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getEmployeeByIdTest() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));

        mockMvc.perform(MockMvcRequestBuilders.get("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getEmployeeByIdNotFoundTest() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/employees/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEmployeeTest() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));
        when(employeeService.updateEmployee("1", employee)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void updateEmployeeNotFoundTest() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/employees/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployeeTest() throws Exception {
        when(employeeService.getEmployeeById("1")).thenReturn(Optional.of(employee));
        doNothing().when(employeeService).deleteEmployee("1");

        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployeeNotFoundTest() throws Exception {
        when(employeeService.getEmployeeById("2")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllEmployeesTest() throws Exception {
        List<Employee> employees = Arrays.asList(employee, new Employee());
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }
}
```

```java