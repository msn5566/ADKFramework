package com.example;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import com.example.service.EmployeeService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
        employee1 = new Employee("1", "John Doe", "Developer", "IT");
        employee2 = new Employee("2", "Jane Smith", "Manager", "HR");
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee savedEmployee = employeeService.createEmployee(employee1);

        assertNotNull(savedEmployee);
        assertEquals(employee1.getId(), savedEmployee.getId());
        verify(employeeRepository, times(1)).save(employee1);
    }

    @Test
    void createEmployee_InvalidEmployee_ThrowsException() {
        Employee invalidEmployee = new Employee(); // Missing required fields

        assertThrows(ConstraintViolationException.class, () -> employeeService.createEmployee(invalidEmployee));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        String employeeId = "1";
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee1));

        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);

        assertTrue(employee.isPresent());
        assertEquals(employee1, employee.get());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsEmptyOptional() {
        String employeeId = "3";
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);

        assertFalse(employee.isPresent());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void updateEmployee_ExistingIdAndValidEmployee_ReturnsUpdatedEmployee() {
        String employeeId = "1";
        Employee updatedEmployee = new Employee(null, "Updated Name", "Updated Role", "Updated Department");
        Employee employeeWithId = new Employee(employeeId, "Updated Name", "Updated Role", "Updated Department");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeWithId);

        Employee result = employeeService.updateEmployee(employeeId, updatedEmployee);

        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("Updated Name", result.getName());
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistingId_ReturnsNull() {
        String employeeId = "3";
        Employee updatedEmployee = new Employee(null, "Updated Name", "Updated Role", "Updated Department");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        Employee result = employeeService.updateEmployee(employeeId, updatedEmployee);

        assertNull(result);
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_InvalidEmployee_ThrowsException() {
        String employeeId = "1";
        Employee invalidEmployee = new Employee(null, null, null, null);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee1));

        assertThrows(ConstraintViolationException.class, () -> employeeService.updateEmployee(employeeId, invalidEmployee));

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsTrue() {
        String employeeId = "1";
        when(employeeRepository.existsById(employeeId)).thenReturn(true);

        boolean result = employeeService.deleteEmployee(employeeId);

        assertTrue(result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    void deleteEmployee_NonExistingId_ReturnsFalse() {
        String employeeId = "3";
        when(employeeRepository.existsById(employeeId)).thenReturn(false);

        boolean result = employeeService.deleteEmployee(employeeId);

        assertFalse(result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, never()).deleteById(employeeId);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(employee1);
        employees.add(employee2);

        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertEquals(2, allEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }
}
```

```java