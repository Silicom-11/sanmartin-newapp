package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;

public class Institution {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String name;
    private String code;
    private String address;
    private String phone;
    private String email;
    private String logo;
    private String evaluationSystem;
    private String gradeScale;
    private int passingGrade;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getEvaluationSystem() { return evaluationSystem; }
    public String getGradeScale() { return gradeScale; }
    public int getPassingGrade() { return passingGrade; }

    public void setName(String n) { this.name = n; }
    public void setCode(String c) { this.code = c; }
    public void setAddress(String a) { this.address = a; }
    public void setPhone(String p) { this.phone = p; }
    public void setEmail(String e) { this.email = e; }
}