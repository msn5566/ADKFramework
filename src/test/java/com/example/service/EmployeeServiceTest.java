package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.validation.ConstraintViolationException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com", "IT");
        Employee savedEmployee = new Employee("123", "John", "Doe", "john.doe@example.com", "IT");

        when(employeeRepository.save(employee)).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(employee);

        assertEquals(savedEmployee, result);
    }

    @Test
    void createEmployee_InvalidInput_ThrowsException() {
        Employee employee = new Employee(null, null, null, null, "IT");

        assertThrows(ConstraintViolationException.class, () -> employeeService.createEmployee(employee));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        Employee employee = new Employee("123", "John", "Doe", "john.doe@example.com", "IT");
        when(employeeRepository.findById("123")).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployeeById("123");

        assertTrue(result.isPresent());
        assertEquals(employee, result.get());
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsEmptyOptional() {
        when(employeeRepository.findById("456")).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById("456");

        assertFalse(result.isPresent());
    }

    @Test
    void updateEmployee_ExistingIdAndValidInput_ReturnsUpdatedEmployee() {
        Employee employee = new Employee("123", "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", "UpdatedIT");
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee result = employeeService.updateEmployee("123", employee);

        assertEquals(employee, result);
        assertEquals("123", result.getId());
    }

     @Test
    void updateEmployee_InvalidInput_ThrowsException() {
        Employee employee = new Employee("123", null, null, null, "IT");

        assertThrows(ConstraintViolationException.class, () -> employeeService.updateEmployee("123", employee));
    }


    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        String employeeId = "123";

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository).deleteById(employeeId);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(
                new Employee("1", "John", "Doe", "john.doe@example.com", "IT"),
                new Employee("2", "Jane", "Smith", "jane.smith@example.com", "HR")
        );
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(employees.size(), result.size());
        assertEquals(employees, result);
    }
}
```