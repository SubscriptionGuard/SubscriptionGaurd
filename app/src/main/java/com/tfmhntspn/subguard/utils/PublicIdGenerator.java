package com.tfmhntspn.subguard.utils;

import java.security.SecureRandom;

public class PublicIdGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom random =
            new SecureRandom();

    public static String generatePublicId() {

        StringBuilder id = new StringBuilder("SG-");

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(index));
        }

        return id.toString();
    }
}
