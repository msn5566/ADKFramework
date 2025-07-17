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
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be a 10-digit number")
    private String phoneNumber;
}
```

```java