package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    @SerializedName("userId") private String userId;
    @SerializedName("user") private String user;
    private Coordinates coordinates;
    private double latitude;
    private double longitude;
    private double accuracy;
    private double altitude;
    private double heading;
    private double speed;
    private double battery;
    private boolean isOnline;
    private String sessionStatus;
    private String timestamp;
    private String createdAt;
    private String clientTimestamp;
    private Student student;

    public String getId() { return id; }
    public String getUserId() { return userId != null ? userId : user; }
    public double getLatitude() { return coordinates != null && coordinates.latitude != null ? coordinates.latitude : latitude; }
    public double getLongitude() { return coordinates != null && coordinates.longitude != null ? coordinates.longitude : longitude; }
    public double getAccuracy() { return coordinates != null && coordinates.accuracy != null ? coordinates.accuracy : accuracy; }
    public double getAltitude() { return coordinates != null && coordinates.altitude != null ? coordinates.altitude : altitude; }
    public double getHeading() { return coordinates != null && coordinates.heading != null ? coordinates.heading : heading; }
    public double getSpeed() { return coordinates != null && coordinates.speed != null ? coordinates.speed : speed; }
    public double getBattery() { return battery; }
    public boolean isOnline() { return sessionStatus != null ? "online".equalsIgnoreCase(sessionStatus) : isOnline; }
    public String getTimestamp() {
        if (timestamp != null) return timestamp;
        if (createdAt != null) return createdAt;
        return clientTimestamp != null ? clientTimestamp : "--";
    }
    public Student getStudent() { return student; }

    public void setLatitude(double l) { this.latitude = l; }
    public void setLongitude(double l) { this.longitude = l; }
    public void setAccuracy(double a) { this.accuracy = a; }
    public void setAltitude(double a) { this.altitude = a; }
    public void setSpeed(double s) { this.speed = s; }
    public void setBattery(double b) { this.battery = b; }

    private static class Coordinates {
        Double latitude;
        Double longitude;
        Double accuracy;
        Double altitude;
        Double speed;
        Double heading;
    }
}
