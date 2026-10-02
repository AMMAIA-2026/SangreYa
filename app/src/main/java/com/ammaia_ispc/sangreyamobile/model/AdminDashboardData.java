package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;
import java.util.List;

public class AdminDashboardData implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int totalCampaigns;
    public final int totalEnrollments;
    public final int totalDonors;
    public final List<DashboardMonthlyDonors> monthlyDonors;
    public final List<DashboardMonthlyDonors> monthlyEnrollments;
    public final List<DashboardCampaignStatus> campaignsByStatus;
    public final List<Campaign> recentCampaigns;

    public AdminDashboardData(
            int totalCampaigns,
            int totalEnrollments,
            int totalDonors,
            List<DashboardMonthlyDonors> monthlyDonors,
            List<DashboardMonthlyDonors> monthlyEnrollments,
            List<DashboardCampaignStatus> campaignsByStatus,
            List<Campaign> recentCampaigns) {
        this.totalCampaigns = totalCampaigns;
        this.totalEnrollments = totalEnrollments;
        this.totalDonors = totalDonors;
        this.monthlyDonors = monthlyDonors;
        this.monthlyEnrollments = monthlyEnrollments;
        this.campaignsByStatus = campaignsByStatus;
        this.recentCampaigns = recentCampaigns;
    }
}
