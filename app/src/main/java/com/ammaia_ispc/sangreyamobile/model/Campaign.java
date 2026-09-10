package com.ammaia_ispc.sangreyamobile.model;
import java.io.Serializable;
public class Campaign implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int id;
    public final String title;
    public final String description;
    public final String location;
    public final Integer healthCenterId;
    public final HealthCenter healthCenter;
    public final String startDate;
    public final String endDate;
    public final Integer maximumCapacity;
    public final int totalRegistered;
    public final String campaignStatus;
    public final String calculatedStatus;

    public Campaign(
            int id,
            String title,
            String description,
            String location,
            Integer healthCenterId,
            HealthCenter healthCenter,
            String startDate,
            String endDate,
            Integer maximumCapacity,
            int totalRegistered,
            String campaignStatus,
            String calculatedStatus) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.healthCenterId = healthCenterId;
        this.healthCenter = healthCenter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maximumCapacity = maximumCapacity;
        this.totalRegistered = totalRegistered;
        this.campaignStatus = campaignStatus;
        this.calculatedStatus = calculatedStatus;
    }
}
