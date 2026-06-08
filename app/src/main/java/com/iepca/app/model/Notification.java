package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("_id") private String id;
    private String recipientId;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private String createdAt;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public String getCreatedAt() { return createdAt; }
    public void setRead(boolean r) { this.isRead = r; }
}