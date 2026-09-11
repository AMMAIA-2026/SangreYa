package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;

public class DashboardMonthlyDonors implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int anio;
    public final int mes;
    public final int cantidad;

    public DashboardMonthlyDonors(int anio, int mes, int cantidad) {
        this.anio = anio;
        this.mes = mes;
        this.cantidad = cantidad;
    }
}
