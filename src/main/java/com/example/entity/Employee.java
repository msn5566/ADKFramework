package com.example.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    private String address;
    private String department;
}
```

```java