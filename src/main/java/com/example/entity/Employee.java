package com.example.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Designation cannot be empty")
    private String designation;

    @Email(message = "Email should be valid")
    private String email;

    private String contactNumber;
}
```

```java