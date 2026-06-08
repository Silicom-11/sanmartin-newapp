package com.iepca.app.model.enums;

import com.google.gson.annotations.SerializedName;

public enum UserRole {
    @SerializedName("administrativo") ADMINISTRATIVO("administrativo"),
    @SerializedName("docente") DOCENTE("docente"),
    @SerializedName("padre") PADRE("padre"),
    @SerializedName("estudiante") ESTUDIANTE("estudiante"),
    @SerializedName("director") DIRECTOR("director");

    private final String value;
    UserRole(String value) { this.value = value; }
    public String getValue() { return value; }
}