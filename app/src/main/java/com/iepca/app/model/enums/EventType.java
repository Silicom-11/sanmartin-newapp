package com.iepca.app.model.enums;

import com.google.gson.annotations.SerializedName;

public enum EventType {
    @SerializedName("exam") EXAM("exam"),
    @SerializedName("meeting") MEETING("meeting"),
    @SerializedName("holiday") HOLIDAY("holiday"),
    @SerializedName("activity") ACTIVITY("activity"),
    @SerializedName("deadline") DEADLINE("deadline");

    private final String value;
    EventType(String value) { this.value = value; }
    public String getValue() { return value; }
}