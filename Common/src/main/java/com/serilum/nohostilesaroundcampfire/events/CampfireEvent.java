package com.serilum.nohostilesaroundcampfire.events;

import com.natamus.collective.functions.CampfireFunctions;
import com.serilum.nohostilesaroundcampfire.config.ConfigHandler;
import com.serilum.nohostilesaroundcampfire.util.Reference;
import com.serilum.nohostilesaroundcampfire.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

public class CampfireEvent {
	private static final int burnDurationInTicks = 600;
	private static final String campfireBurnedTag = Reference.MOD_ID + ".campfireburned";
	private static final WeakHashMap<Level, HashMap<BlockPos, Long>> burnStartTimes = new WeakHashMap<>();

	public static void onCampfirePlaced(Level level, BlockPos campfirePos) {
		if (level.isClientSide()) {
			return;
		}

		if (!ConfigHandler.burnHostilesAroundWhenPlaced) {
			return;
		}

		BlockState campfireState = level.getBlockState(campfirePos);
		if (!CampfireFunctions.isCampfire(campfireState)) {
			return;
		}

		if (!Util.isActiveCampfire(level, campfirePos, campfireState)) {
			return;
		}

		boolean burnedHostiles = false;
		for (Mob mob : level.getEntitiesOfClass(Mob.class, getBurnArea(campfirePos))) {
			if (!Util.entityIsHostile(mob)) {
				continue;
			}

			if (ConfigHandler.preventBurningNamedHostiles) {
				if (mob.hasCustomName()) {
					continue;
				}
			}

			mob.setRemainingFireTicks(burnDurationInTicks);
			mob.addTag(campfireBurnedTag);
			burnedHostiles = true;
		}

		if (burnedHostiles) {
			rememberBurnStart(level, campfirePos);
		}
	}

	public static void onCubeMobRemoved(Mob cubeMob) {
		Level level = cubeMob.level();
		if (level.isClientSide()) {
			return;
		}

		if (!cubeMob.isDeadOrDying()) {
			return;
		}

		if (!cubeMob.entityTags().contains(campfireBurnedTag)) {
			return;
		}

		int remainingFireTicks = cubeMob.getRemainingFireTicks();
		if (remainingFireTicks <= 0) {
			return;
		}

		for (Mob splitMob : level.getEntitiesOfClass(Mob.class, cubeMob.getBoundingBox().inflate(1.0))) {
			if (splitMob.equals(cubeMob) || splitMob.tickCount > 0) {
				continue;
			}

			if (!splitMob.getType().equals(cubeMob.getType())) {
				continue;
			}

			splitMob.setRemainingFireTicks(remainingFireTicks);
			splitMob.addTag(campfireBurnedTag);
		}
	}

	public static boolean onEntityCheckSpawn(Mob mob, ServerLevel level, BlockPos spawnerPos, EntitySpawnReason spawnReason) {
		if (mob == null) {
			return true;
		}

		if (!Util.isPreventableSpawnReason(spawnReason)) {
			return true;
		}

		if (!Util.entityIsHostile(mob)) {
			return true;
		}

		if (Util.isRaidMob(mob)) {
			return true;
		}

		if (!ConfigHandler.preventMobSpawnerSpawns) {
			if (Util.isSpawnerSpawn(mob, spawnReason)) {
				return true;
			}
		}

		if (!Util.isNearActiveCampfire(level, mob.blockPosition())) {
			return true;
		}

		List<Entity> passengers = mob.getPassengers();
		if (!passengers.isEmpty()) {
			for (Entity passenger : passengers) {
				passenger.remove(RemovalReason.DISCARDED);
			}
		}

		return false;
	}

	public static void onCampfireRemoved(Level level, BlockEntity blockEntity, BlockEntityType<?> blockEntityType) {
		if (level.isClientSide()) {
			return;
		}

		if (!CampfireFunctions.isCampfire(blockEntity.getBlockState())) {
			return;
		}

		HashMap<BlockPos, Long> levelBurnStartTimes = burnStartTimes.get(level);
		if (levelBurnStartTimes == null) {
			return;
		}

		BlockPos campfirePos = blockEntity.getBlockPos();
		Long burnStartTime = levelBurnStartTimes.remove(campfirePos);
		if (burnStartTime == null) {
			return;
		}

		if (level.getGameTime() - burnStartTime > burnDurationInTicks) {
			return;
		}

		for (Mob mob : level.getEntitiesOfClass(Mob.class, getBurnArea(campfirePos))) {
			if (Util.entityIsHostile(mob)) {
				if (mob.isOnFire()) {
					mob.clearFire();
					mob.setRemainingFireTicks(40);
				}
			}
		}
	}

	private static AABB getBurnArea(BlockPos campfirePos) {
		int r = (int)(ConfigHandler.preventHostilesRadius * ConfigHandler.burnHostilesRadiusModifier);
		return new AABB(campfirePos.getX()-r, campfirePos.getY()-r, campfirePos.getZ()-r, campfirePos.getX()+r, campfirePos.getY()+r, campfirePos.getZ()+r);
	}

	private static void rememberBurnStart(Level level, BlockPos campfirePos) {
		HashMap<BlockPos, Long> levelBurnStartTimes = burnStartTimes.get(level);
		if (levelBurnStartTimes == null) {
			levelBurnStartTimes = new HashMap<>();
			burnStartTimes.put(level, levelBurnStartTimes);
		}

		long gameTime = level.getGameTime();
		levelBurnStartTimes.values().removeIf(burnStartTime -> gameTime - burnStartTime > burnDurationInTicks);
		levelBurnStartTimes.put(campfirePos.immutable(), gameTime);
	}
}
