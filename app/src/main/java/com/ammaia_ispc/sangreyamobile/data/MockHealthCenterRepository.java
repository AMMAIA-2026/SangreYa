package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.HealthCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MockHealthCenterRepository {
    private static final List<HealthCenter> HEALTH_CENTERS = buildHealthCenters();

    private MockHealthCenterRepository() {
    }

    public static List<HealthCenter> getHealthCenters() {
        List<HealthCenter> sorted = new ArrayList<>(HEALTH_CENTERS);
        Collections.sort(sorted, Comparator
                .comparing((HealthCenter center) -> center.neighborhood, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(center -> center.name, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public static HealthCenter findById(int id) {
        for (HealthCenter center : HEALTH_CENTERS) {
            if (center.id == id) {
                return center;
            }
        }
        return null;
    }

    private static List<HealthCenter> buildHealthCenters() {
        List<HealthCenter> centers = new ArrayList<>();
        centers.add(new HealthCenter(
                1,
                "Banco Central de Sangre de la Provincia de Córdoba",
                "Rosario de Santa Fe 374",
                "Centro",
                "Córdoba",
                "351 2480189",
                "https://ministeriodesalud.cba.gov.ar/banco-de-sangre/",
                "-31.4177671",
                "-64.1792522"));
        centers.add(new HealthCenter(
                3,
                "Banco de Sangre de la Universidad Nacional de Córdoba",
                "Enfermera Gordillo Gómez s/n",
                "Ciudad Universitaria",
                "Córdoba",
                "0351 4334121/28",
                "https://bancodesangre.turnos.unc.edu.ar/",
                "-31.4379571",
                "-64.1878064"));
        centers.add(new HealthCenter(
                4,
                "Fundación Banco Central de Sangre",
                "Caseros 1576",
                "Quinta Santa Ana",
                "Córdoba",
                "0351 4807373",
                "https://www.donarencordoba.com.ar/",
                "-31.4113956",
                "-64.2059516"));
        return centers;
    }
}
