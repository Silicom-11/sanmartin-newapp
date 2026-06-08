package com.iepca.app.model.enums;

import com.google.gson.annotations.SerializedName;

public enum AttendanceStatus {
    @SerializedName("present") PRESENT("present"),
    @SerializedName("absent") ABSENT("absent"),
    @SerializedName("late") LATE("late"),
    @SerializedName("justified") JUSTIFIED("justified");

    private final String value;
    AttendanceStatus(String value) { this.value = value; }
    public String getValue() { return value; }
}