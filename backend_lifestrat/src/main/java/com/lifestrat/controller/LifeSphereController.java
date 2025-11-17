package com.lifestratproj.controller;

import com.lifestratproj.entity.LifeSphere;
import com.lifestratproj.service.LifeSphereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spheres")
public class LifeSphereController {

    @Autowired
    private LifeSphereService lifeSphereService;

    @GetMapping
    public ResponseEntity<List<LifeSphere>> getAllSpheres() {
        List<LifeSphere> spheres = lifeSphereService.getAllSpheres();
        return ResponseEntity.ok(spheres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LifeSphere> getSphereById(@PathVariable Long id) {
        try {
            LifeSphere sphere = lifeSphereService.getSphereById(id);
            return ResponseEntity.ok(sphere);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}