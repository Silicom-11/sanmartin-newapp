package com.iepca.app.model;

public class ParentChild {
    private String student;
    private String relationship;
    private boolean isPrimaryContact;
    private boolean canPickUp;
    private boolean isEmergencyContact;

    public String getStudentId() { return student; }
    public String getRelationship() { return relationship; }
    public boolean isPrimaryContact() { return isPrimaryContact; }
    public boolean isCanPickUp() { return canPickUp; }
    public boolean isEmergencyContact() { return isEmergencyContact; }
}
