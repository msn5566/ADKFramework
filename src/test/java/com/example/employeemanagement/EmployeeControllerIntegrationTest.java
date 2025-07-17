package com.example.employeemanagement;

import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll(); // Clear the database before each test.
    }


    @Test
    void createEmployee_success() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"));
    }

    @Test
    void getEmployeeById_success() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane.smith@example.com");

        Employee savedEmployee = employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/" + savedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void updateEmployee_success() throws Exception {
                Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane.smith@example.com");

        Employee savedEmployee = employeeRepository.save(employee);

        Employee updatedEmployee = new Employee();
        updatedEmployee.setFirstName("Updated");
        updatedEmployee.setLastName("LastName");
        updatedEmployee.setEmail("updated@example.com");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees/" + savedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEmployee)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void deleteEmployee_success() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane.smith@example.com");

        Employee savedEmployee = employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/" + savedEmployee.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllEmployees_success() throws Exception {
        Employee employee1 = new Employee();
        employee1.setFirstName("Jane");
        employee1.setLastName("Smith");
        employee1.setEmail("jane.smith@example.com");

        Employee employee2 = new Employee();
        employee2.setFirstName("John");
        employee2.setLastName("Doe");
        employee2.setEmail("john.doe@example.com");

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("Jane"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].firstName").value("John"));
    }


}
```

```java