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
        student = new Student("1", "Alice Smith", "Computer Science");
    }

    @Test
    void createStudent_ValidInput_ReturnsSavedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(student);

        assertEquals("1", savedStudent.getId());
        assertEquals("Alice Smith", savedStudent.getName());
        assertEquals("Computer Science", savedStudent.getMajor());

        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void getStudentById_ExistingId_ReturnsStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Optional<Student> retrievedStudent = studentService.getStudentById("1");

        assertTrue(retrievedStudent.isPresent());
        assertEquals("Alice Smith", retrievedStudent.get().getName());

        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void getStudentById_NonExistingId_ReturnsEmptyOptional() {
        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        Optional<Student> retrievedStudent = studentService.getStudentById("2");

        assertTrue(retrievedStudent.isEmpty());

        verify(studentRepository, times(1)).findById("2");
    }

    @Test
    void getAllStudents_ReturnsListOfStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<Student> students = studentService.getAllStudents();

        assertEquals(1, students.size());
        assertEquals("Alice Smith", students.get(0).getName());

        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void updateStudent_ExistingId_ReturnsUpdatedStudent() {
        Student studentDetails = new Student(null, "Updated Name", "Updated Major");
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudent = studentService.updateStudent("1", studentDetails);

        assertEquals("Updated Name", updatedStudent.getName());
        assertEquals("Updated Major", updatedStudent.getMajor());
        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void updateStudent_NonExistingId_ThrowsException() {
        Student studentDetails = new Student(null, "Updated Name", "Updated Major");
        when(studentRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> studentService.updateStudent("2", studentDetails));
        verify(studentRepository, times(1)).findById("2");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        doNothing().when(studentRepository).deleteById("1");

        studentService.deleteStudent("1");

        verify(studentRepository, times(1)).deleteById("1");
    }
}
```