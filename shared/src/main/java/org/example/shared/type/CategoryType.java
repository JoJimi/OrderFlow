package org.example.shared.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    ELECTRONICS("ELECTRONICS", "전자제품"),
    CLOTHING("CLOTHING", "의류"),
    BOOKS("BOOKS", "도서"),
    HOME("HOME", "홈/리빙"),
    SPORTS("SPORTS", "스포츠");

    private final String code;
    private final String displayName;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static CategoryType from(String code) {
        return Arrays.stream(CategoryType.values())
                .filter(category -> category.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid category code: " + code + ". Valid values are: " +
                                Arrays.toString(CategoryType.values())
                ));
    }

    public static CategoryType getRandomCategory() {
        CategoryType[] values = CategoryType.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(values.length);
        return values[randomIndex];
    }

    public static boolean isValid(String code) {
        return Arrays.stream(CategoryType.values())
                .anyMatch(category -> category.getCode().equals(code));
    }
}