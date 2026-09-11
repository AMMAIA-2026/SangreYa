package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;

public class DashboardCampaignStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String estado;
    public final int cantidad;

    public DashboardCampaignStatus(String estado, int cantidad) {
        this.estado = estado;
        this.cantidad = cantidad;
    }
}
