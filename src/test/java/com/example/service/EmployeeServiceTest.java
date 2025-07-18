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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
        employee.setName("Test Employee");
        employee.setDepartment("IT");
    }

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("1", savedEmployee.getId());
        assertEquals("Test Employee", savedEmployee.getName());
        assertEquals("IT", savedEmployee.getDepartment());
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployeeById("1");

        assertNotNull(retrievedEmployee);
        assertEquals("1", retrievedEmployee.getId());
        assertEquals("Test Employee", retrievedEmployee.getName());
        assertEquals("IT", retrievedEmployee.getDepartment());
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById("2"));
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Employee");
        employeeDetails.setDepartment("HR");

        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("Updated Employee", updatedEmployee.getName());
        assertEquals("HR", updatedEmployee.getDepartment());
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Employee");
        employeeDetails.setDepartment("HR");

        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee("2", employeeDetails));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee("1");

        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee("2"));
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee);
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> retrievedEmployees = employeeService.getAllEmployees();

        assertNotNull(retrievedEmployees);
        assertEquals(1, retrievedEmployees.size());
        assertEquals("1", retrievedEmployees.get(0).getId());
        assertEquals("Test Employee", retrievedEmployees.get(0).getName());
        assertEquals("IT", retrievedEmployees.get(0).getDepartment());
    }
}
```

```java