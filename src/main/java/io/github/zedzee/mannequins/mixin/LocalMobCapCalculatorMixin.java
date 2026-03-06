package io.github.zedzee.mannequins.mixin;

import io.github.zedzee.mannequins.chunk.ChunkTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LocalMobCapCalculator.class)
public class LocalMobCapCalculatorMixin {
    @Shadow
    @Final
    private ChunkMap chunkMap;
    @Unique
    private Map<BlockPos, LocalMobCapCalculator.MobCounts> mannequins$loaderCountMap = new HashMap<>();

    @Unique
    private static final double SPAWNER_DISTANCE = 16384.0;

    @Inject(method = "canSpawn", at = @At("RETURN"), cancellable = true)
    public void countLoaders(MobCategory category, ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        boolean result = cir.getReturnValue();
        if (result) {
            return;
        }

        int x = SectionPos.sectionToBlockCoord(pos.x);
        int z = SectionPos.sectionToBlockCoord(pos.z);

        if (ChunkTracker.testPoweredLoaders(chunkMap.level, (loader) -> {
            int dx = loader.getX() - x;
            int dz = loader.getZ() - z;
            int distance = dx*dx + dz*dz;

            if (distance > SPAWNER_DISTANCE) {
                return false;
            }

            LocalMobCapCalculator.MobCounts mobCounts = mannequins$loaderCountMap.get(loader);
            return mobCounts == null || mobCounts.canSpawn(category);
        })) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "addMob", at = @At("RETURN"))
    public void addMobToLoaders(ChunkPos pos, MobCategory category, CallbackInfo ci) {
        int x = SectionPos.sectionToBlockCoord(pos.x);
        int z = SectionPos.sectionToBlockCoord(pos.z);

        ChunkTracker.testPoweredLoaders(chunkMap.level, loader -> {
            int dx = loader.getX() - x;
            int dz = loader.getZ() - z;
            int distance = dx*dx + dz*dz;

            if (distance > SPAWNER_DISTANCE) {
                return false;
            }

            mannequins$loaderCountMap.computeIfAbsent(loader, l -> new LocalMobCapCalculator.MobCounts()).add(category);
            return false;
        });
    }
}
