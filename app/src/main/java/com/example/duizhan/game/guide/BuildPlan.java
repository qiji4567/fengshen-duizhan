package com.example.duizhan.game.guide;

import com.example.duizhan.game.EquipmentSlot;
import com.example.duizhan.game.ItemType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BuildPlan {
    public final Map<EquipmentSlot, ItemType> targetLoadout;
    public final List<ItemType> purchasesInOrder;
    public final List<ItemType> affordablePurchases;
    public final ItemType nextPurchase;
    public final String nextPurchaseReason;
    public final ItemType consumableSuggestion;
    public final String consumableReason;
    public final int totalCost;
    public final int affordableCost;
    public final boolean alreadyOptimal;

    public BuildPlan(Map<EquipmentSlot, ItemType> targetLoadout,
                     List<ItemType> purchasesInOrder,
                     List<ItemType> affordablePurchases,
                     ItemType nextPurchase,
                     String nextPurchaseReason,
                     ItemType consumableSuggestion,
                     String consumableReason,
                     int totalCost,
                     int affordableCost,
                     boolean alreadyOptimal) {
        this.targetLoadout = Collections.unmodifiableMap(new LinkedHashMap<>(targetLoadout));
        this.purchasesInOrder = Collections.unmodifiableList(new ArrayList<>(purchasesInOrder));
        this.affordablePurchases = Collections.unmodifiableList(new ArrayList<>(affordablePurchases));
        this.nextPurchase = nextPurchase;
        this.nextPurchaseReason = nextPurchaseReason == null ? "" : nextPurchaseReason;
        this.consumableSuggestion = consumableSuggestion;
        this.consumableReason = consumableReason == null ? "" : consumableReason;
        this.totalCost = totalCost;
        this.affordableCost = affordableCost;
        this.alreadyOptimal = alreadyOptimal;
    }
}
