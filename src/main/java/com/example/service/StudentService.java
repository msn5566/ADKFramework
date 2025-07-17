package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class StudentService {

    private final StudentRepository studentRepository;

    public Student createStudent(@Valid Student student) {
        return studentRepository.save(student);
    }

    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student updateStudent(String id, @Valid Student student) {
        student.setId(id);
        return studentRepository.save(student);
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }
}
```

```java