package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.AuthUser;
import com.ammaia_ispc.sangreyamobile.model.UserUpdateRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ProfileMapperTest {
    // TC-UNIT-03
    @Test
    public void jsonMapeaPerfilSinPassword() {
        // Arrange
        Gson mapper = new Gson();
        String fixture = "{\"username\":\"donante.fixture\",\"email\":\"donante@example.test\","
                + "\"dni\":\"12345678\",\"nombre\":\"Ana\",\"apellido\":\"Prueba\","
                + "\"fecha_nacimiento\":\"1995-04-12\",\"password\":\"clave-sintetica\"}";

        // Act
        AuthUser profile = mapper.fromJson(fixture, AuthUser.class);
        JsonObject mapped = mapper.toJsonTree(profile).getAsJsonObject();
        UserUpdateRequest update = new UserUpdateRequest(profile.getUsername(), profile.getEmail(),
                profile.getDni(), profile.getName(), profile.getLastName(), profile.getBirthDate());
        JsonObject payload = mapper.toJsonTree(update).getAsJsonObject();

        // Assert
        assertEquals("donante@example.test", profile.getEmail());
        assertFalse(mapped.has("password"));
        assertFalse(payload.has("password"));
    }
}
