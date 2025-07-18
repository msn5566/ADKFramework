package com.example.controller;

import com.example.entity.Student;
import com.example.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentControllerTest {

    @InjectMocks
    private StudentController studentController;

    @Mock
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createStudent_ValidInput_ReturnsCreatedStudent() {
        Student student = new Student();
        student.setName("Alice");
        student.setMajor("Computer Science");

        Student createdStudent = new Student();
        createdStudent.setId("1");
        createdStudent.setName("Alice");
        createdStudent.setMajor("Computer Science");

        when(studentService.createStudent(student)).thenReturn(createdStudent);

        ResponseEntity<Student> response = studentController.createStudent(student);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdStudent, response.getBody());
        verify(studentService, times(1)).createStudent(student);
    }

    @Test
    void getStudent_ExistingId_ReturnsStudent() {
        String studentId = "1";
        Student student = new Student();
        student.setId(studentId);
        student.setName("Alice");
        student.setMajor("Computer Science");

        when(studentService.getStudent(studentId)).thenReturn(Optional.of(student));

        ResponseEntity<Student> response = studentController.getStudent(studentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(student, response.getBody());
        verify(studentService, times(1)).getStudent(studentId);
    }

    @Test
    void getStudent_NonExistingId_ReturnsNotFound() {
        String studentId = "1";
        when(studentService.getStudent(studentId)).thenReturn(Optional.empty());

        ResponseEntity<Student> response = studentController.getStudent(studentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).getStudent(studentId);
    }

    @Test
    void updateStudent_ExistingId_ReturnsUpdatedStudent() {
        String studentId = "1";
        Student existingStudent = new Student();
        existingStudent.setId(studentId);
        existingStudent.setName("Alice");
        existingStudent.setMajor("Computer Science");

        Student updatedStudentDetails = new Student();
        updatedStudentDetails.setName("Bob");
        updatedStudentDetails.setMajor("Physics");

        Student updatedStudent = new Student();
        updatedStudent.setId(studentId);
        updatedStudent.setName("Bob");
        updatedStudent.setMajor("Physics");

        when(studentService.getStudent(studentId)).thenReturn(Optional.of(existingStudent));
        when(studentService.updateStudent(studentId, updatedStudentDetails)).thenReturn(updatedStudent);

        ResponseEntity<Student> response = studentController.updateStudent(studentId, updatedStudentDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedStudent, response.getBody());
        verify(studentService, times(1)).getStudent(studentId);
        verify(studentService, times(1)).updateStudent(studentId, updatedStudentDetails);
    }

    @Test
    void updateStudent_NonExistingId_ReturnsNotFound() {
        String studentId = "1";
        Student updatedStudentDetails = new Student();
        updatedStudentDetails.setName("Bob");
        updatedStudentDetails.setMajor("Physics");

        when(studentService.getStudent(studentId)).thenReturn(Optional.empty());

        ResponseEntity<Student> response = studentController.updateStudent(studentId, updatedStudentDetails);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(studentService, times(1)).getStudent(studentId);
        verify(studentService, never()).updateStudent(studentId, updatedStudentDetails);
    }

    @Test
    void deleteStudent_ExistingId_ReturnsNoContent() {
        String studentId = "1";

        ResponseEntity<Void> response = studentController.deleteStudent(studentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(studentService, times(1)).deleteStudent(studentId);
    }

    @Test
    void getAllStudents_ReturnsListOfStudents() {
        List<Student> students = new ArrayList<>();
        Student student1 = new Student();
        student1.setId("1");
        student1.setName("Alice");
        student1.setMajor("Computer Science");
        students.add(student1);

        Student student2 = new Student();
        student2.setId("2");
        student2.setName("Bob");
        student2.setMajor("Physics");
        students.add(student2);

        when(studentService.getAllStudents()).thenReturn(students);

        ResponseEntity<List<Student>> response = studentController.getAllStudents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(students, response.getBody());
        verify(studentService, times(1)).getAllStudents();
    }
}
```

```java