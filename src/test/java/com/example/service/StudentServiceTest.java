package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import jakarta.validation.ConstraintViolationException;
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
        student = new Student("1", "Jane Smith", "Computer Science");
    }

    @Test
    void createStudent_ValidStudent_ReturnsSavedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(student);

        assertNotNull(savedStudent);
        assertEquals("Jane Smith", savedStudent.getName());
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void createStudent_InvalidStudent_ThrowsException() {
        Student invalidStudent = new Student(); // name is blank
        invalidStudent.setMajor("Computer Science");
        assertThrows(ConstraintViolationException.class, () -> studentService.createStudent(invalidStudent));
    }

    @Test
    void getStudentById_ExistingId_ReturnsStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Optional<Student> retrievedStudent = studentService.getStudentById("1");

        assertTrue(retrievedStudent.isPresent());
        assertEquals("Jane Smith", retrievedStudent.get().getName());
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
        assertEquals("Jane Smith", updatedStudent.getName());
        assertEquals("1", updatedStudent.getId()); // Check if ID is set
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void updateStudent_InvalidStudent_ThrowsException() {
        Student invalidStudent = new Student();
        assertThrows(ConstraintViolationException.class, () -> studentService.updateStudent("1", invalidStudent));
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        doNothing().when(studentRepository).deleteById("1");

        studentService.deleteStudent("1");

        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void getAllStudents_ReturnsListOfStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> students = studentService.getAllStudents();

        assertNotNull(students);
        assertEquals(1, students.size());
        assertEquals("Jane Smith", students.get(0).getName());
        verify(studentRepository, times(1)).findAll();
    }
}