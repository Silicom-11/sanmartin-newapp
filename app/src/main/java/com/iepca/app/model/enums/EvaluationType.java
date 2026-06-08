package com.iepca.app.model.enums;

import com.google.gson.annotations.SerializedName;

public enum EvaluationType {
    @SerializedName("examen") EXAMEN("examen"),
    @SerializedName("tarea") TAREA("tarea"),
    @SerializedName("practica") PRACTICA("practica"),
    @SerializedName("proyecto") PROYECTO("proyecto"),
    @SerializedName("participacion") PARTICIPACION("participacion"),
    @SerializedName("exposicion") EXPOSICION("exposicion"),
    @SerializedName("otro") OTRO("otro");

    private final String value;
    EvaluationType(String value) { this.value = value; }
    public String getValue() { return value; }
}