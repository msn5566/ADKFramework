package com.example.service;

import com.example.entity.Employee;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

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
        employee.setEmail("john.doe@example.com");
        employee.setDepartment("IT");
        employee.setDesignation("Developer");
        employee.setContactNumber("123-456-7890");
    }

    @Test
    void createEmployee_ShouldReturnSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("John Doe", savedEmployee.getName());
        verify(employeeRepository).save(employee);
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenEmployeeExists() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployeeById("1");

        assertNotNull(retrievedEmployee);
        assertEquals("John Doe", retrievedEmployee.getName());
        verify(employeeRepository).findById("1");
    }

    @Test
    void getEmployeeById_ShouldThrowException_WhenEmployeeDoesNotExist() {
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById("2"));
        verify(employeeRepository).findById("2");
    }

    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee_WhenEmployeeExists() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setEmail("updated.email@example.com");
        employeeDetails.setDepartment("Updated Department");
        employeeDetails.setDesignation("Updated Designation");
        employeeDetails.setContactNumber("987-654-3210");

        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("Updated Name", updatedEmployee.getName());
        assertEquals("Updated Department", updatedEmployee.getDepartment());
        verify(employeeRepository).findById("1");
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_ShouldThrowException_WhenEmployeeDoesNotExist() {
        Employee employeeDetails = new Employee();
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee("2", employeeDetails));
        verify(employeeRepository).findById("2");
    }

    @Test
    void deleteEmployee_ShouldDeleteEmployee_WhenEmployeeExists() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee("1");

        verify(employeeRepository).findById("1");
        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_ShouldThrowException_WhenEmployeeDoesNotExist() {
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee("2"));
        verify(employeeRepository).findById("2");
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getName());
        verify(employeeRepository).findAll();
    }

    @Test
    void getAllEmployees_ShouldReturnEmptyList_WhenNoEmployeesExist() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertTrue(employees.isEmpty());
        verify(employeeRepository).findAll();
    }
}
```

```java