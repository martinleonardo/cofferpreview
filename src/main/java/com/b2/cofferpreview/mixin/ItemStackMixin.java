package com.b2.cofferpreview.mixin;

import com.b2.cofferpreview.client.tooltip.CofferTooltipData;
import com.b2.cofferpreview.client.tooltip.CofferCollapsedTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.MinecraftClient;

import java.util.*;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void addCofferTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // Check if this is a coffer item from Let's Do Furniture mod
        String translationKey = stack.getItem().getTranslationKey();
        if (translationKey.contains("coffer")) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("BlockEntityTag")) {
                NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");

                if (blockEntityTag.contains("Items")) {
                    NbtList itemsList = blockEntityTag.getList("Items", 10);

                    // Only show tooltip if there are items
                    if (itemsList.isEmpty()) {
                        return; // No items, don't add custom tooltip
                    }

                    // Check if shift and/or alt is held
                    long window = MinecraftClient.getInstance().getWindow().getHandle();
                    boolean shiftHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
                    boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

                    if (shiftHeld && altHeld) {
                        // Shift + Alt: Show full grid view
                        DefaultedList<ItemStack> items = DefaultedList.ofSize(36, ItemStack.EMPTY);
                        for (int i = 0; i < itemsList.size(); i++) {
                            NbtCompound itemTag = itemsList.getCompound(i);
                            int slot = itemTag.getByte("Slot") & 255;
                            if (slot >= 0 && slot < items.size()) {
                                items.set(slot, ItemStack.fromNbt(itemTag));
                            }
                        }
                        cir.setReturnValue(Optional.of(new CofferTooltipData(items, 36)));

                    } else if (shiftHeld) {
                        // Shift only: Show collapsed view
                        Map<String, CofferCollapsedTooltipData.CollapsedStack> collapsedMap = new LinkedHashMap<>();

                        for (int i = 0; i < itemsList.size(); i++) {
                            NbtCompound itemTag = itemsList.getCompound(i);
                            ItemStack itemStack = ItemStack.fromNbt(itemTag);

                            if (!itemStack.isEmpty()) {
                                // Create a key based on item and NBT
                                String key = itemStack.getItem().toString();
                                if (itemStack.hasNbt()) {
                                    key += itemStack.getNbt().toString();
                                }

                                if (collapsedMap.containsKey(key)) {
                                    // Add to existing stack
                                    CofferCollapsedTooltipData.CollapsedStack existing = collapsedMap.get(key);
                                    collapsedMap.put(key, new CofferCollapsedTooltipData.CollapsedStack(
                                            existing.getStack(),
                                            existing.getTotalCount() + itemStack.getCount()
                                    ));
                                } else {
                                    // New stack type
                                    collapsedMap.put(key, new CofferCollapsedTooltipData.CollapsedStack(
                                            itemStack.copy(),
                                            itemStack.getCount()
                                    ));
                                }
                            }
                        }

                        List<CofferCollapsedTooltipData.CollapsedStack> collapsedList = new ArrayList<>(collapsedMap.values());
                        cir.setReturnValue(Optional.of(new CofferCollapsedTooltipData(collapsedList)));
                    }
                }
            }
        }
    }
}