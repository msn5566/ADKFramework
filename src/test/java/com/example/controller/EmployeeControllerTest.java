package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @InjectMocks
    private EmployeeController employeeController;

    @Mock
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_ValidInput_ReturnsCreatedEmployee() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setDepartment("IT");

        Employee createdEmployee = new Employee();
        createdEmployee.setId("1");
        createdEmployee.setName("John Doe");
        createdEmployee.setDepartment("IT");

        when(employeeService.createEmployee(employee)).thenReturn(createdEmployee);

        ResponseEntity<Employee> response = employeeController.createEmployee(employee);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdEmployee, response.getBody());
        verify(employeeService, times(1)).createEmployee(employee);
    }

    @Test
    void getEmployee_ExistingId_ReturnsEmployee() {
        String employeeId = "1";
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setName("John Doe");
        employee.setDepartment("IT");

        when(employeeService.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        ResponseEntity<Employee> response = employeeController.getEmployee(employeeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(employee, response.getBody());
        verify(employeeService, times(1)).getEmployee(employeeId);
    }

    @Test
    void getEmployee_NonExistingId_ReturnsNotFound() {
        String employeeId = "1";
        when(employeeService.getEmployee(employeeId)).thenReturn(Optional.empty());

        ResponseEntity<Employee> response = employeeController.getEmployee(employeeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService, times(1)).getEmployee(employeeId);
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        String employeeId = "1";
        Employee existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setName("John Doe");
        existingEmployee.setDepartment("IT");

        Employee updatedEmployeeDetails = new Employee();
        updatedEmployeeDetails.setName("Jane Doe");
        updatedEmployeeDetails.setDepartment("HR");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(employeeId);
        updatedEmployee.setName("Jane Doe");
        updatedEmployee.setDepartment("HR");

        when(employeeService.getEmployee(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeService.updateEmployee(employeeId, updatedEmployeeDetails)).thenReturn(updatedEmployee);

        ResponseEntity<Employee> response = employeeController.updateEmployee(employeeId, updatedEmployeeDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedEmployee, response.getBody());
        verify(employeeService, times(1)).getEmployee(employeeId);
        verify(employeeService, times(1)).updateEmployee(employeeId, updatedEmployeeDetails);
    }

    @Test
    void updateEmployee_NonExistingId_ReturnsNotFound() {
        String employeeId = "1";
        Employee updatedEmployeeDetails = new Employee();
        updatedEmployeeDetails.setName("Jane Doe");
        updatedEmployeeDetails.setDepartment("HR");

        when(employeeService.getEmployee(employeeId)).thenReturn(Optional.empty());

        ResponseEntity<Employee> response = employeeController.updateEmployee(employeeId, updatedEmployeeDetails);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(employeeService, times(1)).getEmployee(employeeId);
        verify(employeeService, never()).updateEmployee(employeeId, updatedEmployeeDetails);
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() {
        String employeeId = "1";

        ResponseEntity<Void> response = employeeController.deleteEmployee(employeeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(employeeService, times(1)).deleteEmployee(employeeId);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        Employee employee1 = new Employee();
        employee1.setId("1");
        employee1.setName("John Doe");
        employee1.setDepartment("IT");
        employees.add(employee1);

        Employee employee2 = new Employee();
        employee2.setId("2");
        employee2.setName("Jane Doe");
        employee2.setDepartment("HR");
        employees.add(employee2);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(employees, response.getBody());
        verify(employeeService, times(1)).getAllEmployees();
    }
}
```

```java