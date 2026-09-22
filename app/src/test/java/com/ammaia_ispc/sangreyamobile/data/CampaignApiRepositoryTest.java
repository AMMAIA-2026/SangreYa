package com.ammaia_ispc.sangreyamobile.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CampaignApiRepositoryTest {

    @Test
    public void unauthorizedStatusesAreRecognized() {
        assertTrue(CampaignApiRepository.isUnauthorized(
                new CampaignApiRepository.HttpException(401, "")));
        assertTrue(CampaignApiRepository.isUnauthorized(
                new CampaignApiRepository.HttpException(403, "")));
    }

    @Test
    public void unrelatedErrorsAreNotAuthorizationErrors() {
        assertFalse(CampaignApiRepository.isUnauthorized(
                new CampaignApiRepository.HttpException(404, "")));
        assertFalse(CampaignApiRepository.isUnauthorized(new IllegalStateException()));
    }
}
