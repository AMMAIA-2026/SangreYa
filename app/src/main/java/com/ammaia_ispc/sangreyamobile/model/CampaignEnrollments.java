package com.ammaia_ispc.sangreyamobile.model;

import java.util.List;

public class CampaignEnrollments {
    public final Campaign campaign;
    public final int totalRegistered;
    public final List<EnrollmentUser> users;

    public CampaignEnrollments(
            Campaign campaign,
            int totalRegistered,
            List<EnrollmentUser> users) {
        this.campaign = campaign;
        this.totalRegistered = totalRegistered;
        this.users = users;
    }
}
