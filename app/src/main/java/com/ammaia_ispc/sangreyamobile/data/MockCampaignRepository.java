package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.Campaign;
import com.ammaia_ispc.sangreyamobile.model.HealthCenter;
import java.util.ArrayList;
import java.util.List;

public final class MockCampaignRepository {
    private static final List<Campaign> CAMPAIGNS = buildCampaigns();

    private MockCampaignRepository() {
    }

    public static List<Campaign> getCampaigns() {
        return CAMPAIGNS;
    }

    public static void addCampaign(Campaign campaign) {
        CAMPAIGNS.add(campaign);
    }

    public static void updateCampaign(Campaign updated) {
        for (int index = 0; index < CAMPAIGNS.size(); index++) {
            if (CAMPAIGNS.get(index).id == updated.id) {
                CAMPAIGNS.set(index, updated);
                return;
            }
        }
    }

    public static int nextId() {
        return CAMPAIGNS.size() + 1;
    }

    private static List<Campaign> buildCampaigns() {
        List<Campaign> campaigns = new ArrayList<>();

        HealthCenter centralBloodBank = MockHealthCenterRepository.findById(1);
        HealthCenter universityBloodBank = MockHealthCenterRepository.findById(3);
        HealthCenter foundationBloodBank = MockHealthCenterRepository.findById(4);

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
