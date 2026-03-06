package io.github.zedzee.mannequins.chunk;

import io.github.zedzee.mannequins.Mannequins;
import io.github.zedzee.mannequins.block.VillagerSkull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChunkTracker extends SavedData {
    public static final String DATA_STORAGE_KEY = "chunk_tracker";
    public static final SavedData.Factory<ChunkTracker> FACTORY = new SavedData.Factory<>(
            ChunkTracker::new,
            ChunkTracker::load
    );

    private static final String LOADERS_KEY = "chunks";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";

    private final Set<BlockPos> loaders;

    public ChunkTracker() {
        this.loaders = new HashSet<>();
    }

    public ChunkTracker(Set<BlockPos> loaders) {
        this.loaders = loaders;
    }

    public void addLoader(ServerLevel level, BlockPos owner) {
        ChunkPos pos = level.getChunk(owner).getPos();
        loaders.add(owner);
        Mannequins.TICKET_CONTROLLER.forceChunk(level, owner, pos.x, pos.z, true, true);
        setDirty();
    }

    public void removeLoader(ServerLevel level, BlockPos owner) {
        if (!loaders.contains(owner)) {
            return;
        }

        ChunkPos pos = level.getChunk(owner).getPos();
        loaders.remove(owner);
        Mannequins.TICKET_CONTROLLER.forceChunk(level, owner, pos.x, pos.z, false, true);
        setDirty();
    }

    public Set<BlockPos> getLoaders() {
        return this.loaders;
    }

    public static void forceChunks(ServerLevel level) {
        ChunkTracker tracker = getFromLevel(level);
        tracker.loaders.forEach(pos -> tracker.addLoader(level, pos));
    }

    public static boolean testPoweredLoaders(ServerLevel level, Predicate<BlockPos> comparison) {
        ChunkTracker tracker = getFromLevel(level);

        for (BlockPos loader : tracker.getLoaders()) {
            if (!VillagerSkull.isPowered(level, loader)) {
                continue;
            }

            if (comparison.test(loader)) {
                return true;
            }
        }

        return false;
    }

    public static boolean loaderWithinDistance(ServerLevel level, BlockPos pos, double maxDistance) {
        return testPoweredLoaders(level, (loader) -> loader.distSqr(pos) < maxDistance);
    }

    public static boolean forEachPoweredLoader(ServerLevel level, Consumer<BlockPos> consumer) {
        for (BlockPos loader : getFromLevel(level).getLoaders()) {
            if (VillagerSkull.isPowered(level, loader)) {
                consumer.accept(loader);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag tags = tag.getList(LOADERS_KEY, Tag.TAG_COMPOUND);

        for (BlockPos pos : loaders) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(X_KEY, pos.getX());
            compoundTag.putInt(Y_KEY, pos.getY());
            compoundTag.putInt(Z_KEY, pos.getZ());
            tags.add(compoundTag);
        }

        tag.put(LOADERS_KEY, tags);
        return tag;
    }

    public static ChunkTracker load(CompoundTag tag, HolderLookup.Provider provider) {
        if (!tag.contains(LOADERS_KEY)) {
            return new ChunkTracker();
        }

        ListTag listTag = tag.getList(LOADERS_KEY, Tag.TAG_COMPOUND);
        HashSet<BlockPos> loaders = new HashSet<>();
        for (Tag maybeCompound : listTag) {
            if (!(maybeCompound instanceof CompoundTag compoundTag)) {
                continue;
            }

            int x = compoundTag.getInt(X_KEY);
            int y = compoundTag.getInt(Y_KEY);
            int z = compoundTag.getInt(Z_KEY);
            loaders.add(new BlockPos(x, y, z));
        }

        return new ChunkTracker(loaders);
    }

    public static ChunkTracker getFromLevel(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_STORAGE_KEY);
    }
}
