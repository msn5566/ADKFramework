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

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId("1");
        employee.setName("John Doe");
        employee.setDepartment("IT");
        employee.setEmail("john.doe@example.com");
        employee.setPhone("123-456-7890");
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("John Doe", savedEmployee.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployeeById("1");

        assertNotNull(retrievedEmployee);
        assertEquals("John Doe", retrievedEmployee.getName());
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById("1"));
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setDepartment("Updated Department");
        employeeDetails.setEmail("updated.email@example.com");
        employeeDetails.setPhone("999-999-9999");

        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("Updated Name", updatedEmployee.getName());
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        Employee employeeDetails = new Employee();
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee("1", employeeDetails));
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);

        employeeService.deleteEmployee("1");

        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void deleteEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee("1"));
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee, new Employee("2", "Jane Doe", "HR", "jane.doe@example.com", "098-765-4321"));
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertNotNull(allEmployees);
        assertEquals(2, allEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }
}
```