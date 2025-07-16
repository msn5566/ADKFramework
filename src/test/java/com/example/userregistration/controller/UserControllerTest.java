package com.example.userregistration.controller;

import com.example.userregistration.model.User;
import com.example.userregistration.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterUser_success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");

        doNothing().when(userService).registerUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    public void testRegisterUser_emailAlreadyExists() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");

        doThrow(new IllegalArgumentException("Email already exists")).when(userService).registerUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    public void testRegisterUser_invalidInput() throws Exception {
        // Missing email field
        User user = new User();
        user.setPassword("password123");
        user.setFullName("Test User");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()); // Expecting a validation error
    }

    @Test
    public void testRegisterUser_shortPassword() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("short");
        user.setFullName("Test User");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }
}
```

```java