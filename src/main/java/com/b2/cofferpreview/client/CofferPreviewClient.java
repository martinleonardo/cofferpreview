package com.b2.cofferpreview.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import com.b2.cofferpreview.client.tooltip.CofferTooltipComponent;
import com.b2.cofferpreview.client.tooltip.CofferTooltipData;
import com.b2.cofferpreview.client.tooltip.CofferCollapsedTooltipComponent;
import com.b2.cofferpreview.client.tooltip.CofferCollapsedTooltipData;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

@Environment(EnvType.CLIENT)
public class CofferPreviewClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register tooltip component renderers
		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof CofferTooltipData cofferData) {
				return new CofferTooltipComponent(cofferData);
			}
			if (data instanceof CofferCollapsedTooltipData collapsedData) {
				return new CofferCollapsedTooltipComponent(collapsedData);
			}
			return null;
		});

		System.out.println("[CofferPreview] Mod initialized (client side)");
	}
}