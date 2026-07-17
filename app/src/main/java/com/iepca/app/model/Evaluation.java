package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import com.iepca.app.model.enums.EvaluationType;

public class Evaluation {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String courseId;
    private String name;
    private EvaluationType type;
    private int bimester;
    private double maxGrade;
    private double weight;
    private String date;
    private String description;
    private int order;

    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getName() { return name; }
    public EvaluationType getType() { return type; }
    public int getBimester() { return bimester; }
    public double getMaxGrade() { return maxGrade; }
    public double getWeight() { return weight; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public int getOrder() { return order; }

    public void setName(String n) { this.name = n; }
    public void setType(EvaluationType t) { this.type = t; }
    public void setCourseId(String c) { this.courseId = c; }
    public void setBimester(int b) { this.bimester = b; }
    public void setMaxGrade(double m) { this.maxGrade = m; }
    public void setWeight(double w) { this.weight = w; }
    public void setDate(String d) { this.date = d; }
    public void setDescription(String d) { this.description = d; }
}