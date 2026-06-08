package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import com.iepca.app.model.enums.JustificationStatus;
import java.util.List;

public class Justification {
    @SerializedName("_id") private String id;
    private String studentId;
    private String parentId;
    private List<String> dates;
    private String reason;
    private String observations;
    private List<DocumentFile> documents;
    private JustificationStatus status;
    private String reviewedBy;
    private String reviewNote;
    private String createdAt;
    private Student student;
    private Parent parent;

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getParentId() { return parentId; }
    public List<String> getDates() { return dates; }
    public String getReason() { return reason; }
    public String getObservations() { return observations; }
    public List<DocumentFile> getDocuments() { return documents; }
    public JustificationStatus getStatus() { return status; }
    public String getReviewedBy() { return reviewedBy; }
    public String getReviewNote() { return reviewNote; }
    public String getCreatedAt() { return createdAt; }
    public Student getStudent() { return student; }
    public Parent getParent() { return parent; }

    public void setStudentId(String s) { this.studentId = s; }
    public void setParentId(String p) { this.parentId = p; }
    public void setDates(List<String> d) { this.dates = d; }
    public void setReason(String r) { this.reason = r; }
    public void setObservations(String o) { this.observations = o; }
}