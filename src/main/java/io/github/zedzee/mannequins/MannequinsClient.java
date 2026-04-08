package io.github.zedzee.mannequins;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Mannequins.MODID, value = Dist.CLIENT)
public class MannequinsClient {
    public static final ModelLayerLocation VILLAGER_SKULL_LAYER =
            new ModelLayerLocation(Mannequins.resourceLocation("villager_head"), "main");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }
}