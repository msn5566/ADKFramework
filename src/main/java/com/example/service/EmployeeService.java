package com.example.service;

import com.example.entity.Employee;
import com.example.repository.EmployeeRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee createEmployee(@Valid Employee employee) {
        return employeeRepository.save(employee);
    }

    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

    public Employee updateEmployee(String id, @Valid Employee employee) {
        employee.setId(id); // Ensure the ID is set for updating
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
}
```

```java