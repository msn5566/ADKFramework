package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

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
    void createStudent_shouldSaveStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(new Student());

        assertNotNull(savedStudent);
        assertEquals(student, savedStudent);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void getStudent_shouldReturnStudent_whenStudentExists() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Student retrievedStudent = studentService.getStudent("1");

        assertNotNull(retrievedStudent);
        assertEquals(student, retrievedStudent);
        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void getStudent_shouldThrowException_whenStudentDoesNotExist() {
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> studentService.getStudent("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Student not found", exception.getReason());
        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void updateStudent_shouldUpdateStudent_whenStudentExists() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudentDetails = new Student();
        updatedStudentDetails.setName("Updated Name");
        updatedStudentDetails.setAddress("Updated Address");
        updatedStudentDetails.setMajor("Updated Major");

        Student updatedStudent = studentService.updateStudent("1", updatedStudentDetails);

        assertNotNull(updatedStudent);
        assertEquals("Updated Name", updatedStudent.getName());
        assertEquals("Updated Address", updatedStudent.getAddress());
        assertEquals("Updated Major", updatedStudent.getMajor());
        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void updateStudent_shouldThrowException_whenStudentDoesNotExist() {
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        Student updatedStudentDetails = new Student();
        updatedStudentDetails.setName("Updated Name");
        updatedStudentDetails.setAddress("Updated Address");
        updatedStudentDetails.setMajor("Updated Major");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> studentService.updateStudent("1", updatedStudentDetails));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Student not found", exception.getReason());
        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_shouldDeleteStudent_whenStudentExists() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        doNothing().when(studentRepository).deleteById("1");

        studentService.deleteStudent("1");

        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteStudent_shouldThrowException_whenStudentDoesNotExist() {
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> studentService.deleteStudent("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Student not found", exception.getReason());
        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, never()).deleteById("1");
    }

    @Test
    void getAllStudents_shouldReturnListOfStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> students = studentService.getAllStudents();

        assertNotNull(students);
        assertEquals(1, students.size());
        assertEquals(student, students.get(0));
        verify(studentRepository, times(1)).findAll();
    }
}
```

```java