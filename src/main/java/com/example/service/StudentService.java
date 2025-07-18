package com.example.service;

import com.example.entity.Student;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Student createStudent(@Valid Student student) {
        return studentRepository.save(student);
    }

    public Student getStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Student updateStudent(String id, @Valid Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        student.setName(studentDetails.getName());
        student.setMajor(studentDetails.getMajor());

        return studentRepository.save(student);
    }

    public void deleteStudent(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        studentRepository.delete(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}
```

```java