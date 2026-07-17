package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Parent {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String dni;
    private String phone;
    private String secondaryPhone;
    private String address;
    private String occupation;
    private String workplace;
    private List<ParentChild> children;
    private boolean isActive;
    private String email;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public String getPhone() { return phone; }
    public String getOccupation() { return occupation; }
    public String getWorkplace() { return workplace; }
    public List<ParentChild> getChildren() { return children; }
    public boolean isActive() { return isActive; }
    public String getEmail() { return email; }
    public String getFullName() { return firstName + " " + lastName; }

    public void setId(String id) { this.id = id; }
    public void setFirstName(String f) { this.firstName = f; }
    public void setLastName(String l) { this.lastName = l; }
    public void setDni(String d) { this.dni = d; }
    public void setPhone(String p) { this.phone = p; }
    public void setOccupation(String o) { this.occupation = o; }
    public void setWorkplace(String w) { this.workplace = w; }
    public void setActive(boolean a) { this.isActive = a; }
    public void setEmail(String e) { this.email = e; }
}