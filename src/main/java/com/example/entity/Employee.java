package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot be longer than 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot be longer than 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be a positive value")
    private Double salary;

    @Pattern(regexp = "^\\d{3}-\\d{3}-\\d{4}$", message = "Phone number must be in the format XXX-XXX-XXXX")
    private String phoneNumber;
}
```

```java