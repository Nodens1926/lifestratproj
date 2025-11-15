package com.lifestrat.service;

import com.lifestrat.entity.*;
import com.lifestrat.repository.TaskRepository;
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
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private LifeSphere workSphere;
    private Project project1;
    private Task task1;
    private Task task2;

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

        task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setUser(testUser);
        task1.setLifeSphere(workSphere);
        task1.setProject(project1);
        task1.setCompleted(false);
        task1.setDeadline(LocalDate.now().plusDays(5));
        task1.setEstimatedTimeMinutes(120);
        task1.setPriority(Priority.HIGH);
        task1.setEnergyCost(EnergyCost.MEDIUM);
        task1.setType(TaskType.STEP);

        task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUser(testUser);
        task2.setLifeSphere(workSphere);
        task2.setProject(null);
        task2.setCompleted(true);
        task2.setDeadline(LocalDate.now().minusDays(1));
        task2.setEstimatedTimeMinutes(90);
        task2.setPriority(Priority.MEDIUM);
        task2.setEnergyCost(EnergyCost.LOW);
        task2.setType(TaskType.ACTION);
    }

    @Test
    void findAllByUserId_ShouldReturnUserTasks() {
        // Arrange
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAllByUserId(1L)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskService.findAllByUserId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        verify(taskRepository).findAllByUserId(1L);
    }

    @Test
    void findAllByUserIdAndType_ShouldReturnFilteredTasks() {
        // Arrange
        List<Task> expectedTasks = Arrays.asList(task1);
        when(taskRepository.findAllByUserIdAndType(1L, TaskType.STEP)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskService.findAllByUserIdAndType(1L, TaskType.STEP);

        // Assert
        assertEquals(1, result.size());
        assertEquals(TaskType.STEP, result.get(0).getType());
        verify(taskRepository).findAllByUserIdAndType(1L, TaskType.STEP);
    }

    @Test
    void findByIdAndUserId_TaskExists_ShouldReturnTask() {
        // Arrange
        List<Task> userTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAllByUserId(1L)).thenReturn(userTasks);

        // Act
        Optional<Task> result = taskService.findByIdAndUserId(1L, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(task1, result.get());
    }

    @Test
    void findByIdAndUserId_TaskNotFound_ShouldReturnEmpty() {
        // Arrange
        List<Task> userTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAllByUserId(1L)).thenReturn(userTasks);

        // Act
        Optional<Task> result = taskService.findByIdAndUserId(3L, 1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void create_ValidTask_ShouldSaveAndReturn() {
        // Arrange
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setUser(testUser);
        newTask.setLifeSphere(workSphere);
        newTask.setProject(project1);
        newTask.setCompleted(false);
        newTask.setDeadline(LocalDate.now().plusDays(3));
        newTask.setEstimatedTimeMinutes(60);
        newTask.setPriority(Priority.LOW);
        newTask.setEnergyCost(EnergyCost.HIGH);
        newTask.setType(TaskType.RITUAL);

        when(taskRepository.save(newTask)).thenReturn(newTask);

        // Act
        Task result = taskService.create(newTask, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        verify(taskRepository).save(newTask);
    }

    @Test
    void create_UserMismatch_ShouldThrowException() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setId(2L);

        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setUser(wrongUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(newTask, 1L);
        });
    }

    @Test
    void update_ShouldUpdateAndSaveTask() {
        // Arrange
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated description");
        updatedTask.setDeadline(LocalDate.now().plusDays(1));
        updatedTask.setPriority(Priority.CRITICAL);
        updatedTask.setEstimatedTimeMinutes(180);
        updatedTask.setEnergyCost(EnergyCost.HIGH);
        updatedTask.setCompleted(true);

        when(taskRepository.save(task1)).thenReturn(task1);

        // Act
        Task result = taskService.update(task1, updatedTask);

        // Assert
        assertEquals("Updated Task", result.getTitle());
        assertTrue(result.isCompleted());
        verify(taskRepository).save(task1);
    }

    @Test
    void delete_ShouldDeleteTask() {
        // Act
        taskService.delete(task1);

        // Assert
        verify(taskRepository).delete(task1);
    }

    @Test
    void markAsCompleted_ValidTask_ShouldMarkAsCompleted() {
        // Arrange
        List<Task> userTasks = Arrays.asList(task1);
        when(taskRepository.findAllByUserId(1L)).thenReturn(userTasks);
        when(taskRepository.save(task1)).thenReturn(task1);

        // Act
        Task result = taskService.markAsCompleted(1L, 1L);

        // Assert
        assertTrue(result.isCompleted());
        verify(taskRepository).save(task1);
    }

    @Test
    void markAsCompleted_TaskNotFound_ShouldThrowException() {
        // Arrange
        when(taskRepository.findAllByUserId(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taskService.markAsCompleted(1L, 1L);
        });
    }

    @Test
    void markAsCompleted_TaskAlreadyCompleted_ShouldStillWork() {
        // Arrange
        task2.setCompleted(true);
        List<Task> userTasks = Arrays.asList(task2);
        when(taskRepository.findAllByUserId(1L)).thenReturn(userTasks);
        when(taskRepository.save(task2)).thenReturn(task2);

        // Act
        Task result = taskService.markAsCompleted(2L, 1L);

        // Assert
        assertTrue(result.isCompleted());
        verify(taskRepository).save(task2);
    }
}