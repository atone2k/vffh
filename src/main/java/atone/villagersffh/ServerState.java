package atone.villagersffh;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class ServerState extends PersistentState {
    public HashMap<UUID, VillagerState> villagers = new HashMap<>();

    public static ServerState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        ServerState serverState = persistentStateManager.getOrCreate(ServerState::createFromNbt, ServerState::new, VillagersFFHMain.MOD_ID);
        return serverState;
    }

    public static VillagerState getVillagerState(VillagerEntity villager) {
        ServerState serverState = getServerState(villager.getServer());

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        VillagerState playerState = serverState.villagers.computeIfAbsent(villager.getUuid(), uuid -> new VillagerState());

        return playerState;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound villagersNbt = new NbtCompound();
        villagers.forEach((UUID, villagerState) -> {
            NbtCompound villagerStateNbt = new NbtCompound();

            villagerStateNbt.putUuid("entityToFollow", villagerState.entityToFollow);
            villagerStateNbt.putInt("followingTimer", villagerState.followingTimer);

            villagersNbt.put(String.valueOf(UUID), villagerStateNbt);
        });
        nbt.put("villagers", villagersNbt);

        return nbt;
    }

    public static ServerState createFromNbt(NbtCompound tag) {
        VillagersFFHMain.LOGGER.info("Creating villager state from nbt..");
        ServerState serverState = new ServerState();

        NbtCompound villagersTag = tag.getCompound("villagers");
        villagersTag.getKeys().forEach(key -> {
            VillagerState villagerState = new VillagerState();

            villagerState.entityToFollow = villagersTag.getCompound(key).getUuid("entityToFollow");
            villagerState.followingTimer = villagersTag.getCompound(key).getInt("followingTimer");
            VillagersFFHMain.LOGGER.info("Villager following " + String.valueOf(villagerState.entityToFollow));

            UUID uuid = UUID.fromString(key);
            serverState.villagers.put(uuid, villagerState);
        });

        return serverState;
    }
}
