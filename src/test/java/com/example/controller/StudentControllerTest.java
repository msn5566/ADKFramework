package com.example.controller;

import com.example.entity.Student;
import com.example.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("1", "Alice Wonderland", "Computer Science", "alice@example.com");
    }

    @Test
    void createStudent_ValidInput_ReturnsCreated() throws Exception {
        when(studentService.createStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"));

        verify(studentService, times(1)).createStudent(any(Student.class));
    }

    @Test
    void getStudentById_ExistingId_ReturnsOk() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"));

        verify(studentService, times(1)).getStudentById("1");
    }

    @Test
    void getStudentById_NonExistingId_ReturnsNotFound() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/students/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById("2");
    }

    @Test
    void updateStudent_ExistingId_ReturnsOk() throws Exception {
        when(studentService.updateStudent(eq("1"), any(Student.class))).thenReturn(student);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"));

        verify(studentService, times(1)).updateStudent(eq("1"), any(Student.class));
    }

    @Test
    void updateStudent_NonExistingId_ReturnsNotFound() throws Exception {
        when(studentService.updateStudent(eq("2"), any(Student.class))).thenThrow(new RuntimeException("Student not found"));

        mockMvc.perform(put("/api/students/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).updateStudent(eq("2"), any(Student.class));
    }

    @Test
    void deleteStudent_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).deleteStudent("1");
    }

    @Test
    void getAllStudents_ReturnsOk() throws Exception {
        List<Student> students = Arrays.asList(student, new Student("2", "Bob The Builder", "Engineering", "bob@example.com"));
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/api/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Alice Wonderland"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Bob The Builder"));

        verify(studentService, times(1)).getAllStudents();
    }
}
```

```java