package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        employee = new Employee("1", "John Doe", "IT");
    }

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertEquals("1", savedEmployee.getId());
        assertEquals("John Doe", savedEmployee.getName());
        assertEquals("IT", savedEmployee.getDepartment());

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

        assertTrue(retrievedEmployee.isEmpty());

        verify(employeeRepository, times(1)).findById("2");
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> employees = employeeService.getAllEmployees();

        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getName());

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        Employee employeeDetails = new Employee(null, "Updated Name", "Updated Dept");
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employeeDetails);

        assertEquals("Updated Name", updatedEmployee.getName());
        assertEquals("Updated Dept", updatedEmployee.getDepartment());
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsException() {
        Employee employeeDetails = new Employee(null, "Updated Name", "Updated Dept");
        when(employeeRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee("2", employeeDetails));
        verify(employeeRepository, times(1)).findById("2");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        doNothing().when(employeeRepository).deleteById("1");

        employeeService.deleteEmployee("1");

        verify(employeeRepository, times(1)).deleteById("1");
    }
}
```

```java