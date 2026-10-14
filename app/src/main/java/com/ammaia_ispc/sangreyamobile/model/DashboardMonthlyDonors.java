package com.ammaia_ispc.sangreyamobile.model;

import java.io.Serializable;

public class DashboardMonthlyDonors implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int year;
    public final int month;
    public final int count;

    public DashboardMonthlyDonors(int year, int month, int count) {
        this.year = year;
        this.month = month;
        this.count = count;
    }
}
