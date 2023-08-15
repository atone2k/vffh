package atone.villagersffh;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class VillagersFFHMain implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("villagersffh");
	public static final String MOD_ID = "villagersffh";

	public static final Identifier VILLAGER_PARTICLES_ID = new Identifier(MOD_ID, "villager_particle");

	public static MemoryModuleType<PlayerEntity> PLAYER_FOLLOW_TARGET;
	public static final Activity FOLLOW;


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity.getType() == EntityType.VILLAGER) {
				ServerState serverState = ServerState.getServerState(world.getServer());
				VillagerState villagerState = ServerState.getVillagerState((VillagerEntity) entity);
			}
		});

		LOGGER.info("Hello Fabric world!");
	}

	static {
		PLAYER_FOLLOW_TARGET = Registry.register(Registries.MEMORY_MODULE_TYPE, new Identifier(MOD_ID, "player_follow_target"), new MemoryModuleType<>(Optional.empty()));
		FOLLOW = Registry.register(Registries.ACTIVITY, new Identifier(MOD_ID, "follow"), new Activity("follow"));
	}
}