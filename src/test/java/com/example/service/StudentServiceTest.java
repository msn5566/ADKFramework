package com.example.service;

import com.example.entity.Student;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
        student.setName("Test Student");
        student.setMajor("Computer Science");
    }

    @Test
    void createStudent_ValidInput_ReturnsSavedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(student);

        assertNotNull(savedStudent);
        assertEquals("1", savedStudent.getId());
        assertEquals("Test Student", savedStudent.getName());
        assertEquals("Computer Science", savedStudent.getMajor());
    }

    @Test
    void getStudentById_ExistingId_ReturnsStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Student retrievedStudent = studentService.getStudentById("1");

        assertNotNull(retrievedStudent);
        assertEquals("1", retrievedStudent.getId());
        assertEquals("Test Student", retrievedStudent.getName());
        assertEquals("Computer Science", retrievedStudent.getMajor());
    }

    @Test
    void getStudentById_NonExistingId_ThrowsResourceNotFoundException() {
        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById("2"));
    }

    @Test
    void updateStudent_ExistingId_ReturnsUpdatedStudent() {
        Student studentDetails = new Student();
        studentDetails.setName("Updated Student");
        studentDetails.setMajor("Electrical Engineering");

        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudent = studentService.updateStudent("1", studentDetails);

        assertNotNull(updatedStudent);
        assertEquals("Updated Student", updatedStudent.getName());
        assertEquals("Electrical Engineering", updatedStudent.getMajor());
    }

    @Test
    void updateStudent_NonExistingId_ThrowsResourceNotFoundException() {
        Student studentDetails = new Student();
        studentDetails.setName("Updated Student");
        studentDetails.setMajor("Electrical Engineering");

        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.updateStudent("2", studentDetails));
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        studentService.deleteStudent("1");

        verify(studentRepository).delete(student);
    }

    @Test
    void deleteStudent_NonExistingId_ThrowsResourceNotFoundException() {
        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent("2"));
    }

    @Test
    void getAllStudents_ReturnsListOfStudents() {
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAll()).thenReturn(students);

        List<Student> retrievedStudents = studentService.getAllStudents();

        assertNotNull(retrievedStudents);
        assertEquals(1, retrievedStudents.size());
        assertEquals("1", retrievedStudents.get(0).getId());
        assertEquals("Test Student", retrievedStudents.get(0).getName());
        assertEquals("Computer Science", retrievedStudents.get(0).getMajor());
    }
}