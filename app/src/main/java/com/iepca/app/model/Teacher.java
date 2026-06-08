package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Teacher {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String dni;
    private String specialty;
    private List<String> secondarySpecialties;
    private String educationLevel;
    private String university;
    private String employeeCode;
    private String contractType;
    private String workSchedule;
    private String hireDate;
    private List<String> courses;
    private boolean isActive;
    private String email;
    private String phone;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public String getSpecialty() { return specialty; }
    public List<String> getSecondarySpecialties() { return secondarySpecialties; }
    public String getEducationLevel() { return educationLevel; }
    public String getUniversity() { return university; }
    public String getEmployeeCode() { return employeeCode; }
    public String getContractType() { return contractType; }
    public String getWorkSchedule() { return workSchedule; }
    public String getHireDate() { return hireDate; }
    public List<String> getCourses() { return courses; }
    public boolean isActive() { return isActive; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getFullName() { return firstName + " " + lastName; }

    public void setId(String id) { this.id = id; }
    public void setUserId(String u) { this.userId = u; }
    public void setFirstName(String f) { this.firstName = f; }
    public void setLastName(String l) { this.lastName = l; }
    public void setDni(String d) { this.dni = d; }
    public void setSpecialty(String s) { this.specialty = s; }
    public void setActive(boolean a) { this.isActive = a; }
    public void setEmail(String e) { this.email = e; }
    public void setPhone(String p) { this.phone = p; }
}
