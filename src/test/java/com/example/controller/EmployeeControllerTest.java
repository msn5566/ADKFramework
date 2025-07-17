package com.example.controller;

import com.example.entity.Employee;
import com.example.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEmployee_ValidInput_ReturnsCreatedEmployee() throws Exception {
        Employee employee = new Employee(null, "John", "Doe", "john.doe@example.com", "IT");
        Employee createdEmployee = new Employee("123", "John", "Doe", "john.doe@example.com", "IT");

        when(employeeService.createEmployee(any(Employee.class))).thenReturn(createdEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getEmployeeById_ExistingId_ReturnsEmployee() throws Exception {
        Employee employee = new Employee("123", "John", "Doe", "john.doe@example.com", "IT");
        when(employeeService.getEmployeeById("123")).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getEmployeeById_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById("456")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/456"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEmployee_ExistingId_ReturnsUpdatedEmployee() throws Exception {
        Employee employee = new Employee("123", "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", "UpdatedIT");
        when(employeeService.getEmployeeById("123")).thenReturn(Optional.of(new Employee("123", "John", "Doe", "john.doe@example.com", "IT")));
        when(employeeService.updateEmployee("123", employee)).thenReturn(employee);

        mockMvc.perform(put("/api/employees/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.firstName").value("UpdatedJohn"));
    }

    @Test
    void updateEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        Employee employee = new Employee("123", "UpdatedJohn", "UpdatedDoe", "updated.john.doe@example.com", "UpdatedIT");
        when(employeeService.getEmployeeById("456")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/employees/456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_ExistingId_ReturnsNoContent() throws Exception {
        when(employeeService.getEmployeeById("123")).thenReturn(Optional.of(new Employee("123", "John", "Doe", "john.doe@example.com", "IT")));

        mockMvc.perform(delete("/api/employees/123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById("456")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/employees/456"))
                .andExpect(status().isNotFound());
    }
}
```

```xml
<!-- File: pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>employee-management</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>employee-management</name>
    <description>Employee Management Microservice</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.0.2</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.8.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

```dockerfile
# File: Dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```yaml
# File: .github/workflows/main.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean install -DskipTests
  test:
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Tests with Maven
        run: mvn test

  docker:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v3
      - name: Log in to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v3
        with:
          context: .
          push: true
          tags: your-dockerhub-username/employee-management:${{ github.sha }}
```


```java