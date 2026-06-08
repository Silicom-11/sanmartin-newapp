package com.iepca.app.model;

public class ParentChild {
    private String studentId;
    private String relationship;
    private boolean isPrimary;
    private Student student;

    public String getStudentId() { return studentId; }
    public String getRelationship() { return relationship; }
    public boolean isPrimary() { return isPrimary; }
    public Student getStudent() { return student; }
}