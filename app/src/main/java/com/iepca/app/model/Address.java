package com.iepca.app.model;

public class Address {
    private String street;
    private String district;
    private String city;
    private String province;
    private String region;
    private String reference;

    public String getStreet() { return street; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getFull() {
        return (street != null ? street : "") +
               (district != null ? ", " + district : "") +
               (city != null ? ", " + city : "");
    }
}