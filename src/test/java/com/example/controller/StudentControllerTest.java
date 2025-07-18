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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
        student = new Student();
        student.setId("1");
        student.setName("Test Student");
        student.setMajor("Computer Science");
    }

    @Test
    void createStudent_ValidInput_ReturnsCreated() throws Exception {
        when(studentService.createStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Student"))
                .andExpect(jsonPath("$.major").value("Computer Science"));
    }

    @Test
    void getStudentById_ExistingId_ReturnsOk() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Student"))
                .andExpect(jsonPath("$.major").value("Computer Science"));
    }

    @Test
    void updateStudent_ExistingId_ReturnsOk() throws Exception {
        when(studentService.updateStudent("1", student)).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Student"))
                .andExpect(jsonPath("$.major").value("Computer Science"));
    }

    @Test
    void deleteStudent_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllStudents_ReturnsOk() throws Exception {
        List<Student> students = Arrays.asList(student);
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Test Student"))
                .andExpect(jsonPath("$[0].major").value("Computer Science"));
    }
}
```

```java