package com.b2.cofferpreview.client.tooltip;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CofferCollapsedTooltipData implements TooltipData {
    private final List<CollapsedStack> collapsedStacks;

    public CofferCollapsedTooltipData(List<CollapsedStack> collapsedStacks) {
        this.collapsedStacks = collapsedStacks;
    }

    public List<CollapsedStack> getCollapsedStacks() {
        return collapsedStacks;
    }

    public static class CollapsedStack {
        private final ItemStack stack;
        private final int totalCount;

        public CollapsedStack(ItemStack stack, int totalCount) {
            this.stack = stack;
            this.totalCount = totalCount;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public String getFormattedCount() {
            if (totalCount >= 1000) {
                if (totalCount % 1000 == 0) {
                    return (totalCount / 1000) + "k";
                } else {
                    return String.format("%.1fk", totalCount / 1000.0);
                }
            }
            return String.valueOf(totalCount);
        }
    }
}