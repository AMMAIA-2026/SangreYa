package com.ammaia_ispc.sangreyamobile.helpers;

import com.ammaia_ispc.sangreyamobile.model.HealthCenter;

import java.util.Objects;

public final class HealthCenterAddressAutofill {
    private Integer selectedCenterId;
    private boolean selectionInitialized;
    private boolean automaticAddress;

    public void onManualAddressChanged() {
        automaticAddress = false;
    }

    public String onCenterSelected(HealthCenter center, String currentAddress) {
        Integer centerId = center == null ? null : center.id;
        if (selectionInitialized && Objects.equals(selectedCenterId, centerId)) {
            return currentAddress;
        }
        selectedCenterId = centerId;
        selectionInitialized = true;

        // A saved or manually edited address does not belong to the autofill operation.
        if (!automaticAddress && !currentAddress.isEmpty()) {
            return currentAddress;
        }

        String nextAddress = center == null || center.address == null ? "" : center.address;
        automaticAddress = !nextAddress.isEmpty();
        return nextAddress;
    }
}
