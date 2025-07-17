package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Validator validator;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId("1");
        employee.setName("John Doe");
        employee.setDesignation("Software Engineer");
        employee.setEmail("john.doe@example.com");
        employee.setContactNumber("123-456-7890");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNotNull(savedEmployee);
        assertEquals(employee.getId(), savedEmployee.getId());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void createEmployee_InvalidEmployee_ThrowsException() {
        Employee invalidEmployee = new Employee();
        invalidEmployee.setName(""); // Invalid: Name is empty
        invalidEmployee.setDesignation(""); // Invalid: Designation is empty
        invalidEmployee.setEmail("invalid-email");

        Set<ConstraintViolation<Employee>> violations = validator.validate(invalidEmployee);

        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size()); // Expecting 3 violations

    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Optional<Employee> retrievedEmployee = employeeService.getEmployeeById("1");

        assertTrue(retrievedEmployee.isPresent());
        assertEquals(employee.getName(), retrievedEmployee.get().getName());
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
    void getAllEmployees_EmployeesExist_ReturnsListOfEmployees() {
        when(employeeRepository.findAll()).thenReturn(Collections.singletonList(employee));

        List<Employee> employeeList = employeeService.getAllEmployees();

        assertNotNull(employeeList);
        assertEquals(1, employeeList.size());
        assertEquals(employee.getName(), employeeList.get(0).getName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void getAllEmployees_NoEmployeesExist_ReturnsEmptyList() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> employeeList = employeeService.getAllEmployees();

        assertNotNull(employeeList);
        assertTrue(employeeList.isEmpty());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingIdAndValidEmployee_ReturnsUpdatedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employee);

        assertNotNull(updatedEmployee);
        assertEquals("1", updatedEmployee.getId());
        assertEquals(employee.getName(), updatedEmployee.getName());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        String employeeId = "1";

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).deleteById(employeeId);
    }
}
```

```java