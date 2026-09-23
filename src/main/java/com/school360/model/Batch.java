package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    private String note;

    @Column(columnDefinition = "LONGTEXT")
    private String dataJson;

    public Batch() {}

    public Batch(String label, String note, String dataJson) {
        this.label = label;
        this.note = note;
        this.dataJson = dataJson;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
}
