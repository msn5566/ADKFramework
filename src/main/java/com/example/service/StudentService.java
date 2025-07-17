package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

    public Student updateStudent(String id, @Valid Student studentDetails) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));

        student.setName(studentDetails.getName());
        student.setMajor(studentDetails.getMajor());

        return studentRepository.save(student);
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }
}
```

```java