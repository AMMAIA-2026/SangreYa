package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.AdminDashboardData;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class DashboardMapperTest {
    @Test
    public void calculaMetricas() {
        // Arrange
        AdminDashboardData dashboard = new AdminDashboardData(
                6,
                18,
                11,
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(
                        new DashboardCampaignStatus("Activa", 3),
                        new DashboardCampaignStatus("Proximamente", 1),
                        new DashboardCampaignStatus("Finalizada", 2)),
                Collections.emptyList());

        // Act
        int total = AdminDashboardHelper.totalCampaigns(dashboard.campaignsByStatus);

        // Assert
        assertEquals(6, dashboard.totalCampaigns);
        assertEquals(18, dashboard.totalEnrollments);
        assertEquals(11, dashboard.totalDonors);
        assertEquals(6, total);
        assertEquals(50, AdminDashboardHelper.percentage(3, total));
        assertEquals(17, AdminDashboardHelper.percentage(1, total));
        assertEquals(33, AdminDashboardHelper.percentage(2, total));
        assertEquals(0, AdminDashboardHelper.percentage(2, 0));
    }
}
