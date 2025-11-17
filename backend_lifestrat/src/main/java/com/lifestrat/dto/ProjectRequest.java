package com.lifestratproj.app.dto;

import java.time.LocalDate;

public class ProjectRequest {
    private String name;
    private String description;
    private Long lifeSphereId;
    private LocalDate deadline;
    private String priority;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getLifeSphereId() { return lifeSphereId; }
    public void setLifeSphereId(Long lifeSphereId) { this.lifeSphereId = lifeSphereId; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}