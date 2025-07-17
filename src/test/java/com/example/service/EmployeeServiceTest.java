package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
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
        employee1 = new Employee(1L, "John Doe", "IT", 60000.0, "john.doe@example.com");
        employee2 = new Employee(2L, "Jane Doe", "HR", 50000.0, "jane.doe@example.com");
    }

    @Test
    void createEmployee_Success() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee savedEmployee = employeeService.createEmployee(employee1);

        assertNotNull(savedEmployee);
        assertEquals(employee1.getName(), savedEmployee.getName());
        verify(employeeRepository, times(1)).save(employee1);
    }

    @Test
    void getEmployeeById_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));

        Employee retrievedEmployee = employeeService.getEmployeeById(1L);

        assertNotNull(retrievedEmployee);
        assertEquals(employee1.getName(), retrievedEmployee.getName());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_NotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(1L));
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getAllEmployees_Success() {
        when(employeeRepository.findAll()).thenReturn(Arrays.asList(employee1, employee2));

        List<Employee> allEmployees = employeeService.getAllEmployees();

        assertNotNull(allEmployees);
        assertEquals(2, allEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_Success() {
        Employee existingEmployee = new Employee(1L, "Old Name", "Old Department", 40000.0, "old.email@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee updatedEmployee = employeeService.updateEmployee(1L, employee1);

        assertNotNull(updatedEmployee);
        assertEquals(employee1.getName(), updatedEmployee.getName());
        assertEquals(employee1.getDepartment(), updatedEmployee.getDepartment());
        assertEquals(employee1.getSalary(), updatedEmployee.getSalary());
        assertEquals(employee1.getEmail(), updatedEmployee.getEmail());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void updateEmployee_NotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(1L, employee1));
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEmployee_NotFound() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(1L));
        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, never()).deleteById(1L);
    }
}
```

```java