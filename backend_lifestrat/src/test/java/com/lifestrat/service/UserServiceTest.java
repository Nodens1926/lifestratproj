package com.lifestrat.service;

import com.lifestrat.entity.User;
import com.lifestrat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findById_UserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_UserNotFound_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_UserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByEmail_UserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void existsByUsername_UserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        Boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail_UserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        Boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void save_ShouldSaveUser() {
        // Arrange
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        User result = userService.save(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_ValidData_ShouldCreateUser() {
        // Arrange
        String username = "newuser";
        String email = "new@example.com";
        String password = "plainPassword";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // Act
        User result = userService.createUser(username, email, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertNotNull(result.getPassword());
        assertNotEquals("plainPassword", result.getPassword()); // Password should be encoded
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateUsername_ShouldThrowException() {
        // Arrange
        String username = "existinguser";
        String email = "new@example.com";
        String password = "password";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(username, email, password);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ShouldThrowException() {
        // Arrange
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(username, email, password);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void delete_ShouldDeleteUser() {
        // Act
        userService.delete(testUser);

        // Assert
        verify(userRepository).delete(testUser);
    }

    @Test
    void checkPassword_CorrectPassword_ShouldReturnTrue() {
        // Arrange
        String rawPassword = "password";
        String encodedPassword = userService.encodePassword(rawPassword);

        // Act
        boolean result = userService.checkPassword(rawPassword, encodedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void checkPassword_WrongPassword_ShouldReturnFalse() {
        // Arrange
        String rawPassword = "password";
        String wrongPassword = "wrongpassword";
        String encodedPassword = userService.encodePassword(rawPassword);

        // Act
        boolean result = userService.checkPassword(wrongPassword, encodedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void encodePassword_ShouldReturnEncodedPassword() {
        // Arrange
        String rawPassword = "testPassword";

        // Act
        String encodedPassword = userService.encodePassword(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$")); // BCrypt pattern
    }
}