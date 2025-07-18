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
        employee1 = new Employee();
        employee1.setId(1L);
        employee1.setName("John Doe");
        employee1.setEmail("john.doe@example.com");
        employee1.setDepartment("IT");

        employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Jane Smith");
        employee2.setEmail("jane.smith@example.com");
        employee2.setDepartment("HR");
    }

    @Test
    void createEmployee_ValidEmployee_ReturnsSavedEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee1);

        Employee savedEmployee = employeeService.createEmployee(employee1);

        assertNotNull(savedEmployee);
        assertEquals(employee1.getId(), savedEmployee.getId());
        verify(employeeRepository, times(1)).save(employee1);
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));

        Employee retrievedEmployee = employeeService.getEmployeeById(1L);

        assertNotNull(retrievedEmployee);
        assertEquals(employee1.getId(), retrievedEmployee.getId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getEmployeeById_NonExistingId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> employeeService.getEmployeeById(1L));

        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void getAllEmployees_ReturnsListOfEmployees() {
        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> retrievedEmployees = employeeService.getAllEmployees();

        assertNotNull(retrievedEmployees);
        assertEquals(2, retrievedEmployees.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void updateEmployee_ExistingIdAndValidEmployee_ReturnsUpdatedEmployee() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setName("Original Name");
        existingEmployee.setEmail("original.email@example.com");
        existingEmployee.setDepartment("Original Department");

        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setEmail("updated.email@example.com");
        employeeDetails.setDepartment("Updated Department");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee updatedEmployee = employeeService.updateEmployee(1L, employeeDetails);

        assertNotNull(updatedEmployee);
        assertEquals("Updated Name", updatedEmployee.getName());
        assertEquals("updated.email@example.com", updatedEmployee.getEmail());
        assertEquals("Updated Department", updatedEmployee.getDepartment());

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void updateEmployee_NonExistingId_ThrowsEntityNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Employee employeeDetails = new Employee();
        employeeDetails.setName("Updated Name");
        employeeDetails.setEmail("updated.email@example.com");
        employeeDetails.setDepartment("Updated Department");

        assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(1L, employeeDetails));

        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_ExistingId_DeletesEmployee() {
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).deleteById(1L);
    }
}