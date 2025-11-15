package com.lifestrat.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private final String secretKey = "testSecretKey12345678901234567890123456789012345678901234567890";
    private final long expirationTime = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "expirationTime", expirationTime);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtService.generateToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void extractUsername_ShouldReturnUsernameFromToken() {
        // Arrange
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        // Arrange
        String token = jwtService.generateToken("testuser");

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_WithValidTokenAndUsername_ShouldReturnTrue() {
        // Arrange
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // Act
        boolean isValid = jwtService.validateToken(token, username);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithWrongUsername_ShouldReturnFalse() {
        // Arrange
        String username = "testuser";
        String token = jwtService.generateToken(username);

        // Act
        boolean isValid = jwtService.validateToken(token, "differentuser");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken, "testuser");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithoutUsername_ShouldValidateStructure() {
        // Arrange
        String token = jwtService.generateToken("testuser");

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        // Create an expired token by setting very short expiration
        ReflectionTestUtils.setField(jwtService, "expirationTime", 1L); // 1 ms
        String token = jwtService.generateToken("testuser");

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Reset expiration time
        ReflectionTestUtils.setField(jwtService, "expirationTime", expirationTime);

        // Act
        boolean isValid = jwtService.validateToken(token, "testuser");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getRemainingTime_ShouldReturnPositiveValue() {
        // Arrange
        String token = jwtService.generateToken("testuser");

        // Act
        long remainingTime = jwtService.getRemainingTime(token);

        // Assert
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= expirationTime);
    }

    @Test
    void getRemainingTime_WithInvalidToken_ShouldReturnZero() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        long remainingTime = jwtService.getRemainingTime(invalidToken);

        // Assert
        assertEquals(0, remainingTime);
    }
}