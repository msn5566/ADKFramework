package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        Employee employee = new Employee(null, "John Doe", "john.doe@example.com", "IT");
        Employee savedEmployee = new Employee("123", "John Doe", "john.doe@example.com", "IT");
        when(employeeRepository.save(employee)).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(employee);

        assertEquals(savedEmployee, result);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void createEmployee_InvalidEmployee_ThrowsException() {
        Employee employee = new Employee(null, null, "invalid-email", "IT");

        assertThrows(ConstraintViolationException.class, () -> employeeService.createEmployee(employee));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", "john.doe@example.com", "IT");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployeeById(employeeId);

        assertTrue(result.isPresent());
        assertEquals(employee, result.get());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsEmptyOptional() {
        String employeeId = "123";
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById(employeeId);

        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("1", "John Doe", "john.doe@example.com", "IT"));
        employees.add(new Employee("2", "Jane Smith", "jane.smith@example.com", "HR"));
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingIdAndValidEmployee_ReturnsUpdatedEmployee() {
        String employeeId = "123";
        Employee existingEmployee = new Employee(employeeId, "John Doe", "john.doe@example.com", "IT");
        Employee updatedEmployee = new Employee(employeeId, "John Updated", "john.updated@example.com", "Finance");

        when(employeeRepository.existsById(employeeId)).thenReturn(true);
        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        Employee result = employeeService.updateEmployee(employeeId, updatedEmployee);

        assertEquals(updatedEmployee, result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, times(1)).save(updatedEmployee);
    }


    @Test
    void updateEmployee_NonExistingId_ReturnsNull() {
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", "john.doe@example.com", "IT");
        when(employeeRepository.existsById(employeeId)).thenReturn(false);

        Employee result = employeeService.updateEmployee(employeeId, employee);

        assertNull(result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_ExistingIdAndInvalidEmployee_ThrowsException() {
         String employeeId = "123";
        Employee invalidEmployee = new Employee(employeeId, null, "invalid-email", "IT");

        when(employeeRepository.existsById(employeeId)).thenReturn(true);

        assertThrows(ConstraintViolationException.class, () -> employeeService.updateEmployee(employeeId, invalidEmployee));

        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, never()).save(any(Employee.class));

    }

    @Test
    void deleteEmployee_ExistingId_ReturnsTrue() {
        String employeeId = "123";
        when(employeeRepository.existsById(employeeId)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(employeeId);

        boolean result = employeeService.deleteEmployee(employeeId);

        assertTrue(result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    void deleteEmployee_NonExistingId_ReturnsFalse() {
        String employeeId = "123";
        when(employeeRepository.existsById(employeeId)).thenReturn(false);

        boolean result = employeeService.deleteEmployee(employeeId);

        assertFalse(result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, never()).deleteById(employeeId);
    }
}
```

```java