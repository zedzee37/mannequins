package io.github.zedzee.mannequins.block;

import io.github.zedzee.mannequins.Mannequins;
import io.github.zedzee.mannequins.chunk.LoaderChunkTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class VillagerSkull extends SkullBlock {
    public VillagerSkull(Properties properties) {
        super(Mannequins.VILLAGER_SKULL_TYPE, properties);
    }

    @Override
    protected void onPlace(@NotNull BlockState state,
                           @NotNull Level level,
                           @NotNull BlockPos pos,
                           @NotNull BlockState oldState,
                           boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        LoaderChunkTracker tracker = LoaderChunkTracker.getFromLevel(serverLevel);
        tracker.addLoader(serverLevel, pos);
    }

    @Override
    protected void onRemove(@NotNull BlockState state,
                            @NotNull Level level,
                            @NotNull BlockPos pos,
                            @NotNull BlockState newState,
                            boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        LoaderChunkTracker tracker = LoaderChunkTracker.getFromLevel(serverLevel);
        tracker.removeLoader(serverLevel, pos);
    }

    public static boolean isPowered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.getBestNeighborSignal(pos) > 0;
    }

    public static class VillagerSkullType implements Type {
        @Override
        public @NotNull String getSerializedName() {
            return "villager";
        }
    }
}
