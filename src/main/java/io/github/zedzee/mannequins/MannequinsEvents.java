package io.github.zedzee.mannequins;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

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
}
