package com.b2.cofferpreview.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {
    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void addCofferInfoTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        String translationKey = stack.getItem().getTranslationKey();
        boolean isCoffer = translationKey.contains("coffer");
        boolean isShulkerBox = translationKey.contains("shulker_box");

        // Only process coffers and shulker boxes
        if (!isCoffer && !isShulkerBox) {
            return;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("BlockEntityTag")) {
            NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");

            if (blockEntityTag.contains("Items")) {
                NbtList itemsList = blockEntityTag.getList("Items", 10);

                if (!itemsList.isEmpty()) {
                    List<Text> tooltip = cir.getReturnValue();

                    // Check if shift and/or alt is held
                    long window = MinecraftClient.getInstance().getWindow().getHandle();
                    boolean shiftHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
                    boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

                    // Remove BlockEntityTag JSON only when:
                    // - Advanced tooltips are enabled (F3+H)
                    // - Shift is held but Alt is NOT held
                    if (context.isAdvanced() && shiftHeld && !altHeld) {
                        // Remove any line that contains "BlockEntityTag:"
                        tooltip.removeIf(line -> line.getString().contains("BlockEntityTag:"));
                    }

                    // Only add custom tooltip lines for coffers (not shulker boxes)
                    if (isCoffer) {
                        // Count non-empty stacks
                        int stackCount = itemsList.size();

                        // Find where to insert our lines (after any existing F3+H debug info)
                        int insertIndex = tooltip.size();

                        // Look for common debug info patterns to insert after them
                        for (int i = tooltip.size() - 1; i >= 0; i--) {
                            String lineStr = tooltip.get(i).getString();
                            // Insert after lines like "furniture:coffer", durability, etc.
                            if (lineStr.contains("furniture:") ||
                                    lineStr.contains("minecraft:") ||
                                    lineStr.contains("Durability:") ||
                                    lineStr.contains("Unbreakable")) {
                                insertIndex = i + 1;
                                break;
                            }
                        }

                        // Add our custom info lines at the found position
                        tooltip.add(insertIndex, Text.literal("Contains " + stackCount + " stack(s)").formatted(Formatting.GRAY));
                        tooltip.add(insertIndex + 1, Text.literal("Left Alt+Left Shift: ").formatted(Formatting.GOLD)
                                .append(Text.literal("view full contents").formatted(Formatting.YELLOW)));
                    }
                }
            }
        }
    }
}