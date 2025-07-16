package com.example.userregistration.service;

import com.example.userregistration.model.User;
import com.example.userregistration.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testRegisterUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        doNothing().when(userRepository).save(user);

        userService.registerUser(user);

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testRegisterUser_emailAlreadyExists() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(user);
    }
}
```