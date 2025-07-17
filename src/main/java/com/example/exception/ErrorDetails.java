package com.example.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetails {

    private String message;
    private String details;
}
```

```yaml
# File: src/main/resources/application.yml
spring:
  application:
    name: employee-management
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:employee_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update # Use create-drop for initial setup, then update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml


server:
  port: 8080

# OpenAPI configuration
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
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
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>

		<dependency>
			<groupId>org.liquibase</groupId>
			<artifactId>liquibase-core</artifactId>
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

*   Create, Read, Update, and Delete (CRUD) operations for employees.
*   RESTful API endpoints.
*   Data validation.
*   PostgreSQL database integration.
*   API documentation using Swagger/OpenAPI.

## Technologies Used

*   Java 17
*   Spring Boot 3.x
*   Spring Data JPA
*   PostgreSQL
*   Lombok
*   Maven
*   Docker

## Getting Started

### Prerequisites

*   Java 17 or higher
*   Maven
*   Docker (optional)
*   PostgreSQL

### Running the Application

1.  Clone the repository.
2.  Navigate to the project directory.
3.  Build the application: `mvn clean install`
4.  Run the application: `mvn spring-boot:run`

### Running with Docker

1.  Build the Docker image: `docker build -t employee-management .`
2.  Run the Docker container using docker-compose (see docker-compose.yml): `docker-compose up`

### API Documentation

Access the API documentation at: `http://localhost:8080/swagger-ui.html`

## Configuration

The application is configured using `application.yml`. You can configure the database connection, server port, and other properties in this file. Environment variables are used for database configuration, as shown in the yml.

## Database Setup

Make sure you have a PostgreSQL database running. The default configuration in `application.yml` assumes a database named `employee_db` with username `postgres` and password `password`. Adjust the configuration based on your setup. Liquibase is used to manage database schema changes. The changelog file is located at `src/main/resources/db/changelog/db.changelog-master.yaml`.

## Environment Variables

The following environment variables are used:

*   `DB_HOST` (default: `localhost`)
*   `DB_PORT` (default: `5432`)
*   `DB_NAME` (default: `employee_db`)
*   `DB_USER` (default: `postgres`)
*   `DB_PASSWORD` (default: `password`)

## Testing
Execute unit tests using `mvn test`.
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
  db:
    image: postgres:16
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
      DB_USER: postgres
      DB_PASSWORD: password

volumes:
  db_data:
```

```yaml
# File: src/main/resources/db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: CodeGenAgent
      changes:
        - createTable:
            tableName: employee
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: first_name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: last_name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: email
                  type: VARCHAR(255)
              - column:
                  name: department
                  type: VARCHAR(255)
```


```java