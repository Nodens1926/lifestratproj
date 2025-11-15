package com.lifestrat.service;

import com.lifestrat.entity.LifeSphere;
import com.lifestrat.entity.User;
import com.lifestrat.repository.LifeSphereRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LifeSphereServiceTest {

    @Mock
    private LifeSphereRepository lifeSphereRepository;

    @InjectMocks
    private LifeSphereService lifeSphereService;

    private User testUser;
    private LifeSphere workSphere;
    private LifeSphere healthSphere;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        workSphere = new LifeSphere();
        workSphere.setId(1L);
        workSphere.setName("Work");
        workSphere.setColor("#FF6B6B");
        workSphere.setUser(testUser);

        healthSphere = new LifeSphere();
        healthSphere.setId(2L);
        healthSphere.setName("Health");
        healthSphere.setColor("#4ECDC4");
        healthSphere.setUser(testUser);
    }

    @Test
    void findAllByUserId_ShouldReturnUserLifeSpheres() {
        // Arrange
        List<LifeSphere> expectedSpheres = Arrays.asList(workSphere, healthSphere);
        when(lifeSphereRepository.findAllByUserId(1L)).thenReturn(expectedSpheres);

        // Act
        List<LifeSphere> result = lifeSphereService.findAllByUserId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
        verify(lifeSphereRepository).findAllByUserId(1L);
    }

    @Test
    void findByIdAndUserId_SphereExists_ShouldReturnSphere() {
        // Arrange
        List<LifeSphere> userSpheres = Arrays.asList(workSphere, healthSphere);
        when(lifeSphereRepository.findAllByUserId(1L)).thenReturn(userSpheres);

        // Act
        Optional<LifeSphere> result = lifeSphereService.findByIdAndUserId(1L, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(workSphere, result.get());
    }

    @Test
    void findByIdAndUserId_SphereNotFound_ShouldReturnEmpty() {
        // Arrange
        List<LifeSphere> userSpheres = Arrays.asList(workSphere, healthSphere);
        when(lifeSphereRepository.findAllByUserId(1L)).thenReturn(userSpheres);

        // Act
        Optional<LifeSphere> result = lifeSphereService.findByIdAndUserId(3L, 1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void create_ValidLifeSphere_ShouldSaveAndReturn() {
        // Arrange
        LifeSphere newSphere = new LifeSphere();
        newSphere.setName("New Sphere");
        newSphere.setColor("#FFFFFF");
        newSphere.setUser(testUser);

        when(lifeSphereRepository.findAllByUserId(1L)).thenReturn(Arrays.asList());
        when(lifeSphereRepository.save(newSphere)).thenReturn(newSphere);

        // Act
        LifeSphere result = lifeSphereService.create(newSphere, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("New Sphere", result.getName());
        verify(lifeSphereRepository).save(newSphere);
    }

    @Test
    void create_UserMismatch_ShouldThrowException() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setId(2L);

        LifeSphere newSphere = new LifeSphere();
        newSphere.setName("New Sphere");
        newSphere.setUser(wrongUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            lifeSphereService.create(newSphere, 1L);
        });
    }

    @Test
    void create_DuplicateName_ShouldThrowException() {
        // Arrange
        LifeSphere newSphere = new LifeSphere();
        newSphere.setName("Work");
        newSphere.setUser(testUser);

        when(lifeSphereRepository.findAllByUserId(1L)).thenReturn(Arrays.asList(workSphere));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            lifeSphereService.create(newSphere, 1L);
        });
    }

    @Test
    void update_ShouldUpdateAndSaveSphere() {
        // Arrange
        LifeSphere updatedSphere = new LifeSphere();
        updatedSphere.setName("Updated Work");
        updatedSphere.setColor("#000000");

        when(lifeSphereRepository.save(workSphere)).thenReturn(workSphere);

        // Act
        LifeSphere result = lifeSphereService.update(workSphere, updatedSphere);

        // Assert
        assertEquals("Updated Work", result.getName());
        assertEquals("#000000", result.getColor());
        verify(lifeSphereRepository).save(workSphere);
    }

    @Test
    void delete_ShouldDeleteSphere() {
        // Act
        lifeSphereService.delete(workSphere);

        // Assert
        verify(lifeSphereRepository).delete(workSphere);
    }

    @Test
    void createDefaultLifeSpheres_ShouldCreateAllSpheres() {
        // Arrange
        when(lifeSphereRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<LifeSphere> result = lifeSphereService.createDefaultLifeSpheres(testUser);

        // Assert
        assertEquals(6, result.size());
        assertEquals("Карьера", result.get(0).getName());
        assertEquals("Финансы", result.get(1).getName());
        verify(lifeSphereRepository).saveAll(any());
    }
}