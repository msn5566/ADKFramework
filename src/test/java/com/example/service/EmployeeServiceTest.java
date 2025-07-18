package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmployee_ValidInput_ReturnsSavedEmployee() {
        Employee employee = new Employee();
        employee.setName("John Doe");
        employee.setDepartment("IT");

        Employee savedEmployee = new Employee();
        savedEmployee.setId("1");
        savedEmployee.setName("John Doe");
        savedEmployee.setDepartment("IT");

        when(employeeRepository.save(employee)).thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(employee);

        assertEquals(savedEmployee, result);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void getEmployee_ExistingId_ReturnsEmployee() {
        String employeeId = "1";
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setName("John Doe");
        employee.setDepartment("IT");

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployee(employeeId);

        assertTrue(result.isPresent());
        assertEquals(employee, result.get());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void getEmployee_NonExistingId_ReturnsEmptyOptional() {
        String employeeId = "1";
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployee(employeeId);

        assertFalse(result.isPresent());
        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() {
        String employeeId = "1";
        Employee employee = new Employee();
        employee.setName("Jane Doe");
        employee.setDepartment("HR");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(employeeId);
        updatedEmployee.setName("Jane Doe");
        updatedEmployee.setDepartment("HR");


        when(employeeRepository.save(updatedEmployee)).thenReturn(updatedEmployee);

        Employee result = employeeService.updateEmployee(employeeId, employee);

        assertEquals(updatedEmployee, result);
        assertEquals(employeeId, employee.getId());
        verify(employeeRepository, times(1)).save(updatedEmployee);
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        String employeeId = "1";

        employeeService.deleteEmployee(employeeId);

        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        Employee employee1 = new Employee();
        employee1.setId("1");
        employee1.setName("John Doe");
        employee1.setDepartment("IT");
        employees.add(employee1);

        Employee employee2 = new Employee();
        employee2.setId("2");
        employee2.setName("Jane Doe");
        employee2.setDepartment("HR");
        employees.add(employee2);

        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(employees, result);
        verify(employeeRepository, times(1)).findAll();
    }
}
```

```java