package io.github.zedzee.mannequins.block;

import io.github.zedzee.mannequins.Mannequins;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;

public class VillagerSkull extends SkullBlock {
    public VillagerSkull(Properties properties) {
        super(Mannequins.VILLAGER_SKULL_TYPE, properties);
    }

    public static class VillagerSkullType implements Type {
        @Override
        public @NotNull String getSerializedName() {
            return "villager";
        }
    }
}
