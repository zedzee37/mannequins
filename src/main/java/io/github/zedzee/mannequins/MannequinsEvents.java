package io.github.zedzee.mannequins;

import io.github.zedzee.mannequins.block.VillagerSkull;
import io.github.zedzee.mannequins.chunk.LoaderChunkTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Set;

@EventBusSubscriber(modid = Mannequins.MODID)
public class MannequinsEvents {
    @SubscribeEvent
    public static void addVillagerHead(BlockEntityTypeAddBlocksEvent event) {
        event.modify(
                BlockEntityType.SKULL,
                Mannequins.VILLAGER_SKULL.get()
        );
    }

    @SubscribeEvent
    public static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(Mannequins.TICKET_CONTROLLER);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load loadEvent) {
        LevelAccessor levelAccessor = loadEvent.getLevel();
        if (!(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }

        LoaderChunkTracker.forceChunks(serverLevel);
    }

    @SubscribeEvent
    public static void onTryDespawn(MobDespawnEvent event) {
        ServerLevel serverLevel = event.getLevel().getLevel();

        Entity entity = event.getEntity();
        EntityType<?> entityType = entity.getType();
        double despawnDistance = entityType.getCategory().getDespawnDistance() * entityType.getCategory().getDespawnDistance();

        LoaderChunkTracker tracker = LoaderChunkTracker.getFromLevel(serverLevel);
        Set<BlockPos> loaders = tracker.getLoaders();
        for (BlockPos loader : loaders) {
            if (!VillagerSkull.isPowered(serverLevel, loader)) {
                continue;
            }

            double distanceSquared = loader.distSqr(entity.getOnPos());
            if (distanceSquared < despawnDistance) {
                event.setResult(MobDespawnEvent.Result.DENY);
                return;
            }
        }

        event.setResult(MobDespawnEvent.Result.DEFAULT);
    }
}
