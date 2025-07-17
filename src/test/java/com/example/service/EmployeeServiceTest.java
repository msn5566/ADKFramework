package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        employee1 = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0);
        employee2 = new Employee(2L, "Jane", "Smith", "jane.smith@example.com", 60000.0);
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee savedEmployee = employeeService.createEmployee(new Employee(null, "John", "Doe", "john.doe@example.com", 50000.0));

        assertNotNull(savedEmployee);
        assertEquals(employee1.getId(), savedEmployee.getId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void createEmployee_InvalidEmployee_ThrowsException() {
        Employee invalidEmployee = new Employee(null, null, null, null, -1000.0);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.createEmployee(invalidEmployee);
        });
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));

        Employee retrievedEmployee = employeeService.getEmployeeById(1L);

        assertNotNull(retrievedEmployee);
        assertEquals(employee1.getFirstName(), retrievedEmployee.getFirstName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsException() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(100L));
        verify(employeeRepository, times(1)).findById(100L);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertNotNull(allEmployees);
        assertEquals(2, allEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        Employee employeeDetails = new Employee(null, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee updatedEmployee = employeeService.updateEmployee(1L, employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("UpdatedJohn", updatedEmployee.getFirstName());
        assertEquals("UpdatedDoe", updatedEmployee.getLastName());
        assertEquals("updated.john.doe@example.com", updatedEmployee.getEmail());
        assertEquals(55000.0, updatedEmployee.getSalary());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsException() {
        Employee employeeDetails = new Employee(null, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0);
        when(employeeRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(100L, employeeDetails));
        verify(employeeRepository, times(1)).findById(100L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEmployee_NonExistingId_ThrowsException() {
        when(employeeRepository.existsById(100L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(100L));
        verify(employeeRepository, times(1)).existsById(100L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
```

```java