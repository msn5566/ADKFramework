package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
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

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(1L, "John Doe", "john.doe@example.com", "IT", 50000.0);
    }

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals(employee.getName(), savedEmployee.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void createEmployee_InvalidInput_ThrowsException() {
        Employee invalidEmployee = new Employee(1L, "", "invalid-email", "IT", -50000.0);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.createEmployee(invalidEmployee);
        });

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Optional<Employee> foundEmployee = employeeService.getEmployeeById(1L);

        assertTrue(foundEmployee.isPresent());
        assertEquals(employee.getName(), foundEmployee.get().getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsEmptyOptional() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Employee> foundEmployee = employeeService.getEmployeeById(1L);

        assertFalse(foundEmployee.isPresent());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee, new Employee(2L, "Jane Doe", "jane.doe@example.com", "HR", 60000.0));
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertEquals(2, allEmployees.size());
        assertEquals(employees.get(0).getName(), allEmployees.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingIdAndValidInput_ReturnsUpdatedEmployee() {
        Employee employeeDetails = new Employee(null, "Updated Name", "updated.email@example.com", "Finance", 70000.0);
        Employee existingEmployee = new Employee(1L, "John Doe", "john.doe@example.com", "IT", 50000.0);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(existingEmployee);

        Employee updatedEmployee = employeeService.updateEmployee(1L, employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals(employeeDetails.getName(), updatedEmployee.getName()); // Ensure name is updated
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistingId_ReturnsNull() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Employee employeeDetails = new Employee(null, "Updated Name", "updated.email@example.com", "Finance", 70000.0);
        Employee updatedEmployee = employeeService.updateEmployee(1L, employeeDetails);

        assertNull(updatedEmployee);
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

     @Test
    void updateEmployee_InvalidInput_ThrowsException() {
        Employee employeeDetails = new Employee(null, "", "invalid-email", "Finance", -70000.0);
        Employee existingEmployee = new Employee(1L, "John Doe", "john.doe@example.com", "IT", 50000.0);

         when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.updateEmployee(1L, employeeDetails);
        });

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }


    @Test
    void deleteEmployee_ExistingId_ReturnsTrue() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        boolean deleted = employeeService.deleteEmployee(1L);

        assertTrue(deleted);
        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEmployee_NonExistingId_ReturnsFalse() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        boolean deleted = employeeService.deleteEmployee(1L);

        assertFalse(deleted);
        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, never()).deleteById(1L);
    }
}
```