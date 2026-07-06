package com.example.duizhan.game;

public enum EquipmentSlot {
    WEAPON,
    ARMOR,
    BOOTS,
    HAT,
    RELIC,
    CONSUMABLE;

    public boolean isConsumable() {
        return this == CONSUMABLE;
    }
}
