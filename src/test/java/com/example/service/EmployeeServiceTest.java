package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
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

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        Employee employee = new Employee(null, "John Doe", "Developer", "john.doe@example.com", "1234567890");
        Employee savedEmployee = new Employee(1L, "John Doe", "Developer", "john.doe@example.com", "1234567890");

        when(employeeRepository.save(employee)).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(employee);

        assertEquals(savedEmployee, result);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        Long employeeId = 1L;
        Employee employee = new Employee(employeeId, "John Doe", "Developer", "john.doe@example.com", "1234567890");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(employeeId);

        assertEquals(employee, result);
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsEntityNotFoundException() {
        Long employeeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "John Doe", "Developer", "john.doe@example.com", "1234567890"),
                new Employee(2L, "Jane Smith", "Manager", "jane.smith@example.com", "0987654321")
        );

        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(employees, result);
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        Long employeeId = 1L;
        Employee existingEmployee = new Employee(employeeId, "John Doe", "Developer", "john.doe@example.com", "1234567890");
        Employee updatedEmployee = new Employee(employeeId, "Updated Name", "Updated Job", "updated@example.com", "1122334455");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(updatedEmployee);

        Employee result = employeeService.updateEmployee(employeeId, updatedEmployee);

        assertEquals(updatedEmployee.getName(), result.getName());
        assertEquals(updatedEmployee.getJobTitle(), result.getJobTitle());
        assertEquals(updatedEmployee.getEmail(), result.getEmail());
        assertEquals(updatedEmployee.getPhoneNumber(), result.getPhoneNumber());
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsEntityNotFoundException() {
        Long employeeId = 1L;
        Employee updatedEmployee = new Employee(employeeId, "Updated Name", "Updated Job", "updated@example.com", "1122334455");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(employeeId, updatedEmployee));
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        Long employeeId = 1L;

        when(employeeRepository.existsById(employeeId)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(employeeId);

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    void deleteEmployee_NonExistingId_ThrowsEntityNotFoundException() {
        Long employeeId = 1L;

        when(employeeRepository.existsById(employeeId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(employeeId));
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(employeeRepository, never()).deleteById(any());
    }
}
```