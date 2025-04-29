package com.tech.listify.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdvertisementCondition {
    NEW("Новое"),
    USED_PERFECT("Б/у (идеальное)"),
    USED_GOOD("Б/у (хорошее)"),
    USED_FAIR("Б/у (удовлетворительное)");

    private final String displayName;
}
