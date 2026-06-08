package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Conversation {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String type;
    private String name;
    private List<String> participants;
    private Message lastMessage;
    private Map<String, Integer> unreadCount;
    private String updatedAt;

    public String getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public List<String> getParticipants() { return participants; }
    public Message getLastMessage() { return lastMessage; }
    public Map<String, Integer> getUnreadCount() { return unreadCount; }
    public String getUpdatedAt() { return updatedAt; }
}
