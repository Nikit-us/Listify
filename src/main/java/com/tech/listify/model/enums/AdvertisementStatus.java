package com.tech.listify.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdvertisementStatus {
    ACTIVE("Активно"),
    INACTIVE("Неактивно"),
    SOLD("Продано");

    private final String displayName;
}
