package com.natamus.nohostilesaroundcampfire.events;

import com.natamus.collective.data.BlockEntityData;
import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.EntityFunctions;
import com.natamus.nohostilesaroundcampfire.config.ConfigHandler;
import com.natamus.nohostilesaroundcampfire.util.Reference;
import com.natamus.nohostilesaroundcampfire.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

public class CampfireEvent {
	private static final HashMap<Level, List<BlockPos>> checkCampfireBurn = new HashMap<Level, List<BlockPos>>();

	public static void onCampfireAdded(Level level, BlockEntity blockEntity, BlockEntityType<?> blockEntityType) {
		if (level.isClientSide) {
			return;
		}

		if (!blockEntityType.equals(BlockEntityType.CAMPFIRE)) {
			return;
		}

		BlockPos campfirePos = blockEntity.getBlockPos();
		BlockState campfireState = blockEntity.getBlockState();
		if (CompareBlockFunctions.blockIsInRegistryHolder(campfireState.getBlock(), BlockTags.CAMPFIRES)) {
			boolean isLit = true;
			if (ConfigHandler.campfireMustBeLit) {
				isLit = campfireState.getValue(CampfireBlock.LIT);
			}

			if (isLit) {
				int r = (int)(ConfigHandler.preventHostilesRadius * ConfigHandler.burnHostilesRadiusModifier);
				List<Entity> entities = level.getEntities(null, new AABB(campfirePos.getX()-r, campfirePos.getY()-r, campfirePos.getZ()-r, campfirePos.getX()+r, campfirePos.getY()+r, campfirePos.getZ()+r));
				for (Entity entity : entities) {
					if (Util.entityIsHostile(entity)) {
						entity.setRemainingFireTicks(600);
					}
				}
			}
		}
	}

	public static boolean onEntityCheckSpawn(Mob mob, ServerLevel level, BlockPos spawnerPos, MobSpawnType spawnReason) {
		if (mob == null) {
			return true;
		}

		if (mob.getTags().contains(Reference.MOD_ID + ".checked" )) {
			return true;
		}
		mob.addTag(Reference.MOD_ID + ".checked");

		if (!ConfigHandler.preventMobSpawnerSpawns) {
			if (EntityFunctions.isEntityFromSpawner(mob)) {
				return true;
			}
		}

		if (!Util.entityIsHostile(mob)) {
			return true;
		}

		if (!BlockEntityData.cachedBlockEntities.get(BlockEntityType.CAMPFIRE).containsKey(level)) {
			return true;
		}

		BlockPos entityPos = mob.blockPosition();
		Vec3i entityVec3i = new Vec3i(entityPos.getX(), entityPos.getY(), entityPos.getZ());

		boolean foundCampfire = false;
		for (BlockEntity campfireBlockEntity : BlockEntityData.cachedBlockEntities.get(BlockEntityType.CAMPFIRE).get(level)) {
			if (!campfireBlockEntity.getBlockPos().closerThan(entityVec3i, ConfigHandler.preventHostilesRadius)) {
				continue;
			}

			BlockState campfireState = campfireBlockEntity.getBlockState();

			Block block = campfireState.getBlock();
			if (!(block instanceof CampfireBlock)) {
				continue;
			}

			if (!ConfigHandler.enableEffectForNormalCampfires) {
				if (block.equals(Blocks.CAMPFIRE)) {
					continue;
				}
			}
			if (!ConfigHandler.enableEffectForSoulCampfires) {
				if (block.equals(Blocks.SOUL_CAMPFIRE)) {
					continue;
				}
			}

			if (ConfigHandler.campfireMustBeLit) {
				Boolean isLit = campfireState.getValue(CampfireBlock.LIT);
				if (!isLit) {
					continue;
				}
			}
			if (ConfigHandler.campfireMustBeSignalling) {
				Boolean issignalling = campfireState.getValue(CampfireBlock.SIGNAL_FIRE);
				if (!issignalling) {
					continue;
				}
			}

			foundCampfire = true;
			break;
		}

		if (!foundCampfire) {
			return true;
		}

		List<Entity> passengers = mob.getPassengers();
		if (passengers.size() > 0) {
			for (Entity passenger : passengers) {
				passenger.remove(RemovalReason.DISCARDED);
			}
		}

		return false;
	}

	public static void onCampfireRemoved(Level level, BlockEntity blockEntity, BlockEntityType<?> blockEntityType) {
		if (level.isClientSide) {
			return;
		}

		if (!blockEntityType.equals(BlockEntityType.CAMPFIRE)) {
			return;
		}

		if (!ConfigHandler.burnHostilesAroundWhenPlaced) {
			return;
		}

		Block block = blockEntity.getBlockState().getBlock();
		if (!(CompareBlockFunctions.blockIsInRegistryHolder(block, BlockTags.CAMPFIRES))) {
			return;
		}

		if (!ConfigHandler.enableEffectForNormalCampfires) {
			if (block.equals(Blocks.CAMPFIRE)) {
				return;
			}
		}
		if (!ConfigHandler.enableEffectForSoulCampfires) {
			if (block.equals(Blocks.SOUL_CAMPFIRE)) {
				return;
			}
		}

		BlockPos campfirePos = blockEntity.getBlockPos();

		int r = (int)(ConfigHandler.preventHostilesRadius * ConfigHandler.burnHostilesRadiusModifier);

		for (Entity entity : level.getEntities(null, new AABB(campfirePos.getX()-r, campfirePos.getY()-r, campfirePos.getZ()-r, campfirePos.getX()+r, campfirePos.getY()+r, campfirePos.getZ()+r))) {
			if (Util.entityIsHostile(entity)) {
				if (entity.isOnFire()) {
					entity.clearFire();
					entity.setRemainingFireTicks(40);
				}
			}
		}	
	}
}