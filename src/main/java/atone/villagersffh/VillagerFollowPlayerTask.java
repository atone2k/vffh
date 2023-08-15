package atone.villagersffh;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import java.util.Optional;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class VillagerFollowPlayerTask {
    //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
    public VillagerFollowPlayerTask() {
    }

    public static Task<VillagerEntity> create() {
        return TaskTriggerer.task((context) -> {
            return context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (walkTarget) -> {
                return ((world, entity, time) -> {
                    VillagerState villagerState = ServerState.getVillagerState((VillagerEntity) entity);
                    if (villagerState.followingTimer <= 0) {
                        return false;
                    }

                    PlayerEntity player = (PlayerEntity) world.getPlayerByUuid(villagerState.entityToFollow);

                    if (player == null) {
                        return false;
                    }

                    walkTarget.remember(new WalkTarget(player.getPos(), 0.5f, 3));
                    return true;
                });
            });
        });
    }
}
