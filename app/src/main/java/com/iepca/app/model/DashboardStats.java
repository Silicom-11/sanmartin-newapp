package com.iepca.app.model;

public class DashboardStats {
    private int totalStudents;
    private int totalTeachers;
    private int totalCourses;
    private int totalParents;
    private int activeStudents;
    private int pendingJustifications;
    private double attendanceRate;
    private double averageGrade;

    public int getTotalStudents() { return totalStudents; }
    public int getTotalTeachers() { return totalTeachers; }
    public int getTotalCourses() { return totalCourses; }
    public int getTotalParents() { return totalParents; }
    public int getActiveStudents() { return activeStudents; }
    public int getPendingJustifications() { return pendingJustifications; }
    public double getAttendanceRate() { return attendanceRate; }
    public double getAverageGrade() { return averageGrade; }
}