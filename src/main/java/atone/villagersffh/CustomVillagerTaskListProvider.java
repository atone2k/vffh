package atone.villagersffh;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;

public class CustomVillagerTaskListProvider {
    public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> createFollowTasks() {
        return ImmutableList.of(
                Pair.of(0, VillagerFollowPlayerTask.create()),
                Pair.of(0, LookAtMobTask.create(EntityType.PLAYER, 18.0f)),
                Pair.of(1, new LookAroundTask(45, 90))
        );
    }
}
