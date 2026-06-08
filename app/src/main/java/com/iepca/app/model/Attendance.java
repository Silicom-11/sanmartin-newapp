package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import com.iepca.app.model.enums.AttendanceStatus;

public class Attendance {
    @SerializedName("_id") private String id;
    private String studentId;
    private String courseId;
    private String date;
    private AttendanceStatus status;
    private String arrivalTime;
    private String observations;
    private String teacherId;
    private Student student;

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public String getDate() { return date; }
    public AttendanceStatus getStatus() { return status; }
    public String getArrivalTime() { return arrivalTime; }
    public String getObservations() { return observations; }
    public Student getStudent() { return student; }

    public void setStudentId(String s) { this.studentId = s; }
    public void setCourseId(String c) { this.courseId = c; }
    public void setDate(String d) { this.date = d; }
    public void setStatus(AttendanceStatus s) { this.status = s; }
    public void setObservations(String o) { this.observations = o; }
}