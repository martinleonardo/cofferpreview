package com.b2.cofferpreview.client.tooltip;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CofferCollapsedTooltipComponent implements TooltipComponent {
    private final List<CofferCollapsedTooltipData.CollapsedStack> collapsedStacks;
    private final int cols;
    private final int rows;
    private static final int SLOT_SIZE = 18;

    public CofferCollapsedTooltipComponent(CofferCollapsedTooltipData data) {
        this.collapsedStacks = data.getCollapsedStacks();

        // Calculate grid size based on number of unique items
        int itemCount = collapsedStacks.size();
        if (itemCount == 0) {
            this.cols = 1;
            this.rows = 1;
        } else {
            // For single row, show only needed columns
            if (itemCount <= 9) {
                this.cols = itemCount;
                this.rows = 1;
            } else {
                // Multiple rows, use full width
                this.cols = 9;
                this.rows = (int) Math.ceil(itemCount / 9.0);
            }
        }
    }

    @Override
    public int getHeight() {
        return rows * SLOT_SIZE + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return cols * SLOT_SIZE + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        // Draw slot backgrounds
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * 9 + col;
                if (index >= collapsedStacks.size()) continue;

                int px = x + col * SLOT_SIZE + 1;
                int py = y + row * SLOT_SIZE + 1;

                // Draw slot background (dark gray)
                context.fill(px, py, px + 16, py + 16, 0xFF8B8B8B);

                // Draw darker inner slot (to create border effect)
                context.fill(px + 1, py + 1, px + 16, py + 16, 0xFF373737);
            }
        }

        // Draw collapsed items
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * 9 + col;
                if (index >= collapsedStacks.size()) continue;

                CofferCollapsedTooltipData.CollapsedStack collapsedStack = collapsedStacks.get(index);
                ItemStack stack = collapsedStack.getStack();

                int px = x + col * SLOT_SIZE + 1;
                int py = y + row * SLOT_SIZE + 1;

                // Draw the item
                context.drawItem(stack, px, py);

                // Only draw count overlay if total count is greater than 1
                if (collapsedStack.getTotalCount() > 1) {
                    String countText = collapsedStack.getFormattedCount();
                    context.drawItemInSlot(textRenderer, stack, px, py, countText);
                } else {
                    // Don't show count for single items
                    context.drawItemInSlot(textRenderer, stack, px, py, null);
                }
            }
        }
    }
}