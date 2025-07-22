package com.generated.microservice.service;

import com.generated.microservice.entity.Student;
import com.generated.microservice.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
        student.setName("Jane Doe");
        student.setMajor("Computer Science");
        student.setStudentId("STU123");
    }

    @Test
    void createStudent_shouldReturnCreatedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student createdStudent = studentService.createStudent(student);

        assertEquals(student, createdStudent);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void getStudentById_shouldReturnStudent_whenStudentExists() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Student retrievedStudent = studentService.getStudentById("1");

        assertEquals(student, retrievedStudent);
        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void getStudentById_shouldThrowException_whenStudentDoesNotExist() {
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> studentService.getStudentById("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Student not found with id: 1", exception.getReason());
        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void updateStudent_shouldReturnUpdatedStudent_whenStudentExists() {
        Student studentDetails = new Student();
        studentDetails.setName("Updated Name");
        studentDetails.setMajor("Updated Major");
        studentDetails.setStudentId("UPD123");

        when(studentRepository.findById("1")).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudent = studentService.updateStudent("1", studentDetails);

        assertEquals("Updated Name", student.getName());
        assertEquals("Updated Major", student.getMajor());
        assertEquals("UPD123", student.getStudentId());
        assertEquals(student, updatedStudent);
        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void updateStudent_shouldThrowException_whenStudentDoesNotExist() {
        Student studentDetails = new Student();
        when(studentRepository.findById("1")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> studentService.updateStudent("1", studentDetails));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Student not found with id: 1", exception.getReason());
        verify(studentRepository, times(1)).findById("1");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_shouldDeleteStudent_whenStudentExists() {
        when(studentRepository.existsById("1")).thenReturn(true);
        doNothing().when(studentRepository).deleteById("1");

        studentService.deleteStudent("1");

        verify(studentRepository, times(1)).existsById("1");
        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteStudent_shouldThrowException_whenStudentDoesNotExist() {
        when(studentRepository.existsById("1")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> studentService.deleteStudent("1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Student not found with id: 1", exception.getReason());
        verify(studentRepository, times(1)).existsById("1");
        verify(studentRepository, never()).deleteById("1");
    }

    @Test
    void getAllStudents_shouldReturnListOfStudents() {
        List<Student> students = new ArrayList<>();
        students.add(student);
        when(studentRepository.findAll()).thenReturn(students);

        List<Student> retrievedStudents = studentService.getAllStudents();

        assertEquals(students, retrievedStudents);
        verify(studentRepository, times(1)).findAll();
    }
}