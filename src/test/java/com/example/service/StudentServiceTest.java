package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @InjectMocks
    private StudentService studentService;

    @Mock
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createStudent_ValidInput_ReturnsSavedStudent() {
        Student student = new Student();
        student.setName("Alice");
        student.setMajor("Computer Science");

        Student savedStudent = new Student();
        savedStudent.setId("1");
        savedStudent.setName("Alice");
        savedStudent.setMajor("Computer Science");

        when(studentRepository.save(student)).thenReturn(savedStudent);

        Student result = studentService.createStudent(student);

        assertEquals(savedStudent, result);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void getStudent_ExistingId_ReturnsStudent() {
        String studentId = "1";
        Student student = new Student();
        student.setId(studentId);
        student.setName("Alice");
        student.setMajor("Computer Science");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        Optional<Student> result = studentService.getStudent(studentId);

        assertTrue(result.isPresent());
        assertEquals(student, result.get());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void getStudent_NonExistingId_ReturnsEmptyOptional() {
        String studentId = "1";
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        Optional<Student> result = studentService.getStudent(studentId);

        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void updateStudent_ExistingId_ReturnsUpdatedStudent() {
        String studentId = "1";
        Student student = new Student();
        student.setName("Bob");
        student.setMajor("Physics");

        Student updatedStudent = new Student();
        updatedStudent.setId(studentId);
        updatedStudent.setName("Bob");
        updatedStudent.setMajor("Physics");

        when(studentRepository.save(updatedStudent)).thenReturn(updatedStudent);

        Student result = studentService.updateStudent(studentId, student);

        assertEquals(updatedStudent, result);
        assertEquals(studentId, student.getId());
        verify(studentRepository, times(1)).save(updatedStudent);
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        String studentId = "1";

        studentService.deleteStudent(studentId);

        verify(studentRepository, times(1)).deleteById(studentId);
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

        when(studentRepository.findAll()).thenReturn(students);

        List<Student> result = studentService.getAllStudents();

        assertEquals(students, result);
        verify(studentRepository, times(1)).findAll();
    }
}