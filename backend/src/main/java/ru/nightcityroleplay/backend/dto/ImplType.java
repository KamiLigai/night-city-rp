package ru.nightcityroleplay.backend.dto;

import lombok.Getter;

@Getter
public enum ImplType {
    FRONTAL_LOBE("Лобная доля", 3),
    OPTICAL_SYSTEM("Оптическая система", 1),
    CIRCULATORY_SYSTEM("Кровеносная система", 3),
    IMMUNE_SYSTEM("Иммунная система", 2),
    NERVOUS_SYSTEM("Нервная система", 2),
    OPERATING_SYSTEM("Операционная система", 1),
    SKIN("Кожа", 3),
    SKELETON("Скелет", 2),
    PALMS("Ладони", 2),
    ARMS("Руки", 1),
    LEGS("Ноги", 1),
    OTHER("Другое", 666);

    private final String displayName;
    private final int limit;

    ImplType(String displayName, int limit) {
        this.displayName = displayName;
        this.limit = limit;
    }

}
