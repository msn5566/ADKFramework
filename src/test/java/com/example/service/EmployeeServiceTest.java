package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId("1");
        employee.setName("John Doe");
        employee.setAddress("123 Main St");
        employee.setDepartment("IT");
    }

    @Test
    void createEmployee_shouldSaveEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(new Employee());

        assertNotNull(savedEmployee);
        assertEquals(employee, savedEmployee);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployee_shouldReturnEmployee_whenEmployeeExists() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployee("1");

        assertNotNull(retrievedEmployee);
        assertEquals(employee, retrievedEmployee);
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void getEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> employeeService.getEmployee("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Employee not found", exception.getReason());
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void updateEmployee_shouldUpdateEmployee_whenEmployeeExists() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployeeDetails = new Employee();
        updatedEmployeeDetails.setName("Updated Name");
        updatedEmployeeDetails.setAddress("Updated Address");
        updatedEmployeeDetails.setDepartment("Updated Department");

        Employee updatedEmployee = employeeService.updateEmployee("1", updatedEmployeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("Updated Name", updatedEmployee.getName());
        assertEquals("Updated Address", updatedEmployee.getAddress());
        assertEquals("Updated Department", updatedEmployee.getDepartment());
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        Employee updatedEmployeeDetails = new Employee();
        updatedEmployeeDetails.setName("Updated Name");
        updatedEmployeeDetails.setAddress("Updated Address");
        updatedEmployeeDetails.setDepartment("Updated Department");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> employeeService.updateEmployee("1", updatedEmployeeDetails));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Employee not found", exception.getReason());
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenEmployeeExists() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).deleteById("1");

        employeeService.deleteEmployee("1");

        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> employeeService.deleteEmployee("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Employee not found", exception.getReason());
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, never()).deleteById("1");
    }

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals(employee, employees.get(0));
        verify(employeeRepository, times(1)).findAll();
    }
}
```

```java