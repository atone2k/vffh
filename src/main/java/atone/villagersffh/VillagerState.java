package atone.villagersffh;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class VillagerState extends PersistentState {

    public UUID entityToFollow = UUID.randomUUID();
    public int followingTimer = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putUuid("entityToFollow", entityToFollow);
        nbt.putInt("followingTimer", followingTimer);
        return nbt;
    }

    public static VillagerState createFromNbt(NbtCompound tag) {
        VillagerState villagerState = new VillagerState();
        villagerState.entityToFollow = tag.getUuid("entityToFollow");
        villagerState.followingTimer = tag.getInt("followingTimer");
        return villagerState;
    }
}
