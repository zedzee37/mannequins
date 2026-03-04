package io.github.zedzee.mannequins;

import io.github.zedzee.mannequins.chunk.ChunkTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

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

        ChunkTracker.forceChunks(serverLevel);
    }
}
