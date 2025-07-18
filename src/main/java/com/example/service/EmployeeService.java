package com.example.service;

import com.example.entity.Employee;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee createEmployee(@Valid Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public Employee updateEmployee(String id, @Valid Employee employeeDetails) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setName(employeeDetails.getName());
        employee.setDepartment(employeeDetails.getDepartment());

        return employeeRepository.save(employee);
    }

    public void deleteEmployee(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employeeRepository.delete(employee);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
}
```

```java