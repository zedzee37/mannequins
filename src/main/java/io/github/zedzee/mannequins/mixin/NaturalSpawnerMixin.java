package io.github.zedzee.mannequins.mixin;

import io.github.zedzee.mannequins.block.VillagerSkull;
import io.github.zedzee.mannequins.chunk.ChunkTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Shadow
    private static boolean isValidSpawnPostitionForType(ServerLevel level, MobCategory category, StructureManager structureManager, ChunkGenerator generator, MobSpawnSettings.SpawnerData data, BlockPos.MutableBlockPos pos, double distance) {
        return false;
    }

//    @Inject(method = "spawnCategoryForChunk", at = @At("HEAD"))
//    private static void gug(MobCategory category, ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci) {
//        Mannequins.LOGGER.info("gug");
//    }

    @Shadow
    private static boolean isValidPositionForMob(ServerLevel level, Mob mob, double distance) {
        return false;
    }

    @Redirect(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;isValidPositionForMob(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;D)Z")
    )
    private static boolean isCloseToLoaderOrPlayer(ServerLevel level, Mob mob, double distance) {
        boolean result = isValidPositionForMob(level, mob, distance);
        if (result) {
            return true;
        }

        double despawnDistance = mob.getType().getCategory().getDespawnDistance();
        return ChunkTracker.testPoweredLoaders(level, mob.getOnPos(), despawnDistance * despawnDistance);
    }

    @Inject(
            method = "isValidSpawnPostitionForType",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private static void checkCanDespawn(ServerLevel level,
                                        MobCategory category,
                                        StructureManager structureManager,
                                        ChunkGenerator generator,
                                        MobSpawnSettings.SpawnerData data,
                                        BlockPos.MutableBlockPos pos,
                                        double distance,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (distance == -1) return;

        boolean ret = cir.getReturnValue();
        if (ret) {
            return;
        }

        EntityType<?> entityType = data.type;
        double despawnDistance = entityType.getCategory().getDespawnDistance() * entityType.getCategory().getDespawnDistance();

        ChunkTracker tracker = ChunkTracker.getFromLevel(level);
        Set<BlockPos> loaders = tracker.getLoaders();

        for (BlockPos loader : loaders) {
            if (!VillagerSkull.isPowered(level, loader)) {
                continue;
            }

            double distanceSquared = loader.distSqr(pos);
            if (distanceSquared < despawnDistance) {
                boolean isValid = isValidSpawnPostitionForType(level, category, structureManager, generator, data, pos, -1);
                cir.setReturnValue(isValid);
                return;
            }
        }
    }
}
