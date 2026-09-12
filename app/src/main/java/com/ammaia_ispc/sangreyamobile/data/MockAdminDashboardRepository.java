package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.AdminDashboardData;
import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.model.DashboardMonthlyDonors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MockAdminDashboardRepository {
    private MockAdminDashboardRepository() {
    }

    public static AdminDashboardData getDashboard() {
        List<Campaign> campaigns = MockCampaignRepository.getCampaigns();
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        List<Campaign> recentCampaigns = new ArrayList<>();

        for (Campaign campaign : campaigns) {
            String status = campaign.calculatedStatus;
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            if (!"Finalizada".equals(status) && recentCampaigns.size() < 5) {
                recentCampaigns.add(campaign);
            }
        }

        List<DashboardCampaignStatus> campaignsByStatus = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            campaignsByStatus.add(new DashboardCampaignStatus(entry.getKey(), entry.getValue()));
        }

        List<DashboardMonthlyDonors> donantesPorMes = Arrays.asList(
                new DashboardMonthlyDonors(2026, 4, 35),
                new DashboardMonthlyDonors(2026, 5, 48),
                new DashboardMonthlyDonors(2026, 6, 42),
                new DashboardMonthlyDonors(2026, 7, 57),
                new DashboardMonthlyDonors(2026, 8, 58),
                new DashboardMonthlyDonors(2026, 9, 72));

        int totalInscripciones = 0;
        for (Campaign campaign : campaigns) {
            totalInscripciones += campaign.totalRegistered;
        }
        return new AdminDashboardData(
                campaigns.size(),
                totalInscripciones,
                312,
                donantesPorMes,
                campaignsByStatus,
                recentCampaigns);
    }
}
