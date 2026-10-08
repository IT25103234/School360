package com.school360.model;

import jakarta.persistence.*;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "modules")
public class Module {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private Long courseId;
    private Long teacherId; // links to User ID of teacher

    @Column(columnDefinition = "LONGTEXT")
    private String materialsJson;

    @Transient
    private List<Map<String, Object>> materials;

    public Module() {}

    public Module(String name, String code, Long courseId, Long teacherId) {
        this.name = name;
        this.code = code;
        this.courseId = courseId;
        this.teacherId = teacherId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    @JsonIgnore
    public String getMaterialsJson() {
        return materialsJson;
    }

    public void setMaterialsJson(String materialsJson) {
        this.materialsJson = materialsJson;
        this.materials = null;
    }

    @JsonProperty("materials")
    public List<Map<String, Object>> getMaterials() {
        if (this.materials != null) {
            return this.materials;
        }
        if (this.materialsJson != null && !this.materialsJson.trim().isEmpty()) {
            try {
                this.materials = MAPPER.readValue(this.materialsJson, new TypeReference<List<Map<String, Object>>>() {});
                return this.materials;
            } catch (Exception e) {
                this.materials = new ArrayList<>();
                return this.materials;
            }
        }
        this.materials = new ArrayList<>();
        return this.materials;
    }

    public void setMaterials(List<Map<String, Object>> materials) {
        this.materials = materials;
        if (materials != null) {
            try {
                this.materialsJson = MAPPER.writeValueAsString(materials);
            } catch (Exception e) {
                this.materialsJson = "[]";
            }
        } else {
            this.materialsJson = "[]";
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.materials != null) {
            try {
                this.materialsJson = MAPPER.writeValueAsString(this.materials);
            } catch (Exception ignored) {}
        }
    }

    @PostLoad
    public void postLoad() {
        getMaterials();
    }
}
