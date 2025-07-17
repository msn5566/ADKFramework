package com.example.employeemanagement.service;

import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.repository.EmployeeRepository;
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

    private Employee employee;

    @BeforeEach
    void setup() {
        employee = new Employee();
        employee.setId("1");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
    }

     @Test
    void createEmployee_success() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("John", savedEmployee.getFirstName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_success() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployeeById("1");

        assertNotNull(retrievedEmployee);
        assertEquals("John", retrievedEmployee.getFirstName());
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void getEmployeeById_notFound() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        Employee retrievedEmployee = employeeService.getEmployeeById("1");

        assertNull(retrievedEmployee);
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void updateEmployee_success() {
        when(employeeRepository.existsById("1")).thenReturn(true);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employee);

        assertNotNull(updatedEmployee);
        assertEquals("John", updatedEmployee.getFirstName());
        verify(employeeRepository, times(1)).existsById("1");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_notFound() {
        when(employeeRepository.existsById("1")).thenReturn(false);

        Employee updatedEmployee = employeeService.updateEmployee("1", employee);

        assertNull(updatedEmployee);
        verify(employeeRepository, times(1)).existsById("1");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_success() {
        doNothing().when(employeeRepository).deleteById("1");

        employeeService.deleteEmployee("1");

        verify(employeeRepository, times(1)).deleteById("1");
    }

    @Test
    void getAllEmployees_success() {
        List<Employee> employees = new ArrayList<>();
        employees.add(employee);

        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> retrievedEmployees = employeeService.getAllEmployees();

        assertNotNull(retrievedEmployees);
        assertEquals(1, retrievedEmployees.size());
        assertEquals("John", retrievedEmployees.get(0).getFirstName());
        verify(employeeRepository, times(1)).findAll();
    }

}
```