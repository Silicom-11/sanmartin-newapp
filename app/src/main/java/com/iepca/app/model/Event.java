package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import com.iepca.app.model.enums.EventType;
import java.util.List;

public class Event {
    @SerializedName("_id") private String id;
    private String title;
    private String date;
    private String time;
    private EventType type;
    private String description;
    private String location;
    private List<String> participants;
    private boolean notifyStudents;
    private boolean notifyParents;
    private boolean notifyTeachers;
    private String createdBy;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public EventType getType() { return type; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }

    public void setTitle(String t) { this.title = t; }
    public void setDate(String d) { this.date = d; }
    public void setTime(String t) { this.time = t; }
    public void setType(EventType t) { this.type = t; }
    public void setDescription(String d) { this.description = d; }
    public void setLocation(String l) { this.location = l; }
}