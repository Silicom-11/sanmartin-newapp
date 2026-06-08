package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Message {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private String type;
    private List<String> readBy;
    private String createdAt;
    private String sentAt;
    private String sender;

    public String getId() { return id; }
    public String getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getCreatedAt() { return createdAt != null ? createdAt : sentAt; }
    public String getSender() { return sender; }

    public void setContent(String c) { this.content = c; }
    public void setConversationId(String c) { this.conversationId = c; }
}
