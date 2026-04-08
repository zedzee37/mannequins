package io.github.zedzee.mannequins.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.zedzee.mannequins.MannequinsClient;
import io.github.zedzee.mannequins.model.VillagerSkullModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
    @Inject(method = "createRoots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AllayModel;createBodyLayer()Lnet/minecraft/client/model/geom/builders/LayerDefinition;"))
    private static void addVillagerHeadModel(
            CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir,
            @Local(ordinal = 0)ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        LayerDefinition villagerHeadDefinition = LayerDefinition.create(
                VillagerSkullModel.createHeadModel(),
                64,
                64);
        builder.put(MannequinsClient.VILLAGER_SKULL_LAYER, villagerHeadDefinition);
    }
}
