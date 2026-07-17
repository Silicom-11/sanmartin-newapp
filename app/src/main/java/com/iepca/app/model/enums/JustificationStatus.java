package com.iepca.app.model.enums;

import com.google.gson.annotations.SerializedName;

/**
 * Justification status. Wire values match the backend (English);
 * {@link #getLabel()} returns the Spanish text shown in the UI.
 */
public enum JustificationStatus {
    @SerializedName("pending") PENDING("pending", "Pendiente"),
    @SerializedName("approved") APPROVED("approved", "Aprobada"),
    @SerializedName("rejected") REJECTED("rejected", "Rechazada");

    private final String value;
    private final String label;

    JustificationStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }
}
