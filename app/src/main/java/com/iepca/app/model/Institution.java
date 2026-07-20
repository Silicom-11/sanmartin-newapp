package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;

public class Institution {
    @SerializedName(value = "_id", alternate = {"id"}) private String id;
    private String name;
    private String code;
    private Address address;
    private String phone;
    private String email;
    private String logo;
    private Object evaluationSystem;
    private Object gradeScale;
    private int passingGrade;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Object getEvaluationSystem() { return evaluationSystem; }
    public Object getGradeScale() { return gradeScale; }
    public int getPassingGrade() { return passingGrade; }

    public String getAddress() {
        if (address == null) return null;
        return address.getFormatted();
    }

    public void setName(String n) { this.name = n; }
    public void setCode(String c) { this.code = c; }
    public void setPhone(String p) { this.phone = p; }
    public void setEmail(String e) { this.email = e; }

    public static class Address {
        private String street;
        private String district;
        private String city;
        private String region;
        private String postalCode;
        private String reference;
        private String country;
        private String formattedAddress;

        public String getStreet() { return street; }
        public String getDistrict() { return district; }
        public String getCity() { return city; }
        public String getRegion() { return region; }

        public String getFormatted() {
            if (formattedAddress != null && !formattedAddress.isEmpty()) return formattedAddress;
            StringBuilder sb = new StringBuilder();
            if (street != null) sb.append(street);
            if (district != null) { if (sb.length() > 0) sb.append(", "); sb.append(district); }
            if (city != null) { if (sb.length() > 0) sb.append(", "); sb.append(city); }
            if (region != null) { if (sb.length() > 0) sb.append(", "); sb.append(region); }
            return sb.toString();
        }
    }
}
