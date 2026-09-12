package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.model.Campaign;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AdminDashboardHelper {
    public static final String MOCK_ADMIN_EMAIL = "admin@unmail.com";
    public static final String MOCK_ADMIN_PASSWORD = "Qwerty123.";

    private AdminDashboardHelper() {
    }

    public static boolean isMockAdmin(String email, String password) {
        return MOCK_ADMIN_EMAIL.equals(email)
                && MOCK_ADMIN_PASSWORD.equals(password);
    }

    public static boolean isMockAdminEmail(String email) {
        return MOCK_ADMIN_EMAIL.equals(email);
    }

    public static int totalCampaigns(List<DashboardCampaignStatus> statuses) {
        int total = 0;
        for (DashboardCampaignStatus status : statuses) {
            total += status.cantidad;
        }
        return total;
    }

    public static List<Campaign> orderRecentCampaigns(List<Campaign> campaigns) {
        List<Campaign> ordered = new ArrayList<>(campaigns);
        ordered.sort(Comparator
                .comparingInt((Campaign campaign) -> statusOrder(campaign.calculatedStatus))
                .thenComparing(campaign -> campaign.startDate));
        return ordered;
    }

    private static int statusOrder(String status) {
        if ("Activa".equals(status)) {
            return 0;
        }
        if ("Proximamente".equals(status)) {
            return 1;
        }
        return 2;
    }

    public static int percentage(int amount, int total) {
        return total == 0 ? 0 : Math.round(amount * 100f / total);
    }

    public static String monthLabel(int month) {
        String[] months = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }

}
