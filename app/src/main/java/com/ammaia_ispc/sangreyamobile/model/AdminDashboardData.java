package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;
import java.util.List;

public class AdminDashboardData implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int totalCampanias;
    public final int totalInscripciones;
    public final int totalDonantes;
    public final List<DashboardMonthlyDonors> donantesPorMes;
    public final List<DashboardCampaignStatus> campaniasPorEstado;
    public final List<Campaign> campaniasRecientes;

    public AdminDashboardData(
            int totalCampanias,
            int totalInscripciones,
            int totalDonantes,
            List<DashboardMonthlyDonors> donantesPorMes,
            List<DashboardCampaignStatus> campaniasPorEstado,
            List<Campaign> campaniasRecientes) {
        this.totalCampanias = totalCampanias;
        this.totalInscripciones = totalInscripciones;
        this.totalDonantes = totalDonantes;
        this.donantesPorMes = donantesPorMes;
        this.campaniasPorEstado = campaniasPorEstado;
        this.campaniasRecientes = campaniasRecientes;
    }
}
