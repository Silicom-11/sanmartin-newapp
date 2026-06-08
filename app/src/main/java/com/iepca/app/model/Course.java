package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Course {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String name;
    private String code;
    private String description;
    private String gradeLevel;
    private String section;
    @SerializedName("teacher") private String teacherId;
    @SerializedName("teacherData") private Teacher teacher;
    @SerializedName("students") private List<String> studentIds;
    private List<Schedule> schedule;
    private String academicYear;
    private boolean isActive;
    private int studentCount;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSection() { return section; }
    public String getTeacherId() { return teacherId; }
    public Teacher getTeacher() { return teacher; }
    public List<String> getStudentIds() { return studentIds; }
    public List<Schedule> getSchedule() { return schedule; }
    public boolean isActive() { return isActive; }
    public int getStudentCount() { return studentCount; }

    public void setId(String id) { this.id = id; }
    public void setName(String n) { this.name = n; }
    public void setCode(String c) { this.code = c; }
    public void setDescription(String d) { this.description = d; }
    public void setGradeLevel(String g) { this.gradeLevel = g; }
    public void setSection(String s) { this.section = s; }
    public void setTeacherId(String t) { this.teacherId = t; }
    public void setActive(boolean a) { this.isActive = a; }
}
