package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Student {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String dni;
    private String birthDate;
    private String gender;
    private String photo;
    private String studentCode;
    private String enrollmentNumber;
    private String gradeLevel;
    private String section;
    private String shift;
    private String status;
    private Address address;
    private MedicalInfo medicalInfo;
    private List<Guardian> guardians;

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public String getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
    public String getPhoto() { return photo; }
    public String getStudentCode() { return studentCode; }
    public String getEnrollmentNumber() { return enrollmentNumber; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSection() { return section; }
    public String getShift() { return shift; }
    public String getStatus() { return status; }
    public Address getAddress() { return address; }
    public MedicalInfo getMedicalInfo() { return medicalInfo; }
    public List<Guardian> getGuardians() { return guardians; }
    public String getFullName() { return firstName + " " + lastName; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String u) { this.userId = u; }
    public void setFirstName(String f) { this.firstName = f; }
    public void setLastName(String l) { this.lastName = l; }
    public void setDni(String d) { this.dni = d; }
    public void setBirthDate(String b) { this.birthDate = b; }
    public void setGender(String g) { this.gender = g; }
    public void setPhoto(String p) { this.photo = p; }
    public void setStudentCode(String s) { this.studentCode = s; }
    public void setEnrollmentNumber(String e) { this.enrollmentNumber = e; }
    public void setGradeLevel(String g) { this.gradeLevel = g; }
    public void setSection(String s) { this.section = s; }
    public void setShift(String s) { this.shift = s; }
    public void setStatus(String s) { this.status = s; }
    public void setAddress(Address a) { this.address = a; }
    public void setMedicalInfo(MedicalInfo m) { this.medicalInfo = m; }
    public void setGuardians(List<Guardian> g) { this.guardians = g; }
}
