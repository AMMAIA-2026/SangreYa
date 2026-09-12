package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.ContactMessage;

import java.util.ArrayList;
import java.util.List;

public final class MockContactRepository {

    private MockContactRepository() {
    }

    public static List<ContactMessage> getContacts() {
        List<ContactMessage> contacts = new ArrayList<>();
        contacts.add(new ContactMessage("Juan Pérez", "Hace 2 h", "Consulta general",
                "Hola, quería saber si puedo donar si tomo medicación para la presión arterial.", false));
        contacts.add(new ContactMessage("Laura Fernández", "Hace 1 día", "Problema técnico",
                "No me deja inscribirme a la campaña de Plaza San Martín, me tira un error al confirmar.", false));
        contacts.add(new ContactMessage("Martín Sosa", "Hace 3 días", "Sugerencia",
                "Estaría bueno poder ver en un mapa todas las campañas cercanas a mi domicilio.", true));
        return contacts;
    }
}