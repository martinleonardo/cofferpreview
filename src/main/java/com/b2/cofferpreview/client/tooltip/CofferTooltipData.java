package com.b2.cofferpreview.client.tooltip;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class CofferTooltipData implements TooltipData {
    private final DefaultedList<ItemStack> items;
    private final int slotCount;

    public CofferTooltipData(DefaultedList<ItemStack> items, int slotCount) {
        this.items = items;
        this.slotCount = slotCount;
    }

    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public int getSlotCount() {
        return slotCount;
    }
}