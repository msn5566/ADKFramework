package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management API")
                        .description("API for managing employee data")
                        .version("1.0"));
    }
}
```

```yaml
# File: src/main/resources/application.yml
spring:
  application:
    name: employee-management
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:employee_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update # Use create-drop for initial setup, then switch to update/validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  port: 8080

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
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
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
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
         <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
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

```markdown
# File: README.md

# Employee Management Microservice

This is a Spring Boot microservice for managing employee data.

## Features

-   Create, read, update, and delete employee records.
-   REST API endpoints for all operations.
-   Data validation.
-   PostgreSQL database integration.
-   API documentation using Swagger/OpenAPI.

## Technologies Used

-   Java 17
-   Spring Boot 3.x
-   Spring Data JPA
-   PostgreSQL
-   Lombok
-   Maven
-   Docker

## Getting Started

### Prerequisites

-   Java 17 or higher
-   Maven
-   Docker (optional)
-   PostgreSQL database

### Installation

1.  Clone the repository:

    ```bash
    git clone <repository_url>
    ```

2.  Navigate to the project directory:

    ```bash
    cd employee-management
    ```

3.  Build the project:

    ```bash
    mvn clean install
    ```

### Configuration

1.  Configure the PostgreSQL database connection in `src/main/resources/application.yml`.  Make sure to set DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, and DB_PASSWORD. If you're using Docker, those can be defined in the `docker-compose.yml` file.

### Running the Application

1.  Run the application using Spring Boot:

    ```bash
    mvn spring-boot:run
    ```

2.  Alternatively, run the application using Docker:

    ```bash
    docker-compose up --build
    ```

### API Documentation

Access the API documentation at: `http://localhost:8080/swagger-ui.html`

## Endpoints
* `POST /api/employees` - Create a new employee
* `GET /api/employees/{id}` - Get employee by ID
* `PUT /api/employees/{id}` - Update employee
* `DELETE /api/employees/{id}` - Delete employee
* `GET /api/employees` - Get all employees

## Database Setup
Flyway is configured to run migrations. The database schema will be created automatically upon application startup.

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
  db:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
      POSTGRES_DB: employee_db
    volumes:
      - db_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      DB_HOST: db
      DB_PORT: 5432
      DB_NAME: employee_db
      DB_USERNAME: postgres
      DB_PASSWORD: password

volumes:
  db_data:
```

```yaml
# File: .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches:
      - main

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
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Login to Docker Hub
        run: docker login -u ${{ secrets.DOCKER_USERNAME }} -p ${{ secrets.DOCKER_PASSWORD }}
      - name: Build the Docker image
        run: docker build . --file Dockerfile --tag ${{ secrets.DOCKER_USERNAME }}/employee-management:${GITHUB_SHA::7}
      - name: Push the Docker image
        run: docker push ${{ secrets.DOCKER_USERNAME }}/employee-management:${GITHUB_SHA::7}
```

```sql
-- File: src/main/resources/db/migration/V1__create_employee_table.sql
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department VARCHAR(255)
);
```

```java