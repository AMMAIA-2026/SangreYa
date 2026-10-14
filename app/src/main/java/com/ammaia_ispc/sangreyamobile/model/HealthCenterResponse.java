package com.ammaia_ispc.sangreyamobile.model;

import com.google.gson.annotations.SerializedName;

public class HealthCenterResponse {
    public int id;
    @SerializedName("nombre") public String name;
    @SerializedName("direccion") public String address;
    @SerializedName("barrio") public String neighborhood;
    @SerializedName("localidad") public String city;
    @SerializedName("telefono") public String phone;
    @SerializedName("sitio_web") public String website;
    @SerializedName("latitud") public String latitude;
    @SerializedName("longitud") public String longitude;
}