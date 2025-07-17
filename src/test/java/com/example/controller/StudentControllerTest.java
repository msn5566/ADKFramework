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

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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
        student.setName("Jane Doe");
        student.setMajor("Computer Science");
        student.setEmail("jane.doe@example.com");
        student.setContactNumber("987-654-3210");
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
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(studentService, times(1)).createStudent(any(Student.class));
    }

    @Test
    void getStudentById_ExistingId_ReturnsOk() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(studentService, times(1)).getStudentById("1");
    }

    @Test
    void getStudentById_NonExistingId_ReturnsNotFound() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/students/2"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById("2");
    }

    @Test
    void getAllStudents_StudentsExist_ReturnsOk() throws Exception {
        when(studentService.getAllStudents()).thenReturn(Collections.singletonList(student));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"));

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    void updateStudent_ExistingIdAndValidInput_ReturnsOk() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));
        when(studentService.updateStudent("1", student)).thenReturn(student);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(studentService, times(1)).getStudentById("1");
        verify(studentService, times(1)).updateStudent("1", student);
    }

    @Test
    void updateStudent_NonExistingId_ReturnsNotFound() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/students/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById("2");
        verify(studentService, never()).updateStudent(anyString(), any(Student.class));
    }

    @Test
    void deleteStudent_ExistingId_ReturnsNoContent() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(Optional.of(student));
        doNothing().when(studentService).deleteStudent("1");

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).getStudentById("1");
        verify(studentService, times(1)).deleteStudent("1");
    }

    @Test
    void deleteStudent_NonExistingId_ReturnsNotFound() throws Exception {
        when(studentService.getStudentById("2")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/students/2"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentById("2");
        verify(studentService, never()).deleteStudent(anyString());
    }
}