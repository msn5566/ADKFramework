package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Get all employees")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of employee",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return new ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        return new ResponseEntity<>(employeeService.createEmployee(employee), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        return new ResponseEntity<>(employeeService.updateEmployee(id, employee), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
```

```java