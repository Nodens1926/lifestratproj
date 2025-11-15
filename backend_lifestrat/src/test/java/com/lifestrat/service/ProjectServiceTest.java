package com.lifestrat.service;

import com.lifestrat.entity.Project;
import com.lifestrat.entity.LifeSphere;
import com.lifestrat.entity.User;
import com.lifestrat.entity.Priority;
import com.lifestrat.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private LifeSphere workSphere;
    private Project project1;
    private Project project2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        workSphere = new LifeSphere();
        workSphere.setId(1L);
        workSphere.setName("Work");
        workSphere.setUser(testUser);

        project1 = new Project();
        project1.setId(1L);
        project1.setTitle("Project 1");
        project1.setUser(testUser);
        project1.setLifeSphere(workSphere);
        project1.setPriority(Priority.HIGH);
        project1.setDeadline(LocalDate.now().plusDays(30));

        project2 = new Project();
        project2.setId(2L);
        project2.setTitle("Project 2");
        project2.setUser(testUser);
        project2.setLifeSphere(workSphere);
        project2.setPriority(Priority.MEDIUM);
        project2.setDeadline(LocalDate.now().plusDays(15));
    }

    @Test
    void findAllByUserId_ShouldReturnUserProjects() {
        // Arrange
        List<Project> expectedProjects = Arrays.asList(project1, project2);
        when(projectRepository.findAllByUserId(1L)).thenReturn(expectedProjects);

        // Act
        List<Project> result = projectService.findAllByUserId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getTitle());
        verify(projectRepository).findAllByUserId(1L);
    }

    @Test
    void findByIdAndUserId_ProjectExists_ShouldReturnProject() {
        // Arrange
        List<Project> userProjects = Arrays.asList(project1, project2);
        when(projectRepository.findAllByUserId(1L)).thenReturn(userProjects);

        // Act
        Optional<Project> result = projectService.findByIdAndUserId(1L, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(project1, result.get());
    }

    @Test
    void create_ValidProject_ShouldSaveAndReturn() {
        // Arrange
        Project newProject = new Project();
        newProject.setTitle("New Project");
        newProject.setUser(testUser);
        newProject.setLifeSphere(workSphere);
        newProject.setPriority(Priority.LOW);
        newProject.setDeadline(LocalDate.now().plusDays(10));

        when(projectRepository.findAllByUserId(1L)).thenReturn(Arrays.asList());
        when(projectRepository.save(newProject)).thenReturn(newProject);

        // Act
        Project result = projectService.create(newProject, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("New Project", result.getTitle());
        verify(projectRepository).save(newProject);
    }

    @Test
    void create_UserMismatch_ShouldThrowException() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setId(2L);

        Project newProject = new Project();
        newProject.setTitle("New Project");
        newProject.setUser(wrongUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.create(newProject, 1L);
        });
    }

    @Test
    void create_LifeSphereMismatch_ShouldThrowException() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setId(2L);
        LifeSphere wrongSphere = new LifeSphere();
        wrongSphere.setUser(wrongUser);

        Project newProject = new Project();
        newProject.setTitle("New Project");
        newProject.setUser(testUser);
        newProject.setLifeSphere(wrongSphere);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.create(newProject, 1L);
        });
    }

    @Test
    void create_DuplicateTitle_ShouldThrowException() {
        // Arrange
        Project newProject = new Project();
        newProject.setTitle("Project 1");
        newProject.setUser(testUser);
        newProject.setLifeSphere(workSphere);

        when(projectRepository.findAllByUserId(1L)).thenReturn(Arrays.asList(project1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.create(newProject, 1L);
        });
    }

    @Test
    void update_ShouldUpdateAndSaveProject() {
        // Arrange
        Project updatedProject = new Project();
        updatedProject.setTitle("Updated Project");
        updatedProject.setDescription("Updated description");
        updatedProject.setDeadline(LocalDate.now().plusDays(5));
        updatedProject.setPriority(Priority.CRITICAL);
        updatedProject.setLifeSphere(workSphere);

        when(projectRepository.save(project1)).thenReturn(project1);

        // Act
        Project result = projectService.update(project1, updatedProject);

        // Assert
        assertEquals("Updated Project", result.getTitle());
        verify(projectRepository).save(project1);
    }

    @Test
    void delete_ShouldDeleteProject() {
        // Act
        projectService.delete(project1);

        // Assert
        verify(projectRepository).delete(project1);
    }

    @Test
    void findAllByUserIdAndLifeSphereId_ShouldReturnFilteredProjects() {
        // Arrange
        List<Project> userProjects = Arrays.asList(project1, project2);
        when(projectRepository.findAllByUserId(1L)).thenReturn(userProjects);

        // Act
        List<Project> result = projectService.findAllByUserIdAndLifeSphereId(1L, 1L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getLifeSphere().getId().equals(1L)));
    }

    @Test
    void findOverdueProjectsByUserId_ShouldReturnOverdueProjects() {
        // Arrange
        Project overdueProject = new Project();
        overdueProject.setId(3L);
        overdueProject.setTitle("Overdue Project");
        overdueProject.setUser(testUser);
        overdueProject.setLifeSphere(workSphere);
        overdueProject.setDeadline(LocalDate.now().minusDays(1));

        List<Project> userProjects = Arrays.asList(project1, project2, overdueProject);
        when(projectRepository.findAllByUserId(1L)).thenReturn(userProjects);

        // Act
        List<Project> result = projectService.findOverdueProjectsByUserId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Overdue Project", result.get(0).getTitle());
    }
}