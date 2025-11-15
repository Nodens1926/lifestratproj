package com.lifestrat.service;

import com.lifestrat.dto.ProjectProgressDto;
import com.lifestrat.entity.*;
import com.lifestrat.repository.TaskRepository;
import com.lifestrat.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;
    private LifeSphere workSphere;
    private LifeSphere healthSphere;
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

        healthSphere = new LifeSphere();
        healthSphere.setId(2L);
        healthSphere.setName("Health");

        project1 = new Project();
        project1.setId(1L);
        project1.setTitle("Project 1");
        project1.setUser(testUser);

        project2 = new Project();
        project2.setId(2L);
        project2.setTitle("Project 2");
        project2.setUser(testUser);
    }

    private Task createTestTask(Long id, LifeSphere sphere, Project project, boolean completed,
                                LocalDate deadline, int estimatedTime, TaskType type) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Task " + id);
        task.setUser(testUser);
        task.setLifeSphere(sphere);
        task.setProject(project);
        task.setCompleted(completed);
        task.setDeadline(deadline);
        task.setEstimatedTimeMinutes(estimatedTime);
        task.setPriority(Priority.HIGH);
        task.setEnergyCost(EnergyCost.MEDIUM);
        task.setType(type);
        return task;
    }

    @Test
    void getLifeSphereBalance_ShouldReturnCorrectBalance() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate within30Days = today.minusDays(15);

        List<Task> tasks = Arrays.asList(
                createTestTask(1L, workSphere, project1, true, within30Days, 120, TaskType.STEP),
                createTestTask(2L, workSphere, project1, true, within30Days, 180, TaskType.ACTION),
                createTestTask(3L, healthSphere, null, true, within30Days, 90, TaskType.RITUAL),
                createTestTask(4L, workSphere, project2, false, within30Days, 60, TaskType.STEP),
                createTestTask(5L, healthSphere, null, true, today.minusDays(35), 200, TaskType.ACTION)
        );

        when(taskRepository.findAllByUserId(1L)).thenReturn(tasks);

        // Act
        Map<String, Double> result = analyticsService.getLifeSphereBalance(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals(300.0, result.get("Work"));
        assertEquals(90.0, result.get("Health"));
        verify(taskRepository).findAllByUserId(1L);
    }

    @Test
    void getLifeSphereBalance_NoCompletedTasks_ShouldReturnEmptyMap() {
        // Arrange
        List<Task> tasks = Arrays.asList(
                createTestTask(1L, workSphere, project1, false, LocalDate.now().minusDays(10), 120, TaskType.STEP),
                createTestTask(2L, healthSphere, null, false, LocalDate.now().minusDays(5), 180, TaskType.ACTION)
        );

        when(taskRepository.findAllByUserId(1L)).thenReturn(tasks);

        // Act
        Map<String, Double> result = analyticsService.getLifeSphereBalance(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getMainProjectsProgress_ShouldCalculateProgressCorrectly() {
        // Arrange
        List<Project> projects = Arrays.asList(project1, project2);

        List<Task> project1Tasks = Arrays.asList(
                createTestTask(1L, workSphere, project1, true, LocalDate.now(), 60, TaskType.STEP),
                createTestTask(2L, workSphere, project1, false, LocalDate.now().plusDays(1), 90, TaskType.STEP),
                createTestTask(3L, workSphere, project1, true, LocalDate.now(), 30, TaskType.ACTION)
        );

        List<Task> project2Tasks = Arrays.asList(
                createTestTask(4L, healthSphere, project2, true, LocalDate.now(), 45, TaskType.STEP)
        );

        when(projectRepository.findAllByUserId(1L)).thenReturn(projects);
        when(taskRepository.findAllByProjectId(1L)).thenReturn(project1Tasks);
        when(taskRepository.findAllByProjectId(2L)).thenReturn(project2Tasks);

        // Act
        List<ProjectProgressDto> result = analyticsService.getMainProjectsProgress(1L);

        // Assert
        assertEquals(2, result.size());

        ProjectProgressDto project1Progress = result.get(0);
        assertEquals("Project 1", project1Progress.title());
        assertEquals(2, project1Progress.totalSteps());
        assertEquals(1, project1Progress.completedSteps());
        assertEquals(50.0, project1Progress.progressPercentage());

        ProjectProgressDto project2Progress = result.get(1);
        assertEquals("Project 2", project2Progress.title());
        assertEquals(1, project2Progress.totalSteps());
        assertEquals(1, project2Progress.completedSteps());
        assertEquals(100.0, project2Progress.progressPercentage());
    }

    @Test
    void getProductivityStats_ShouldCalculateStreaksCorrectly() {
        // Arrange
        LocalDate today = LocalDate.now();
        List<Task> completedTasks = Arrays.asList(
                createTestTask(1L, workSphere, project1, true, today.minusDays(3), 60, TaskType.STEP),
                createTestTask(2L, workSphere, project1, true, today.minusDays(2), 90, TaskType.ACTION),
                createTestTask(3L, healthSphere, null, true, today.minusDays(1), 45, TaskType.RITUAL),
                createTestTask(4L, workSphere, project2, true, today, 120, TaskType.STEP)
        );

        when(taskRepository.findAllByUserId(1L)).thenReturn(completedTasks);

        // Act
        Map<String, Object> result = analyticsService.getProductivityStats(1L);

        // Assert
        assertEquals(4L, result.get("currentStreak"));
        assertEquals(4L, result.get("maxStreak"));
        assertEquals(Integer.valueOf(4), result.get("totalCompletedTasks"));
        assertEquals(LocalDate.now(), result.get("analysisDate"));
    }

    @Test
    void getTimeStatistics_ShouldCalculateCorrectMetrics() {
        // Arrange
        List<Task> tasks = Arrays.asList(
                createTestTask(1L, workSphere, project1, true, LocalDate.now(), 120, TaskType.STEP),
                createTestTask(2L, workSphere, project1, false, LocalDate.now().plusDays(1), 180, TaskType.ACTION),
                createTestTask(3L, healthSphere, null, true, LocalDate.now(), 90, TaskType.RITUAL)
        );

        when(taskRepository.findAllByUserId(1L)).thenReturn(tasks);

        // Act
        Map<String, Object> result = analyticsService.getTimeStatistics(1L);

        // Assert
        assertEquals(390, result.get("totalTimePlanned"));
        assertEquals(210, result.get("totalTimeCompleted"));
        assertEquals(53.85, result.get("completionRate"));
        assertEquals(Integer.valueOf(3), result.get("tasksCount"));
        assertEquals(2L, result.get("completedTasksCount"));
    }

    @Test
    void getPriorityDistribution_ShouldCountTasksByPriority() {
        // Arrange
        Task task1 = createTestTask(1L, workSphere, project1, true, LocalDate.now(), 120, TaskType.STEP);
        task1.setPriority(Priority.HIGH);

        Task task2 = createTestTask(2L, workSphere, project1, false, LocalDate.now().plusDays(1), 180, TaskType.ACTION);
        task2.setPriority(Priority.HIGH);

        Task task3 = createTestTask(3L, healthSphere, null, true, LocalDate.now(), 90, TaskType.RITUAL);
        task3.setPriority(Priority.MEDIUM);

        Task task4 = createTestTask(4L, healthSphere, null, false, LocalDate.now(), 60, TaskType.ACTION);
        task4.setPriority(Priority.LOW);

        List<Task> tasks = Arrays.asList(task1, task2, task3, task4);

        when(taskRepository.findAllByUserId(1L)).thenReturn(tasks);

        // Actt
        Map<String, Long> result = analyticsService.getPriorityDistribution(1L);

        // Assert
        assertEquals(3, result.size());
        assertEquals(2L, result.get("HIGH"));
        assertEquals(1L, result.get("MEDIUM"));
        assertEquals(1L, result.get("LOW"));
    }
}