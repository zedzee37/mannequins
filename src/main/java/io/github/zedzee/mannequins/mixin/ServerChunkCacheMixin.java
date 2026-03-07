package io.github.zedzee.mannequins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.zedzee.mannequins.chunk.LoaderChunkTracker;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {
    @Final
    @Unique
    private static final double MAX_DISTANCE = 16384.0;

    @Shadow
    @Final
    public ServerLevel level;

    @Shadow
    @Final
    public ChunkMap chunkMap;

    @Shadow
    @Final
    private DistanceManager distanceManager;

    @Shadow
    @Nullable
    private NaturalSpawner.SpawnState lastSpawnState;

    @Shadow
    private boolean spawnFriendlies;

    @Shadow
    private boolean spawnEnemies;

    @Shadow
    protected abstract void getFullChunk(long chunkPos, Consumer<LevelChunk> fullChunkGetter);

    @Inject(
            method = "tickChunks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z")
    )
    public void chunkLoaderSpawn(
            CallbackInfo ci,
            @Local()LevelChunk levelChunk,
            @Local()ChunkPos chunkPos,
            @Local(ordinal = 1)boolean flag) {
        boolean willVanillaSpawn = (
                this.level.isNaturalSpawningAllowed(chunkPos) &&
                this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkPos)
        ) || this.distanceManager.shouldForceTicks(chunkPos.toLong());

        if (willVanillaSpawn) {
            return;
        }

        int x = SectionPos.sectionToBlockCoord(chunkPos.x, 8);
        int z = SectionPos.sectionToBlockCoord(chunkPos.z, 8);

        if (LoaderChunkTracker.testPoweredLoaders(level, (loader) -> {
            int dx = loader.getX() - x;
            int dz = loader.getZ() - z;
            int distanceSquared = (dx*dx) + (dz*dz);
            return distanceSquared < MAX_DISTANCE;
        }) && this.lastSpawnState != null) {
            NaturalSpawner.spawnForChunk(
                    level,
                    levelChunk,
                    this.lastSpawnState,
                    this.spawnFriendlies,
                    this.spawnEnemies,
                    flag
            );
        }
    }
}
