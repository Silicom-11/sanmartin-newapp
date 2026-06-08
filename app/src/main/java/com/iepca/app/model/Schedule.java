package com.iepca.app.model;

public class Schedule {
    private String day;
    private String startTime;
    private String endTime;
    private String classroom;

    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getClassroom() { return classroom; }
    public String getDisplay() { return day + " " + startTime + "-" + endTime; }
}