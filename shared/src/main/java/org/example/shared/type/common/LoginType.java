package org.example.shared.type.common;

import java.util.Arrays;
import java.util.Optional;

public enum LoginType {
    GOOGLE,
    KAKAO;

    public static Optional<LoginType> fromProvider(String provider) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(provider))
                .findFirst();
    }
}
