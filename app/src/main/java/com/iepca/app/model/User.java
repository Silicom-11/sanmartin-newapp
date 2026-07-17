package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import com.iepca.app.model.enums.UserRole;
import java.util.List;

public class User {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String avatar;
    private String phone;
    private String dni;
    private boolean isActive;
    private String studentProfile;

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserRole getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getPhone() { return phone; }
    public String getDni() { return dni; }
    public boolean isActive() { return isActive; }
    public String getStudentProfile() { return studentProfile; }
    public String getFullName() { return firstName + " " + lastName; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(UserRole role) { this.role = role; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDni(String dni) { this.dni = dni; }
    public void setActive(boolean active) { isActive = active; }
    public void setStudentProfile(String studentProfile) { this.studentProfile = studentProfile; }
}
