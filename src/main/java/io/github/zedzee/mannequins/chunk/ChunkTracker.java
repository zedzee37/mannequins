package io.github.zedzee.mannequins.chunk;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.zedzee.mannequins.Mannequins;
import net.minecraft.Util;
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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ChunkTracker extends SavedData {
    public Map<ChunkPos, Integer> forceLoadedChunks;
    public static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.INT_STREAM.comapFlatMap(
                            stream -> Util.fixedSize(stream, 2).map(i -> new ChunkPos(i[0], i[1])),
                            chunkPos -> IntStream.of(chunkPos.x, chunkPos.z)
                    );


    public static final String CHUNKS_KEY = "chunks";
    public static final String CHUNK_POS_KEY = "pos";
    public static final String COUNT_KEY = "count";

    public ChunkTracker() {
        this.forceLoadedChunks = new HashMap<>();
    }

    public ChunkTracker(Map<ChunkPos, Integer> forceLoadedChunks) {
        this.forceLoadedChunks =forceLoadedChunks;
    }

    public void forceChunk(ServerLevel level, BlockPos owner, ChunkPos pos) {
        int count = forceLoadedChunks.getOrDefault(pos, 0);
        forceLoadedChunks.put(pos, ++count);

        Mannequins.TICKET_CONTROLLER.forceChunk(level, owner, pos.x, pos.z, true, true);

        setDirty();
    }

    public void unForceChunk(ServerLevel level, BlockPos owner, ChunkPos pos) {
        if (!forceLoadedChunks.containsKey(pos)) {
            return;
        }

        int count = forceLoadedChunks.getOrDefault(pos, 1);
        count--;

        if (count <= 0) {
            forceLoadedChunks.remove(pos);
            Mannequins.TICKET_CONTROLLER.forceChunk(level, owner, pos.x, pos.z, false, false);
        } else {
            forceLoadedChunks.put(pos, count);
        }

        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag tags = tag.getList(CHUNKS_KEY, Tag.TAG_COMPOUND);

        for (ChunkPos pos : forceLoadedChunks.keySet()) {
            DataResult<Tag> chunkPosResult = CHUNK_POS_CODEC.encodeStart(NbtOps.INSTANCE, pos);

            if (chunkPosResult.isError()) {
                Mannequins.LOGGER.error(chunkPosResult.error().get().message());
                return tag;
            }

            CompoundTag compoundTag = new CompoundTag();

            Tag chunkPosTag = chunkPosResult.getOrThrow();
            compoundTag.put(CHUNK_POS_KEY, chunkPosTag);
            compoundTag.putInt(COUNT_KEY, forceLoadedChunks.getOrDefault(pos, 0));

            tags.add(compoundTag);
        }
        return tag;
    }

    public static ChunkTracker load(CompoundTag tag) {
        if (!tag.contains(CHUNKS_KEY)) {
            return new ChunkTracker();
        }

        ListTag listTag = tag.getList(CHUNKS_KEY, Tag.TAG_COMPOUND);
        Map<ChunkPos, Integer> forcedChunks = new HashMap<>();
        for (Tag maybeCompound : listTag) {
            if (!(maybeCompound instanceof CompoundTag compoundTag)) {
                continue;
            }

            int count = compoundTag.getInt(COUNT_KEY);
            DataResult<Pair<ChunkPos, Tag>> maybeChunkPos = CHUNK_POS_CODEC.decode(NbtOps.INSTANCE, compoundTag.get(CHUNK_POS_KEY));

            if (maybeChunkPos.isError()) {
                Mannequins.LOGGER.error(maybeChunkPos.error().get().message());
            }

            ChunkPos chunkPos = maybeChunkPos.getOrThrow().getFirst();
            forcedChunks.put(chunkPos, count);
        }

        return new ChunkTracker(forcedChunks);
    }
}
