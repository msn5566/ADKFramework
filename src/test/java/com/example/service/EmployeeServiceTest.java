package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

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

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890"));
        employees.add(new Employee(2L, "Jane", "Smith", "jane.smith@example.com", 60000.0, "987-654-3210"));

        Mockito.when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee_whenEmployeeExists() {
        Employee employee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertEquals("John", result.getFirstName());
    }

    @Test
    void getEmployeeById_shouldThrowException_whenEmployeeDoesNotExist() {
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    @Test
    void createEmployee_shouldReturnSavedEmployee() {
        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");
        Employee savedEmployee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");

        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(employee);

        assertEquals("John", result.getFirstName());
        assertEquals(1L, result.getId());
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee_whenEmployeeExists() {
        Employee existingEmployee = new Employee(1L, "John", "Doe", "john.doe@example.com", 50000.0, "123-456-7890");
        Employee updatedEmployee = new Employee(1L, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0, "000-000-0000");

        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        Mockito.when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        Employee result = employeeService.updateEmployee(1L, updatedEmployee);

        assertEquals("UpdatedJohn", result.getFirstName());
        assertEquals("UpdatedDoe", result.getLastName());
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        Employee updatedEmployee = new Employee(1L, "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", 55000.0, "000-000-0000");
        Mockito.when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(1L, updatedEmployee));
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenEmployeeExists() {
       Mockito.when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        Mockito.verify(employeeRepository, Mockito.times(1)).deleteById(1L);
    }

     @Test
    void deleteEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        Mockito.when(employeeRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(1L));
    }
}
```