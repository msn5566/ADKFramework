package com.example;

import com.example.controller.EmployeeController;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        Employee employee = new Employee(employeeId, "John Doe", "Developer", "IT");
        when(employeeService.getEmployeeById(employeeId)).thenReturn(Optional.of(employee));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(employeeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("Developer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.department").value("IT"));
    }

    @Test
    public void testCreateEmployee() throws Exception {
        // Arrange
        Employee employee = new Employee(null, "Jane Smith", "Manager", "HR");
        Employee savedEmployee = new Employee("456", "Jane Smith", "Manager", "HR");
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(savedEmployee);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("456"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Jane Smith"));
    }

    @Test
    public void testUpdateEmployee() throws Exception {
        // Arrange
        String employeeId = "123";
        Employee employee = new Employee(null, "Updated Name", "Updated Role", "Updated Department");
        Employee updatedEmployee = new Employee(employeeId, "Updated Name", "Updated Role", "Updated Department");

        when(employeeService.updateEmployee(any(String.class), any(Employee.class))).thenReturn(updatedEmployee);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(employeeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Name"));

    }
}
```


```java