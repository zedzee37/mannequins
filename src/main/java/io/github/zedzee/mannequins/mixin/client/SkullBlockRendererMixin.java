package io.github.zedzee.mannequins.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.zedzee.mannequins.Mannequins;
import io.github.zedzee.mannequins.model.VillagerSkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {
    @Unique
    private static final ResourceLocation VILLAGER_HEAD_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/villager/villager.png"
    );

    @Inject(method = "createSkullRenderers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/PiglinHeadModel;<init>(Lnet/minecraft/client/model/geom/ModelPart;)V",
                    shift = At.Shift.AFTER)
    )
    private static void addVillagerSkullRenderer(EntityModelSet entityModelSet,
                                                 CallbackInfoReturnable<Map<SkullBlock.Type, SkullModelBase>> cir,
                                                 @Local() ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder) {
        builder.put(Mannequins.VILLAGER_SKULL_TYPE, new VillagerSkullModel(entityModelSet.bakeLayer(Mannequins.VILLAGER_SKULL_LAYER)));
    }

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private static void villagerRenderType(SkullBlock.Type type,
                                           ResolvableProfile profile,
                                           CallbackInfoReturnable<RenderType> cir) {
        if (type == Mannequins.VILLAGER_SKULL_TYPE) {
            cir.setReturnValue(RenderType.entityCutoutNoCullZOffset(VILLAGER_HEAD_TEXTURE));
        }
    }
}
