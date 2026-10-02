package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;

public class DashboardCampaignStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String status;
    public final int count;

    public DashboardCampaignStatus(String status, int count) {
        this.status = status;
        this.count = count;
    }
}
