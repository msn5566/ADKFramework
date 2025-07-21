package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId("1");
        employee.setName("John Doe");
        employee.setDepartment("IT");
        employee.setSalary(50000.0);
    }

    @Test
    void createEmployeeTest() {
        when(employeeRepository.save(employee)).thenReturn(employee);
        Employee savedEmployee = employeeService.createEmployee(employee);
        assertEquals(employee, savedEmployee);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void getEmployeeByIdTest() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        Optional<Employee> foundEmployee = employeeService.getEmployeeById("1");
        assertTrue(foundEmployee.isPresent());
        assertEquals(employee, foundEmployee.get());
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void updateEmployeeTest() {
        when(employeeRepository.save(employee)).thenReturn(employee);
        Employee updatedEmployee = employeeService.updateEmployee("1", employee);
        assertEquals(employee, updatedEmployee);
        assertEquals("1", updatedEmployee.getId());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void deleteEmployeeTest() {
        employeeService.deleteEmployee("1");
        verify(employeeRepository, times(1)).deleteById("1");
    }

    @Test
    void getAllEmployeesTest() {
        List<Employee> employees = Arrays.asList(employee, new Employee());
        when(employeeRepository.findAll()).thenReturn(employees);
        List<Employee> allEmployees = employeeService.getAllEmployees();
        assertEquals(2, allEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }
}
```

```java