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

import java.util.List;
import java.util.ArrayList;
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
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment("IT");
    }

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals(employee.getFirstName(), savedEmployee.getFirstName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployeeById(1L);

        assertNotNull(retrievedEmployee);
        assertEquals(employee.getFirstName(), retrievedEmployee.getFirstName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(1L));
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void updateEmployee_ExistingIdAndValidInput_ReturnsUpdatedEmployee() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setFirstName("OldFirstName");
        existingEmployee.setLastName("OldLastName");
        existingEmployee.setEmail("old@example.com");
        existingEmployee.setDepartment("OldDepartment");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        employee.setFirstName("NewFirstName");
        employee.setLastName("NewLastName");
        employee.setEmail("new@example.com");
        employee.setDepartment("NewDepartment");

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee(1L, employee);

        assertNotNull(updatedEmployee);
        assertEquals("NewFirstName", updatedEmployee.getFirstName());
        assertEquals("NewLastName", updatedEmployee.getLastName());
        assertEquals("new@example.com", updatedEmployee.getEmail());
        assertEquals("NewDepartment", updatedEmployee.getDepartment());

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployee(1L, employee));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).delete(employee);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).delete(employee);
    }

    @Test
    void deleteEmployee_NonExistingId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(employee);

        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertNotNull(allEmployees);
        assertEquals(1, allEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }
}
```