package com.tech.listify.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ACTIVE("Активно"),
    INACTIVE("Неактивно"),
    SOLD("Продано");

    private final String displayName;
}
