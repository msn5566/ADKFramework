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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
        student.setName("Alice Smith");
        student.setMajor("Computer Science");
        student.setGpa(4.0);
    }

    @Test
    void createStudentTest() throws Exception {
        when(studentService.createStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void getStudentByIdTest() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));

        mockMvc.perform(MockMvcRequestBuilders.get("/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void getStudentByIdNotFoundTest() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/students/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStudentTest() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));
        when(studentService.updateStudent("1", student)).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.put("/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void updateStudentNotFoundTest() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put("/students/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudentTest() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));
        doNothing().when(studentService).deleteStudent("1");

        mockMvc.perform(MockMvcRequestBuilders.delete("/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudentNotFoundTest() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/students/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStudentsTest() throws Exception {
        List<Student> students = Arrays.asList(student, new Student());
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(MockMvcRequestBuilders.get("/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Alice Smith"));
    }
}