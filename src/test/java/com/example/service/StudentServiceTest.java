package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student = new Student();
        student.setId("1");
        student.setName("Alice Smith");
        student.setMajor("Computer Science");
        student.setGpa(4.0);
    }

    @Test
    void createStudentTest() {
        when(studentRepository.save(student)).thenReturn(student);
        Student savedStudent = studentService.createStudent(student);
        assertEquals(student, savedStudent);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void getStudentByIdTest() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        Optional<Student> foundStudent = studentService.getStudentById("1");
        assertTrue(foundStudent.isPresent());
        assertEquals(student, foundStudent.get());
        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void updateStudentTest() {
        when(studentRepository.save(student)).thenReturn(student);
        Student updatedStudent = studentService.updateStudent("1", student);
        assertEquals(student, updatedStudent);
        assertEquals("1", updatedStudent.getId());
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void deleteStudentTest() {
        studentService.deleteStudent("1");
        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void getAllStudentsTest() {
        List<Student> students = Arrays.asList(student, new Student());
        when(studentRepository.findAll()).thenReturn(students);
        List<Student> allStudents = studentService.getAllStudents();
        assertEquals(2, allStudents.size());
        verify(studentRepository, times(1)).findAll();
    }
}
```

```java