package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.Campaign;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CampaignMapperTest {
    // TC-UNIT-08
    @Test
    public void optionalHealthCenterNull() throws Exception {
        // Arrange
        JSONObject withoutCenter = new JSONObject("{\"id\":7,\"titulo\":\"Campana sintetica\","
                + "\"descripcion\":\"Jornada de prueba\",\"ubicacion\":\"Sede ficticia\","
                + "\"centro_salud\":null,\"fecha_inicio\":\"2026-11-10\",\"fecha_fin\":\"2026-11-12\","
                + "\"cupo_maximo\":50,\"total_inscriptos\":3}");
        JSONObject withCenter = new JSONObject("{\"id\":8,\"titulo\":\"Campana con centro\","
                + "\"fecha_inicio\":\"2026-11-10\",\"fecha_fin\":\"2026-11-12\",\"cupo_maximo\":50,"
                + "\"centro_salud_detalle\":{\"id\":4,\"nombre\":\"Centro ficticio\"}}");

        // Act
        Campaign campaignWithoutCenter = CampaignApiRepository.fromJson(withoutCenter);
        Campaign campaignWithCenter = CampaignApiRepository.fromJson(withCenter);

        // Assert: the health center is optional and does not break the rest of the mapping.
        assertNull(campaignWithoutCenter.healthCenter);
        assertNull(campaignWithoutCenter.healthCenterId);
        assertEquals("Campana sintetica", campaignWithoutCenter.title);
        assertEquals("2026-11-10", campaignWithoutCenter.startDate);
        assertEquals("2026-11-12", campaignWithoutCenter.endDate);
        assertNotNull(campaignWithCenter.healthCenter);
        assertEquals(Integer.valueOf(4), campaignWithCenter.healthCenterId);
        assertEquals("Centro ficticio", campaignWithCenter.healthCenter.name);
    }
}
