package com.ammaia_ispc.sangreyamobile.data;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ApiErrorMapperTest {
    @Test
    public void noExponeBodyInterno() {
        // Arrange
        String internalBody = "{" +
                "\"token\":\"fixture-token-987\"," +
                "\"detalle\":\"stack trace interno\"}";
        CampaignApiRepository.HttpException error =
                new CampaignApiRepository.HttpException(500, internalBody);

        // Act
        String message = error.getMessage();

        // Assert
        assertEquals("HTTP 500", message);
        assertFalse(message.contains("fixture-token-987"));
        assertFalse(message.contains("stack trace interno"));
        assertEquals(internalBody, error.getBody());
    }

    @Test
    public void mapeaStatusHttp() {
        // Arrange / Act / Assert
        assertTrue(CampaignApiRepository.isUnauthorized(
                new CampaignApiRepository.HttpException(401, "")));
        assertTrue(CampaignApiRepository.isUnauthorized(
                new CampaignApiRepository.HttpException(403, "")));
        assertFalse(CampaignApiRepository.isUnauthorized(
                new CampaignApiRepository.HttpException(404, "")));
        assertTrue(CampaignApiRepository.isNotFound(
                new CampaignApiRepository.HttpException(404, "")));
        assertFalse(CampaignApiRepository.isNotFound(
                new CampaignApiRepository.HttpException(500, "")));

        IOException networkFailure = mock(IOException.class);
        assertTrue(CampaignApiRepository.isNetworkError(networkFailure));
        assertFalse(CampaignApiRepository.isNetworkError(
                new CampaignApiRepository.HttpException(500, "")));
    }
}
