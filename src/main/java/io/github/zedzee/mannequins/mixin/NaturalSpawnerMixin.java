package io.github.zedzee.mannequins.mixin;

import io.github.zedzee.mannequins.chunk.LoaderChunkTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Shadow
    private static boolean isValidSpawnPostitionForType(ServerLevel level, MobCategory category, StructureManager structureManager, ChunkGenerator generator, MobSpawnSettings.SpawnerData data, BlockPos.MutableBlockPos pos, double distance) {
        return false;
    }

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
        if (LoaderChunkTracker.loaderWithinDistance(level, mob.blockPosition(), despawnDistance * despawnDistance)) {
            return isValidPositionForMob(level, mob, 0);
        }

        return false;
    }

    @Redirect(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;isValidSpawnPostitionForType(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/biome/MobSpawnSettings$SpawnerData;Lnet/minecraft/core/BlockPos$MutableBlockPos;D)Z")
    )
    private static boolean canSpawn(ServerLevel level,
                                    MobCategory category,
                                    StructureManager structureManager,
                                    ChunkGenerator generator,
                                    MobSpawnSettings.SpawnerData data,
                                    BlockPos.MutableBlockPos pos,
                                    double distance) {
        boolean canSpawn = isValidSpawnPostitionForType(level, category, structureManager, generator, data, pos, distance);
        if (!canSpawn) {
            if (LoaderChunkTracker.loaderWithinDistance(
                    level, pos, category.getDespawnDistance() * category.getDespawnDistance()
            )) {
                return isValidSpawnPostitionForType(level, category, structureManager, generator, data, pos, 0);
            }
        }
        return canSpawn;
    }
}
