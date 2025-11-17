package com.lifestratproj.app.controller;

import com.lifestratproj.app.dto.ProjectRequest;
import com.lifestratproj.app.entity.Project;
import com.lifestratproj.app.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getUserProjects(@RequestParam Long userId) {
        try {
            List<Project> projects = projectService.getUserProjects(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequest projectRequest, 
                                               @RequestParam Long userId) {
        try {
            Project project = projectService.createProject(projectRequest, userId);
            return new ResponseEntity<>(project, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Project>> getProjectsByFilter(
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long lifeSphereId) {
        
        try {
            List<Project> projects;
            if (status != null) {
                projects = projectService.getUserProjectsByStatus(userId, status);
            } else if (lifeSphereId != null) {
                projects = projectService.getUserProjectsBySphere(userId, lifeSphereId);
            } else {
                projects = projectService.getUserProjects(userId);
            }
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}