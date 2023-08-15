package atone.villagersffh.mixin;

import atone.villagersffh.*;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(VillagerEntity.class)
public abstract class VillagerMixin {
	@Unique
	public final Set<Item> MORE_GATHERABLE_ITEMS = ImmutableSet.of(Items.EMERALD);

	@Shadow
	public abstract VillagerData getVillagerData();

	@Shadow
	public abstract boolean canGather(ItemStack stack);

	@Shadow
	@Final
	private static Logger LOGGER;

	@Unique
	public int timeSinceLastFootstep = 0;

	@Inject(method = "initBrain", at = @At(value = "HEAD"))
	public void inject(Brain<VillagerEntity> brain, CallbackInfo ci) {
		brain.setTaskList(VillagersFFHMain.FOLLOW, CustomVillagerTaskListProvider.createFollowTasks());
	}

	@Unique
	public void endFollow(boolean happy) {
		ServerState.getVillagerState((VillagerEntity) (Object) this).followingTimer = 0;
		((VillagerEntity) (Object) this).getBrain().resetPossibleActivities();
		SoundEvent sound = happy ? SoundEvents.ENTITY_VILLAGER_CELEBRATE : SoundEvents.ENTITY_VILLAGER_NO;
		((VillagerEntity) (Object) this).playSound(sound, 1.0f, 1.0f);
	}

	@Unique
	public void beginFollow(PlayerEntity player) {
		LOGGER.info("MARKED VILLAGER to follow {} !", player.getUuid());
		ServerState.getVillagerState((VillagerEntity) (Object) this).followingTimer = 20 * 180;
		ServerState.getVillagerState((VillagerEntity) (Object) this).entityToFollow = player.getUuid();
		LOGGER.info(String.valueOf(ServerState.getVillagerState((VillagerEntity) (Object) this).entityToFollow));
		ServerState.getServerState(player.getServer()).markDirty();

		VillagerEntity villager = (VillagerEntity) (Object) this;

		Random random = Random.create();

		for (PlayerEntity playerL: villager.getWorld().getPlayers()) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeVector3f(villager.getPos().toVector3f());
			ServerPlayNetworking.send((ServerPlayerEntity) playerL, VillagersFFHMain.VILLAGER_PARTICLES_ID, buf);
		}

		villager.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
		villager.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
	}

	@Inject(method = "interactMob", at = @At(value = "HEAD"), cancellable = true)
	public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		VillagerEntity villager = (VillagerEntity) (Object) this;
		ItemStack itemStack = player.getStackInHand(hand);
		if (!player.getWorld().isClient() && player.isSneaking()) {
			if (itemStack.isOf(Items.EMERALD) && villager.isAlive() && !villager.hasCustomer() && !villager.isSleeping()) {
				if (villager.getVillagerData().getLevel() >= 3 || villager.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
					beginFollow(player);
					player.getInventory().removeStack(player.getInventory().selectedSlot, 1);
					cir.setReturnValue(ActionResult.SUCCESS);
				}
			} else {
				VillagerState villagerState = ServerState.getVillagerState(villager);
				if (player.getWorld().getPlayerByUuid(villagerState.entityToFollow) == player) {
					endFollow(true);
					cir.setReturnValue(ActionResult.SUCCESS);
				}
			}
		}
	}

	@Inject(method = "mobTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
	public void tick(CallbackInfo ci) {

		VillagerState villagerState = ServerState.getVillagerState((VillagerEntity) (Object) this);

		if (villagerState != null && !((VillagerEntity) (Object) this).getWorld().isClient) {
			if (villagerState.followingTimer > 0) {
				villagerState.followingTimer -= 1;
				ServerState.getServerState(((VillagerEntity) (Object) this).getServer()).markDirty();

				VillagerEntity villager = (VillagerEntity) (Object) this;
				villager.getBrain().doExclusively(VillagersFFHMain.FOLLOW);

				if (villagerState.followingTimer == 1) {
					LOGGER.info("TIME'S UP!");
					endFollow(false);
				}

				if (timeSinceLastFootstep > 0) {
					timeSinceLastFootstep -= 1;
				} else {
					timeSinceLastFootstep = 8;
					Random random = Random.create();

					for (PlayerEntity player: villager.getWorld().getPlayers()) {
						PacketByteBuf buf = PacketByteBufs.create();
						buf.writeVector3f(villager.getPos().toVector3f());
						ServerPlayNetworking.send((ServerPlayerEntity) player, VillagersFFHMain.VILLAGER_PARTICLES_ID, buf);
					}
				}
			}
		}
	}



//	@Inject(at = @At("TAIL"), method = "loot")
//	private void loot(ItemEntity item, CallbackInfo ci) {
//
//		if (item.getServer().getOverworld().isClient()) { return; }
//
//		if (item.getOwner() == null) { return; }
//
//		if (this.canGather(item.getStack()) && item.getOwner().getType() == EntityType.PLAYER) {
//			LOGGER.info("MARKED VILLAGER to follow {} !", ((PlayerEntity) item.getOwner()).getUuid());
////			((PlayerEntity)item.getOwner()).sendMessage(Text.literal("YOU have a villager following you!"));
//			PlayerEntity player = (PlayerEntity) item.getOwner();
//			ServerState.getVillagerState((VillagerEntity) (Object) this).followingTimer = 20 * 60 * 4;
//			ServerState.getVillagerState((VillagerEntity) (Object) this).entityToFollow = player.getUuid();
//			LOGGER.info(String.valueOf(ServerState.getVillagerState((VillagerEntity) (Object) this).entityToFollow));
//			ServerState.getServerState(item.getServer()).markDirty();
//
//			VillagerEntity villager = (VillagerEntity) (Object) this;
//
//			Random random = Random.create();
//
//			for (PlayerEntity playerL: villager.getWorld().getPlayers()) {
//				PacketByteBuf buf = PacketByteBufs.create();
//				buf.writeVector3f(villager.getPos().toVector3f());
//				ServerPlayNetworking.send((ServerPlayerEntity) playerL, VillagersFFHMain.VILLAGER_PARTICLES_ID, buf);
//			}
//
//			villager.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
//			villager.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
//		}
//	}

//	@Inject(at = @At("HEAD"), method = "canGather", cancellable = true)
//	private void canGather(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
//		Item item = stack.getItem();
//		if (this.getVillagerData().getLevel() >= 2) {
//			if (item.equals(Items.EMERALD) && stack.getCount() == 1) {
//				cir.setReturnValue(true);
//			}
//		}
//	}
}