package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "An unexpected error occurred");
        body.put("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
```

```yaml
# File: src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: user-registration
  data:
    mongodb:
      uri: mongodb://localhost:27017/userdb  # Replace with your MongoDB URI
      auto-index-creation: true
```

```markdown
# File: README.md
# User Registration Microservice

This microservice allows users to register with their email, password, and full name.

## Features

*   User registration with email, password, and full name.
*   Email validation (format and uniqueness).
*   Password validation (minimum length of 8 characters).
*   Data storage in MongoDB.

## Technologies Used

*   Spring Boot
*   Spring Data MongoDB
*   Lombok
*   Validation API
*   Maven

## Getting Started

### Prerequisites

*   Java 17 or higher
*   Maven
*   Docker (optional)
*   MongoDB

### Running the Application

1.  Clone the repository.
2.  Build the application using Maven: `mvn clean install`
3.  Run the application: `mvn spring-boot:run`

The application will be running on port 8080.

### Docker

1.  Build the Docker image: `docker build -t user-registration .`
2.  Run the Docker container using docker-compose (see docker-compose.yml).

## Endpoints

*   `POST /api/users/register`: Registers a new user.  Expects a JSON body with email, password, and fullName.
```

```dockerfile
# File: Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```yaml
# File: docker-compose.yml
version: "3.8"
services:
  user-registration:
    image: user-registration
    ports:
      - "8080:8080"
    depends_on:
      - mongo

  mongo:
    image: mongo:latest
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: userdb
    volumes:
      - mongo_data:/data/db

volumes:
  mongo_data:
```

**Explanation and Key improvements:**

*   **Clear File Structure:**  Each file is explicitly marked with its path, making it easy to understand the project structure.
*   **Complete `pom.xml`:** Includes all necessary dependencies (Spring Web, Spring Data MongoDB, Lombok, Validation, Mapstruct) and the Maven build configuration for annotation processing (Lombok and Mapstruct). Correctly configured Maven Compiler Plugin to work with Lombok and Mapstruct.
*   **Entity with Validation:** The `User` entity includes `@NotBlank`, `@Email`, and `@Size` annotations for input validation.  The `@Document` annotation specifies the MongoDB collection name ("student").
*   **Repository Interface:**  `UserRepository` extends `MongoRepository` for easy data access. It also includes `findByEmail` method to check for existing users.
*   **Service Layer:** The `UserService` handles the business logic, including checking for existing emails and saving the user. It now correctly throws an exception when the email already exists.  It uses constructor injection via `@RequiredArgsConstructor` for cleaner dependency injection. Uses `@Valid` annotation to trigger validation before saving.
*   **Controller with REST Endpoint:** The `UserController` exposes a `POST /api/users/register` endpoint for user registration.  It receives the `User` object in the request body. Uses `@Valid` to trigger validation.
*   **Exception Handling:** Includes `EmailAlreadyExistsException` and `GlobalExceptionHandler` to handle email duplication and validation errors gracefully. The `GlobalExceptionHandler` now returns a proper HTTP status code (409 Conflict) for email already exists errors and handles general exceptions.  Also includes handler for `MethodArgumentNotValidException` to return validation error messages to the client.
*   **application.yml:**  Configures the Spring Boot application, including the MongoDB connection URI and port.
*   **README.md:**  A basic README file explaining the microservice and how to run it.
*   **Dockerfile:** A Dockerfile to containerize the application.
*   **docker-compose.yml:** A `docker-compose.yml` file to run the application and MongoDB using Docker.
*   **Lombok:**  Using `@Data`, `@RequiredArgsConstructor` annotations for concise code.
*   **MongoDB URI:** The `application.yml` now includes a MongoDB URI that you *must* replace with your actual MongoDB connection string.  Defaults to a local instance.
*   **Error Handling:**  Added global exception handling to return meaningful error responses to the client, including validation errors.
*   **Auto Index Creation**: Enabled in application.yml

**How to use:**

1.  **MongoDB Setup:** Make sure you have MongoDB running (either locally or in a Docker container).  If using Docker, the provided `docker-compose.yml` file will set it up for you.
2.  **Configuration:**  Update the `application.yml` file with your MongoDB connection string.
3.  **Build and Run:**  Build the project using `mvn clean install` and run it using `mvn spring-boot:run`.  Alternatively, build the Docker image and run the application using `docker-compose up`.
4.  **Test:**  Send a `POST` request to `/api/users/register` with a JSON payload containing the user's email, password, and full name. Example:

```json
{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User"
}
```

This comprehensive example provides a solid foundation for your user registration microservice. Remember to adapt the MongoDB URI in `application.yml` and consider adding more robust error handling and logging in a production environment.


```java