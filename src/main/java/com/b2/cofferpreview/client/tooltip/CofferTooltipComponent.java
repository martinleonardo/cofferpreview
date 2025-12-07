package com.b2.cofferpreview.client.tooltip;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class CofferTooltipComponent implements TooltipComponent {
    private final DefaultedList<ItemStack> items;
    private final int cols;
    private final int rows;
    private static final int SLOT_SIZE = 18;

    public CofferTooltipComponent(CofferTooltipData data) {
        this.items = data.getItems();

        // Find the highest slot index that contains an item
        int maxSlot = -1;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                maxSlot = i;
                break;
            }
        }

        // Calculate rows and columns based on actual content
        if (maxSlot == -1) {
            // No items (shouldn't happen since we check in mixin, but just in case)
            this.cols = 1;
            this.rows = 1;
        } else {
            // Calculate minimum rows needed (9 columns per row)
            this.rows = (maxSlot / 9) + 1;

            // For the last row, calculate actual columns needed
            int lastRowSlot = maxSlot % 9;

            // If not on the last row, use full 9 columns
            // If on last row, use only columns needed
            if (this.rows == 1) {
                this.cols = lastRowSlot + 1;
            } else {
                // Multiple rows - check if last row needs all 9 columns
                int lastRowMax = -1;
                int lastRowStart = (this.rows - 1) * 9;
                for (int i = lastRowStart; i < Math.min(lastRowStart + 9, items.size()); i++) {
                    if (!items.get(i).isEmpty()) {
                        lastRowMax = i - lastRowStart;
                    }
                }

                // Always show full width for multi-row inventories for consistency
                this.cols = 9;
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
        // Draw slot backgrounds with borders (like inventory GUI)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int px = x + col * SLOT_SIZE + 1;
                int py = y + row * SLOT_SIZE + 1;

                // Draw slot background (dark gray)
                context.fill(px, py, px + 16, py + 16, 0xFF8B8B8B);

                // Draw darker inner slot (to create border effect)
                context.fill(px + 1, py + 1, px + 16, py + 16, 0xFF373737);
            }
        }

        // Draw items on top of the grid
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * 9 + col;
                if (index >= items.size()) continue;

                ItemStack stack = items.get(index);
                if (stack.isEmpty()) continue;

                int px = x + col * SLOT_SIZE + 1;
                int py = y + row * SLOT_SIZE + 1;

                context.drawItem(stack, px, py);
                context.drawItemInSlot(textRenderer, stack, px, py);
            }
        }
    }
}