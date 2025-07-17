package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("1", savedEmployee.getId());
        verify(employeeRepository).save(employee);
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Optional<Employee> retrievedEmployee = employeeService.getEmployeeById("1");

        assertTrue(retrievedEmployee.isPresent());
        assertEquals("John Doe", retrievedEmployee.get().getName());
        verify(employeeRepository).findById("1");
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsEmptyOptional() {
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Employee> retrievedEmployee = employeeService.getEmployeeById("2");

        assertFalse(retrievedEmployee.isPresent());
        verify(employeeRepository).findById("2");
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setEmail("updated.email@example.com");
        employeeDetails.setDepartment("Updated Department");

        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("Updated Name", updatedEmployee.getName());
        assertEquals("updated.email@example.com", updatedEmployee.getEmail());
        assertEquals("Updated Department", updatedEmployee.getDepartment());
        verify(employeeRepository).findById("1");
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsException() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setEmail("updated.email@example.com");
        employeeDetails.setDepartment("Updated Department");

        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> employeeService.updateEmployee("2", employeeDetails));
        verify(employeeRepository).findById("2");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        employeeService.deleteEmployee("1");

        verify(employeeRepository).deleteById("1");
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getName());
        verify(employeeRepository).findAll();
    }
}
```

```java