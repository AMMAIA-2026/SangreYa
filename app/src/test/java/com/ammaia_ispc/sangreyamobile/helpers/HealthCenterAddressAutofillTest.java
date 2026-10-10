package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.HealthCenter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HealthCenterAddressAutofillTest {
    private final HealthCenter centerA = center(1, "Dirección A");
    private final HealthCenter centerB = center(2, "Dirección B");
    private final HealthCenterAddressAutofill autofill = new HealthCenterAddressAutofill();

    @Test
    public void completaDireccionVaciaAlSeleccionarUnCentro() {
        String address = autofill.onCenterSelected(null, "");

        assertEquals("Dirección A", autofill.onCenterSelected(centerA, address));
    }

    // BUG-029: A -> Sin centro asignado limpia solo la dirección automática.
    @Test
    public void limpiaDireccionAutomaticaAlDeseleccionarElCentro() {
        String address = autofill.onCenterSelected(centerA, "");

        assertEquals("", autofill.onCenterSelected(null, address));
    }

    // BUG-030: A -> B reemplaza la dirección automática de A por la de B.
    @Test
    public void actualizaDireccionAutomaticaAlCambiarDeCentro() {
        String address = autofill.onCenterSelected(centerA, "");

        assertEquals("Dirección B", autofill.onCenterSelected(centerB, address));
    }

    @Test
    public void conservaDireccionIngresadaAntesDeSeleccionarUnCentro() {
        String address = "Sede elegida manualmente";
        autofill.onManualAddressChanged();

        assertEquals(address, autofill.onCenterSelected(centerA, address));
        assertEquals(address, autofill.onCenterSelected(centerB, address));
        assertEquals(address, autofill.onCenterSelected(null, address));
    }

    @Test
    public void conservaDireccionEditadaAlDeseleccionarElCentro() {
        autofill.onCenterSelected(centerA, "");
        autofill.onManualAddressChanged();

        assertEquals("Dirección A, entrada lateral",
                autofill.onCenterSelected(null, "Dirección A, entrada lateral"));
    }

    @Test
    public void conservaDireccionEditadaAlCambiarDeCentro() {
        autofill.onCenterSelected(centerA, "");
        autofill.onManualAddressChanged();

        assertEquals("Dirección alternativa",
                autofill.onCenterSelected(centerB, "Dirección alternativa"));
    }

    @Test
    public void noConfundeUnaEdicionManualConElValorAutomaticoOriginal() {
        String address = autofill.onCenterSelected(centerA, "");
        autofill.onManualAddressChanged();

        // El usuario puede editar y volver a escribir exactamente la misma dirección.
        assertEquals(address, autofill.onCenterSelected(centerB, address));
        assertEquals(address, autofill.onCenterSelected(null, address));
    }

    @Test
    public void conservaUbicacionGuardadaAlCargarUnaCampaniaParaEditar() {
        String savedAddress = "Ubicación guardada";

        // El selector se carga primero vacío y luego recibe los centros de la API.
        assertEquals(savedAddress, autofill.onCenterSelected(null, savedAddress));
        assertEquals(savedAddress, autofill.onCenterSelected(centerA, savedAddress));
        assertEquals(savedAddress, autofill.onCenterSelected(centerB, savedAddress));
        assertEquals(savedAddress, autofill.onCenterSelected(null, savedAddress));
    }

    @Test
    public void limpiaDireccionAnteriorSiElNuevoCentroNoTieneDireccion() {
        String address = autofill.onCenterSelected(centerA, "");

        assertEquals("", autofill.onCenterSelected(center(3, null), address));
        assertEquals("Dirección B", autofill.onCenterSelected(centerB, ""));
    }

    @Test
    public void limpiaDireccionAnteriorSiElNuevoCentroTieneDireccionVacia() {
        String address = autofill.onCenterSelected(centerA, "");

        assertEquals("", autofill.onCenterSelected(center(3, ""), address));
    }

    @Test
    public void noReponeDireccionBorradaAnteUnaNotificacionDelMismoCentro() {
        autofill.onCenterSelected(centerA, "");
        autofill.onManualAddressChanged();

        assertEquals("", autofill.onCenterSelected(center(1, "Dirección A"), ""));
        assertEquals("Dirección B", autofill.onCenterSelected(centerB, ""));
    }

    @Test
    public void permiteAutocompletarNuevamenteDespuesDeDeseleccionar() {
        String address = autofill.onCenterSelected(centerA, "");
        address = autofill.onCenterSelected(centerB, address);
        assertEquals("Dirección B", address);
        address = autofill.onCenterSelected(null, address);
        assertEquals("", address);

        assertEquals("Dirección A", autofill.onCenterSelected(centerA, address));
    }

    private static HealthCenter center(int id, String address) {
        return new HealthCenter(id, "Centro " + id, address, "Barrio", "Ciudad", "", "", "", "");
    }
}
