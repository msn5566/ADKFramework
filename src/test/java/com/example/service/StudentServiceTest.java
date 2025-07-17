package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student = new Student("1", "Alice Wonderland", "Computer Science", "alice@example.com");
    }

    @Test
    void createStudent_ValidStudent_ReturnsSavedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(student);

        assertNotNull(savedStudent);
        assertEquals("1", savedStudent.getId());
        assertEquals("Alice Wonderland", savedStudent.getName());

        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void getStudentById_ExistingId_ReturnsStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Optional<Student> retrievedStudent = studentService.getStudentById("1");

        assertTrue(retrievedStudent.isPresent());
        assertEquals("Alice Wonderland", retrievedStudent.get().getName());

        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void getStudentById_NonExistingId_ReturnsEmptyOptional() {
        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Student> retrievedStudent = studentService.getStudentById("2");

        assertFalse(retrievedStudent.isPresent());

        verify(studentRepository, times(1)).findById("2");
    }

    @Test
    void updateStudent_ExistingId_ReturnsUpdatedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudent = studentService.updateStudent("1", student);

        assertNotNull(updatedStudent);
        assertEquals("1", updatedStudent.getId());
        assertEquals("Alice Wonderland", updatedStudent.getName());

        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        studentService.deleteStudent("1");

        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void getAllStudents_ReturnsListOfStudents() {
        List<Student> students = Arrays.asList(student, new Student("2", "Bob The Builder", "Engineering", "bob@example.com"));
        when(studentRepository.findAll()).thenReturn(students);

        List<Student> retrievedStudents = studentService.getAllStudents();

        assertEquals(2, retrievedStudents.size());
        assertEquals("Alice Wonderland", retrievedStudents.get(0).getName());
        assertEquals("Bob The Builder", retrievedStudents.get(1).getName());

        verify(studentRepository, times(1)).findAll();
    }
}