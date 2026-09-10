package com.ammaia_ispc.sangreyamobile.model;
import java.io.Serializable;
public class HealthCenter implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int id;
    public final String name;
    public final String address;
    public final String neighborhood;
    public final String city;
    public final String phone;
    public final String website;
    public final String latitude;
    public final String longitude;

    public HealthCenter(
            int id,
            String name,
            String address,
            String neighborhood,
            String city,
            String phone,
            String website,
            String latitude,
            String longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.neighborhood = neighborhood;
        this.city = city;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
