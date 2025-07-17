package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private Validator validator;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId("1");
        student.setName("Jane Doe");
        student.setMajor("Computer Science");
        student.setEmail("jane.doe@example.com");
        student.setContactNumber("987-654-3210");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createStudent_ValidStudent_ReturnsSavedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student savedStudent = studentService.createStudent(student);

        assertNotNull(savedStudent);
        assertEquals(student.getId(), savedStudent.getId());
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void createStudent_InvalidStudent_ThrowsException() {
        Student invalidStudent = new Student();
        invalidStudent.setName(""); // Invalid: Name is empty
        invalidStudent.setMajor(""); // Invalid: Major is empty
        invalidStudent.setEmail("invalid-email");

        Set<ConstraintViolation<Student>> violations = validator.validate(invalidStudent);

        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size()); // Expecting 3 violations

    }

    @Test
    void getStudentById_ExistingId_ReturnsStudent() {
        when(studentRepository.findById("1")).thenReturn(Optional.of(student));

        Optional<Student> retrievedStudent = studentService.getStudentById("1");

        assertTrue(retrievedStudent.isPresent());
        assertEquals(student.getName(), retrievedStudent.get().getName());
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
    void getAllStudents_StudentsExist_ReturnsListOfStudents() {
        when(studentRepository.findAll()).thenReturn(Collections.singletonList(student));

        List<Student> studentList = studentService.getAllStudents();

        assertNotNull(studentList);
        assertEquals(1, studentList.size());
        assertEquals(student.getName(), studentList.get(0).getName());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getAllStudents_NoStudentsExist_ReturnsEmptyList() {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        List<Student> studentList = studentService.getAllStudents();

        assertNotNull(studentList);
        assertTrue(studentList.isEmpty());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void updateStudent_ExistingIdAndValidStudent_ReturnsUpdatedStudent() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updatedStudent = studentService.updateStudent("1", student);

        assertNotNull(updatedStudent);
        assertEquals("1", updatedStudent.getId());
        assertEquals(student.getName(), updatedStudent.getName());
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void deleteStudent_ExistingId_DeletesStudent() {
        String studentId = "1";

        studentService.deleteStudent(studentId);

        verify(studentRepository, times(1)).deleteById(studentId);
    }
}
```

```java