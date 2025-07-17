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
        employee1 = new Employee(1L, "John", "Doe", "john.doe@example.com", "Software Engineer");
        employee2 = new Employee(2L, "Jane", "Smith", "jane.smith@example.com", "Data Scientist");
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee savedEmployee = employeeService.createEmployee(employee1);

        assertNotNull(savedEmployee);
        assertEquals(employee1.getFirstName(), savedEmployee.getFirstName());
        verify(employeeRepository, times(1)).save(employee1);
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));

        Employee retrievedEmployee = employeeService.getEmployeeById(1L);

        assertNotNull(retrievedEmployee);
        assertEquals(employee1.getId(), retrievedEmployee.getId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(1L));
        verify(employeeRepository, times(1)).findById(1L);
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
    void updateEmployee_ExistingIdAndValidEmployee_ReturnsUpdatedEmployee() {
        Employee existingEmployee = new Employee(1L, "OldJohn", "OldDoe", "old.john.doe@example.com", "Old Tester");
        Employee updatedEmployeeDetails = new Employee(null, "NewJohn", "NewDoe", "new.john.doe@example.com", "New Developer");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee updatedEmployee = employeeService.updateEmployee(1L, updatedEmployeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals(1L, updatedEmployee.getId());
        assertEquals("NewJohn", updatedEmployee.getFirstName());
        assertEquals("NewDoe", updatedEmployee.getLastName());
        assertEquals("new.john.doe@example.com", updatedEmployee.getEmail());
        assertEquals("New Developer", updatedEmployee.getJobTitle());

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(existingEmployee); //Verify that the *existing* employee object is saved.
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(1L, employee1));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        doNothing().when(employeeRepository).delete(employee1);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).delete(employee1);
    }

    @Test
    void deleteEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }
}
```