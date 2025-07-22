package com.generated.microservice.controller;

import com.generated.microservice.entity.Student;
import com.generated.microservice.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        student.setStudentId("STU123");
    }

    @Test
    void createStudent_shouldReturnCreatedStudent() throws Exception {
        when(studentService.createStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.major").value("Computer Science"))
                .andExpect(jsonPath("$.studentId").value("STU123"));

        verify(studentService, times(1)).createStudent(any(Student.class));
    }

    @Test
    void getStudentById_shouldReturnStudent() throws Exception {
        when(studentService.getStudentById("1")).thenReturn(student);

        mockMvc.perform(get("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.major").value("Computer Science"))
                .andExpect(jsonPath("$.studentId").value("STU123"));

        verify(studentService, times(1)).getStudentById("1");
    }

    @Test
    void updateStudent_shouldReturnUpdatedStudent() throws Exception {
        Student updatedStudent = new Student();
        updatedStudent.setId("1");
        updatedStudent.setName("Updated Name");
        updatedStudent.setMajor("Updated Major");
        updatedStudent.setStudentId("UPD123");

        when(studentService.updateStudent(eq("1"), any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.major").value("Updated Major"))
                .andExpect(jsonPath("$.studentId").value("UPD123"));

        verify(studentService, times(1)).updateStudent(eq("1"), any(Student.class));
    }

    @Test
    void deleteStudent_shouldReturnNoContent() throws Exception {
        doNothing().when(studentService).deleteStudent("1");

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).deleteStudent("1");
    }

    @Test
    void getAllStudents_shouldReturnListOfStudents() throws Exception {
        List<Student> students = new ArrayList<>();
        students.add(student);

        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/api/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].major").value("Computer Science"))
                .andExpect(jsonPath("$[0].studentId").value("STU123"));

        verify(studentService, times(1)).getAllStudents();
    }
}