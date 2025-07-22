package com.generated.microservice.service;

import com.generated.microservice.entity.Employee;
import com.generated.microservice.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId("1");
        employee.setName("John Doe");
        employee.setDepartment("IT");
        employee.setEmployeeId("EMP123");
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee createdEmployee = employeeService.createEmployee(employee);

        assertEquals(employee, createdEmployee);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void getEmployeeById_shouldReturnEmployee_whenEmployeeExists() {
        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));

        Employee retrievedEmployee = employeeService.getEmployeeById("1");

        assertEquals(employee, retrievedEmployee);
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void getEmployeeById_shouldThrowException_whenEmployeeDoesNotExist() {
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> employeeService.getEmployeeById("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Employee not found with id: 1", exception.getReason());
        verify(employeeRepository, times(1)).findById("1");
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee_whenEmployeeExists() {
        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setDepartment("Updated Department");
        employeeDetails.setEmployeeId("UPD123");

        when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.updateEmployee("1", employeeDetails);

        assertEquals("Updated Name", employee.getName());
        assertEquals("Updated Department", employee.getDepartment());
        assertEquals("UPD123", employee.getEmployeeId());
        assertEquals(employee, updatedEmployee);
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        Employee employeeDetails = new Employee();
        when(employeeRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> employeeService.updateEmployee("1", employeeDetails));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Employee not found with id: 1", exception.getReason());
        verify(employeeRepository, times(1)).findById("1");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenEmployeeExists() {
        when(employeeRepository.existsById("1")).thenReturn(true);
        doNothing().when(employeeRepository).deleteById("1");

        employeeService.deleteEmployee("1");

        verify(employeeRepository, times(1)).existsById("1");
        verify(employeeRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteEmployee_shouldThrowException_whenEmployeeDoesNotExist() {
        when(employeeRepository.existsById("1")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> employeeService.deleteEmployee("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Employee not found with id: 1", exception.getReason());
        verify(employeeRepository, times(1)).existsById("1");
        verify(employeeRepository, never()).deleteById("1");
    }

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(employee);
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> retrievedEmployees = employeeService.getAllEmployees();

        assertEquals(employees, retrievedEmployees);
        verify(employeeRepository, times(1)).findAll();
    }
}