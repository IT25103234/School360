package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long moduleId;
    private String moduleName;
    private Long teacherId;

    private String dayOfWeek; // e.g. Monday, Tuesday
    private String startTime; // e.g. 09:00
    private String endTime; // e.g. 11:00
    private String room; // e.g. Room 204

    private String status; // ACTIVE, CANCELLED, DELAYED
    private String statusMessage; // e.g. "Delayed by 15 mins due to technical setup"

    public Schedule() {}

    public Schedule(Long moduleId, String moduleName, Long teacherId, String dayOfWeek, String startTime, String endTime, String room, String status, String statusMessage) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.status = status;
        this.statusMessage = statusMessage;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
