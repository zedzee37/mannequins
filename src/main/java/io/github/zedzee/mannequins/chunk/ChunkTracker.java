package io.github.zedzee.mannequins.chunk;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import io.github.zedzee.mannequins.Mannequins;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChunkTracker extends SavedData {
    public static final String LOADERS_KEY = "chunks";
    public static final String DATA_STORAGE_KEY = "chunk_tracker";
    public static final SavedData.Factory<ChunkTracker> FACTORY = new SavedData.Factory<>(
            ChunkTracker::new,
            ChunkTracker::load
    );

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

    public static void forceChunks(ServerLevel level) {
        ChunkTracker tracker = getFromLevel(level);
        tracker.loaders.forEach(pos -> tracker.addLoader(level, pos));
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag tags = tag.getList(LOADERS_KEY, Tag.TAG_COMPOUND);

        for (BlockPos pos : loaders) {
            DataResult<Tag> blockPosResult = BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos);

            if (blockPosResult.isError()) {
                Mannequins.LOGGER.error(blockPosResult.error().get().message());
                return tag;
            }

            tags.add(blockPosResult.getOrThrow());
        }
        return tag;
    }

    public static ChunkTracker load(CompoundTag tag, HolderLookup.Provider provider) {
        if (!tag.contains(LOADERS_KEY)) {
            return new ChunkTracker();
        }

        ListTag listTag = tag.getList(LOADERS_KEY, Tag.TAG_COMPOUND);
        HashSet<BlockPos> loaders = new HashSet<>();
        for (Tag maybeCompound : listTag) {
            DataResult<Pair<BlockPos, Tag>> maybeBlockPos = BlockPos.CODEC.decode(NbtOps.INSTANCE, maybeCompound);

            if (maybeBlockPos.isError()) {
                Mannequins.LOGGER.error(maybeBlockPos.error().get().message());
            }

            loaders.add(maybeBlockPos.getOrThrow().getFirst());
        }

        return new ChunkTracker(loaders);
    }

    public static ChunkTracker getFromLevel(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_STORAGE_KEY);
    }
}
