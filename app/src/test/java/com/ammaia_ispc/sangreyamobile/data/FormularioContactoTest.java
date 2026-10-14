package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.helpers.ApiService;
import com.ammaia_ispc.sangreyamobile.model.ContactRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import retrofit2.Call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class FormularioContactoTest {
    // TC-UNIT-01
    @Test
    @SuppressWarnings("unchecked")
    public void validoEnviaSolicitud() {
        // Arrange: los máximos permitidos también son válidos.
        String nombre = text(20);
        String mensaje = text(500);
        ContactRequest request = new ContactRequest(nombre, "ana@example.test", "Consulta general", mensaje);
        ApiService api = mock(ApiService.class);
        Call<Void> call = mock(Call.class);
        ContactApiRepository.ContactCallback callback = mock(ContactApiRepository.ContactCallback.class);
        when(api.sendContact(any())).thenReturn(call);
        ArgumentCaptor<ContactRequest> sent = ArgumentCaptor.forClass(ContactRequest.class);

        // Act
        ContactApiRepository.sendContact(api, request, callback);

        // Assert
        verify(api).sendContact(sent.capture());
        assertEquals(JsonParser.parseString("{\"nombre_completo\":\"" + nombre
                        + "\",\"correo_electronico\":\"ana@example.test\",\"motivo\":\"Consulta general\","
                        + "\"mensaje\":\"" + mensaje + "\"}"),
                new Gson().toJsonTree(sent.getValue()));
        verify(call).enqueue(any());
    }

    // TC-UNIT-02
    @Test
    public void rechazaObligatoriosYLimites() {
        // Arrange
        ApiService api = mock(ApiService.class);
        ContactApiRepository.ContactCallback callback = mock(ContactApiRepository.ContactCallback.class);
        ContactRequest[] invalid = {
                new ContactRequest(" ", "ana@example.test", "Consulta general", "Consulta de prueba"),
                new ContactRequest("Ana", "", "Consulta general", "Consulta de prueba"),
                new ContactRequest("Ana", "ana@example.test", "", "Consulta de prueba"),
                new ContactRequest("Ana", "ana@example.test", "Consulta general", ""),
                new ContactRequest(text(21), "ana@example.test", "Consulta general", "Consulta de prueba"),
                new ContactRequest("Ana", "ana@example.test", "Consulta general", text(501)),
                new ContactRequest("Ana", "sin-arroba", "Consulta general", "Consulta de prueba")
        };

        // Act
        boolean anyAccepted = false;
        for (ContactRequest request : invalid) {
            anyAccepted |= ContactApiRepository.sendContact(api, request, callback);
        }

        // Assert
        assertFalse(anyAccepted);
        verifyNoInteractions(api, callback);
    }

    private String text(int length) {
        return new String(new char[length]).replace('\0', 'A');
    }
}
