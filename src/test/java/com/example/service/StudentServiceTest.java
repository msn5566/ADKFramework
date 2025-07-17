package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId("1");
        student.setName("Alice");
        student.setMajor("Computer Science");
        student.setGrade(12);
    }

    @Test
    void createStudent_ValidStudent_ReturnsSavedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(student);

        assertNotNull(savedStudent);
        assertEquals("1", savedStudent.getId());
        verify(studentRepository).save(student);
    }

    @Test
    void getStudentById_ExistingId_ReturnsStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Optional<Student> retrievedStudent = studentService.getStudentById("1");

        assertTrue(retrievedStudent.isPresent());
        assertEquals("Alice", retrievedStudent.get().getName());
        verify(studentRepository).findById("1");
    }

    @Test
    void getStudentById_NonExistingId_ReturnsEmptyOptional() {
        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Student> retrievedStudent = studentService.getStudentById("2");

        assertFalse(retrievedStudent.isPresent());
        verify(studentRepository).findById("2");
    }

    @Test
    void updateStudent_ExistingId_ReturnsUpdatedStudent() {
        Student studentDetails = new Student();
        studentDetails.setName("Updated Name");
        studentDetails.setMajor("Updated Major");
        studentDetails.setGrade(10);

        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudent = studentService.updateStudent("1", studentDetails);

        assertNotNull(updatedStudent);
        assertEquals("Updated Name", updatedStudent.getName());
        assertEquals("Updated Major", updatedStudent.getMajor());
        assertEquals(10, updatedStudent.getGrade());
        verify(studentRepository).findById("1");
        verify(studentRepository).save(student);
    }

    @Test
    void updateStudent_NonExistingId_ThrowsException() {
        Student studentDetails = new Student();
        studentDetails.setName("Updated Name");
        studentDetails.setMajor("Updated Major");
        studentDetails.setGrade(10);

        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.updateStudent("2", studentDetails));
        verify(studentRepository).findById("2");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        studentService.deleteStudent("1");

        verify(studentRepository).deleteById("1");
    }

    @Test
    void getAllStudents_ReturnsListOfStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> students = studentService.getAllStudents();

        assertNotNull(students);
        assertEquals(1, students.size());
        assertEquals("Alice", students.get(0).getName());
        verify(studentRepository).findAll();
    }
}
```