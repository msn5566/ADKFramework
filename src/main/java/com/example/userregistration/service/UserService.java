package com.example.userregistration.service;

import com.example.userregistration.model.User;
import com.example.userregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void registerUser(User user) {
        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Save the user
        userRepository.save(user);
    }
}
```

```yaml
# File: src/main/resources/application.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/userdb  # Replace with your MongoDB URI
```

```markdown
# File: README.md

## User Registration Microservice

This microservice allows users to register with their email, password, and full name.

### Features

*   User registration with email, password, and full name.
*   Email validation (must be a valid email format).
*   Email uniqueness validation (email cannot already exist).
*   Password validation (must be at least 8 characters long).
*   Data stored in a MongoDB collection named "student".

### Technologies Used

*   Spring Boot
*   Spring Data MongoDB
*   Lombok
*   Validation API

### How to Run

1.  Make sure you have Docker installed.
2.  Run `docker-compose up --build` in the root directory of the project.
3.  The service will be accessible at `http://localhost:8080`.

### Endpoints

*   `POST /api/users/register`: Registers a new user.  Request body should be a JSON object with the following fields:
    *   `email`: User's email address.
    *   `password`: User's password.
    *   `fullName`: User's full name.

### Example Request

```json
{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User"
}
```
```

```dockerfile
# File: Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```yaml
# File: docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/userdb

  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

volumes:
  mongodb_data:
```

```java