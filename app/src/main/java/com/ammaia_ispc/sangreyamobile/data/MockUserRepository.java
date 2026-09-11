package com.ammaia_ispc.sangreyamobile.data;

import com.ammaia_ispc.sangreyamobile.model.User;

import java.util.ArrayList;
import java.util.List;

public class MockUserRepository {

    private static final List<User> users = new ArrayList<>();

    static {
        users.add(new User(
                "1",
                "Juan Pérez",
                "juan.perez@email.com",
                "O+"
        ));

        users.add(new User(
                "2",
                "María Gómez",
                "maria.gomez@email.com",
                "A+"
        ));

        users.add(new User(
                "3",
                "Lucas Rodríguez",
                "lucas.rodriguez@email.com",
                "B-"
        ));

        users.add(new User(
                "4",
                "Sofía Fernández",
                "sofia.fernandez@email.com",
                "AB+"
        ));
    }

    public static List<User> getUsers() {
        return users;
    }

    public static void updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                return;
            }
        }
    }

    public static void deleteUser(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(userId)) {
                users.remove(i);
                return;
            }
        }
    }
}