package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;

public class Grade {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    @SerializedName(value = "studentId", alternate = {"student"}) private String studentId;
    @SerializedName(value = "courseId", alternate = {"course"}) private String courseId;
    private String evaluationId;
    private int bimester;
    private double score;
    private double average;
    private String comments;
    private String academicYear;
    private String status;
    @SerializedName("studentData") private Student student;
    private Evaluation evaluation;

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public String getEvaluationId() { return evaluationId; }
    public int getBimester() { return bimester; }
    public double getScore() { return score > 0 ? score : average; }
    public double getAverage() { return average; }
    public String getComments() { return comments; }
    public String getStatus() { return status; }
    public Student getStudent() { return student; }
    public Evaluation getEvaluation() { return evaluation; }

    public void setStudentId(String s) { this.studentId = s; }
    public void setCourseId(String c) { this.courseId = c; }
    public void setEvaluationId(String e) { this.evaluationId = e; }
    public void setBimester(int b) { this.bimester = b; }
    public void setScore(double s) { this.score = s; }
    public void setComments(String c) { this.comments = c; }

    public String getGradeLetter() {
        double value = getScore();
        if (value >= 17) return "AD";
        if (value >= 14) return "A";
        if (value >= 11) return "B";
        return "C";
    }
}
