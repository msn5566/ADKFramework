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

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

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
        student.setName("Jane Smith");
        student.setAddress("456 Oak Ave");
        student.setMajor("Computer Science");
    }

    @Test
    void createStudent_shouldCreateNewStudent() throws Exception {
        when(studentService.createStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Jane Smith")))
                .andExpect(jsonPath("$.address", is("456 Oak Ave")))
                .andExpect(jsonPath("$.major", is("Computer Science")));
    }

    @Test
    void getStudent_shouldReturnStudent() throws Exception {
        when(studentService.getStudent("1")).thenReturn(student);

        mockMvc.perform(get("/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Jane Smith")))
                .andExpect(jsonPath("$.address", is("456 Oak Ave")))
                .andExpect(jsonPath("$.major", is("Computer Science")));
    }

    @Test
    void updateStudent_shouldUpdateStudent() throws Exception {
        when(studentService.updateStudent("1", any(Student.class))).thenReturn(student);

        mockMvc.perform(put("/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Jane Smith")))
                .andExpect(jsonPath("$.address", is("456 Oak Ave")))
                .andExpect(jsonPath("$.major", is("Computer Science")));
    }

    @Test
    void deleteStudent_shouldDeleteStudent() throws Exception {
        mockMvc.perform(delete("/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllStudents_shouldReturnAllStudents() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(student));

        mockMvc.perform(get("/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].name", is("Jane Smith")))
                .andExpect(jsonPath("$[0].address", is("456 Oak Ave")))
                .andExpect(jsonPath("$[0].major", is("Computer Science")));
    }
}