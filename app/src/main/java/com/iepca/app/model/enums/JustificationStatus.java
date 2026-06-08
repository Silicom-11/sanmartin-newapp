package com.iepca.app.model.enums;

import com.google.gson.annotations.SerializedName;

public enum JustificationStatus {
    @SerializedName("pendiente") PENDIENTE("pendiente"),
    @SerializedName("aprobada") APROBADA("aprobada"),
    @SerializedName("rechazada") RECHAZADA("rechazada");

    private final String value;
    JustificationStatus(String value) { this.value = value; }
    public String getValue() { return value; }
}