package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import com.iepca.app.model.enums.AttendanceStatus;

public class Attendance {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    @SerializedName(value = "studentId", alternate = {"student"}) private String studentId;
    @SerializedName(value = "courseId", alternate = {"course"}) private String courseId;
    @SerializedName(value = "teacherId", alternate = {"teacher"}) private String teacherId;
    private String date;
    private AttendanceStatus status;
    private String arrivalTime;
    private String observations;

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public String getTeacherId() { return teacherId; }
    public String getDate() { return date; }
    public AttendanceStatus getStatus() { return status; }
    public String getArrivalTime() { return arrivalTime; }
    public String getObservations() { return observations; }

    public void setStudentId(String s) { this.studentId = s; }
    public void setCourseId(String c) { this.courseId = c; }
    public void setDate(String d) { this.date = d; }
    public void setStatus(AttendanceStatus s) { this.status = s; }
    public void setObservations(String o) { this.observations = o; }
}
