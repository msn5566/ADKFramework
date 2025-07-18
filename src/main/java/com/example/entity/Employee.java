package com.example.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Data
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    private String department;
}
```

```java