package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import java.util.ArrayList;
import java.util.List;

public final class MockCampaignRepository {
    private MockCampaignRepository() {
    }

    public static List<Campaign> getCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();

        HealthCenter centralBloodBank = new HealthCenter(
                1,
                "Banco Central de Sangre de la Provincia de Córdoba",
                "Rosario de Santa Fe 374",
                "Centro",
                "Córdoba",
                "351 2480189",
                "https://ministeriodesalud.cba.gov.ar/banco-de-sangre/",
                "-31.4177671",
                "-64.1792522");
        HealthCenter universityBloodBank = new HealthCenter(
                3,
                "Banco de Sangre de la Universidad Nacional de Córdoba",
                "Enfermera Gordillo Gómez s/n",
                "Ciudad Universitaria",
                "Córdoba",
                "0351 4334121/28",
                "https://bancodesangre.turnos.unc.edu.ar/",
                "-31.4379571",
                "-64.1878064");
        HealthCenter foundationBloodBank = new HealthCenter(
                4,
                "Fundación Banco Central de Sangre",
                "Caseros 1576",
                "Quinta Santa Ana",
                "Córdoba",
                "0351 4807373",
                "https://www.donarencordoba.com.ar/",
                "-31.4113956",
                "-64.2059516");

        campaigns.add(new Campaign(
                1,
                "Donación de Sangre Hospital Central",
                "Campaña solidaria para pacientes en cirugías y emergencias críticas. La jornada recibe donantes voluntarios de todos los grupos sanguíneos.",
                "Hospital Central Córdoba",
                centralBloodBank.id,
                centralBloodBank,
                "2026-09-10",
                "2026-09-15",
                40,
                24,
                "Activa",
                "Activa"));
        campaigns.add(new Campaign(
                2,
                "Jornada Solidaria Barrio Güemes",
                "Recolección de sangre destinada a hospitales públicos de la ciudad, con atención de profesionales de hemoterapia.",
                "Centro Cultural Güemes",
                foundationBloodBank.id,
                foundationBloodBank,
                "2026-09-18",
                "2026-09-20",
                30,
                9,
                "Proximamente",
                "Proximamente"));
        campaigns.add(new Campaign(
                3,
                "Maratón de Donación Universitaria",
                "Evento organizado junto a la comunidad universitaria para fomentar la donación voluntaria y habitual.",
                "Ciudad Universitaria Córdoba",
                universityBloodBank.id,
                universityBloodBank,
                "2026-09-20",
                "2026-09-21",
                25,
                3,
                "Proximamente",
                "Proximamente"));
        campaigns.add(new Campaign(
                4,
                "Campaña Emergencia Pediátrica",
                "Donación urgente para acompañar las necesidades de sangre del Hospital de Niños de la ciudad.",
                "Hospital de Niños",
                null,
                null,
                "2026-08-12",
                "2026-08-14",
                null,
                41,
                "Finalizada",
                "Finalizada"));
        campaigns.add(new Campaign(
                5,
                "Colecta Solidaria Barrio Norte",
                "Jornada de donación para acompañar las necesidades de los hospitales de la zona norte.",
                "Club Atlético Norte",
                centralBloodBank.id,
                centralBloodBank,
                "2026-09-12",
                "2026-09-14",
                35,
                12,
                "Activa",
                "Activa"));
        campaigns.add(new Campaign(
                6,
                "Donación Solidaria Zona Sur",
                "Campaña abierta a vecinos de la zona sur para reforzar las reservas de sangre.",
                "Centro Vecinal Zona Sur",
                foundationBloodBank.id,
                foundationBloodBank,
                "2026-09-28",
                "2026-09-30",
                45,
                8,
                "Proximamente",
                "Proximamente"));

        return campaigns;
    }
}
