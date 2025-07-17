package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee("1", "John Doe", "IT", "john.doe@example.com");
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals("1", savedEmployee.getId());
        assertEquals("John Doe", savedEmployee.getName());

        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Optional<Employee> retrievedEmployee = employeeService.getEmployeeById("1");

        assertTrue(retrievedEmployee.isPresent());
        assertEquals("John Doe", retrievedEmployee.get().getName());

        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsEmptyOptional() {
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Employee> retrievedEmployee = employeeService.getEmployeeById("2");

        assertFalse(retrievedEmployee.isPresent());

        verify(employeeRepository, times(1)).findById("2");
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employee);

        assertNotNull(updatedEmployee);
        assertEquals("1", updatedEmployee.getId());
        assertEquals("John Doe", updatedEmployee.getName());

        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        employeeService.deleteEmployee("1");

        verify(employeeRepository, times(1)).deleteById("1");
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee, new Employee("2", "Jane Smith", "HR", "jane.smith@example.com"));
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> retrievedEmployees = employeeService.getAllEmployees();

        assertEquals(2, retrievedEmployees.size());
        assertEquals("John Doe", retrievedEmployees.get(0).getName());
        assertEquals("Jane Smith", retrievedEmployees.get(1).getName());

        verify(employeeRepository, times(1)).findAll();
    }
}
```

```java