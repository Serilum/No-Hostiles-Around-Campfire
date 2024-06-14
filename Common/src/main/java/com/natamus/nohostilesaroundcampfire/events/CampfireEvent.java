package com.natamus.nohostilesaroundcampfire.events;

import com.natamus.collective.functions.CompareBlockFunctions;
import com.natamus.collective.functions.EntityFunctions;
import com.natamus.collective.functions.FABFunctions;
import com.natamus.collective.functions.HashMapFunctions;
import com.natamus.nohostilesaroundcampfire.config.ConfigHandler;
import com.natamus.nohostilesaroundcampfire.util.Reference;
import com.natamus.nohostilesaroundcampfire.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CampfireEvent {
	private static final HashMap<Level, List<BlockPos>> checkCampfireBurn = new HashMap<Level, List<BlockPos>>();

	public static void onWorldTick(ServerLevel level) {
		if (HashMapFunctions.computeIfAbsent(checkCampfireBurn, level, k -> new ArrayList<BlockPos>()).size() > 0) {
			BlockPos campfirepos = checkCampfireBurn.get(level).getFirst();
			BlockState campfirestate = level.getBlockState(campfirepos);
			if (CompareBlockFunctions.blockIsInRegistryHolder(campfirestate.getBlock(), BlockTags.CAMPFIRES)) {
				boolean islit = true;
				if (ConfigHandler.campfireMustBeLit) {
					islit = campfirestate.getValue(CampfireBlock.LIT);
				}
				
				if (islit) {
					int r = (int)(ConfigHandler.preventHostilesRadius * ConfigHandler.burnHostilesRadiusModifier);
					List<Entity> entities = level.getEntities(null, new AABB(campfirepos.getX()-r, campfirepos.getY()-r, campfirepos.getZ()-r, campfirepos.getX()+r, campfirepos.getY()+r, campfirepos.getZ()+r));
					for (Entity entity : entities) {
						if (Util.entityIsHostile(entity)) {
							entity.setRemainingFireTicks(600);
						}
					}
				}
			}
			
			checkCampfireBurn.get(level).removeFirst();
		}
	}
	
	public static boolean onEntityCheckSpawn(Mob entity, ServerLevel world, BlockPos spawnerPos, MobSpawnType spawnReason) {
		if (entity == null) {
			return true;
		}

		if (entity.getTags().contains(Reference.MOD_ID + ".checked" )) {
			return true;	
		}
		entity.addTag(Reference.MOD_ID + ".checked");
		
		if (!ConfigHandler.preventMobSpawnerSpawns) {
			if (EntityFunctions.isEntityFromSpawner(entity)) {
				return true;
			}
		}
		
		if (!Util.entityIsHostile(entity)) {
			return true;
		}
		
		List<BlockPos> nearbycampfires = FABFunctions.getAllTaggedTileEntityPositionsNearbyEntity(BlockTags.CAMPFIRES, ConfigHandler.preventHostilesRadius, world, entity);
		if (nearbycampfires.size() == 0) {
			return true;
		}
		
		BlockPos campfire = null;
		for (BlockPos nearbycampfire : nearbycampfires) {
			BlockState campfirestate = world.getBlockState(nearbycampfire);
			Block block = campfirestate.getBlock();
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
				Boolean islit = campfirestate.getValue(CampfireBlock.LIT);
				if (!islit) {
					continue;
				}
			}
			if (ConfigHandler.campfireMustBeSignalling) {
				Boolean issignalling = campfirestate.getValue(CampfireBlock.SIGNAL_FIRE);
				if (!issignalling) {
					continue;
				}
			}
			
			campfire = nearbycampfire.immutable();
			break;
		}
		
		if (campfire == null) {
			return true;
		}
		
		List<Entity> passengers = entity.getPassengers();
		if (passengers.size() > 0) {
			for (Entity passenger : passengers) {
				passenger.remove(RemovalReason.DISCARDED);
			}
		}
		
		return false;
	}
	
	public static void onCampfirePlace(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (level.isClientSide) {
			return;
		}
		
		if (!ConfigHandler.burnHostilesAroundWhenPlaced) {
			return;
		}
		
		Block block = blockState.getBlock();
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
		
		HashMapFunctions.computeIfAbsent(checkCampfireBurn, level, k -> new ArrayList<BlockPos>()).add(blockPos.immutable());
	}
	
	public static boolean onRightClickCampfireBlock(Level level, Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
		if (level.isClientSide) {
			return true;
		}

		BlockState state = level.getBlockState(pos);
		Block block = state.getBlock();
		
		if (CompareBlockFunctions.blockIsInRegistryHolder(block, BlockTags.CAMPFIRES)) {
			if (state.getValue(CampfireBlock.LIT)) {
				return true;
			}
			
			if (player.getMainHandItem().getItem() instanceof FlintAndSteelItem || player.getOffhandItem().getItem() instanceof FlintAndSteelItem) {
				HashMapFunctions.computeIfAbsent(checkCampfireBurn, level, k -> new ArrayList<BlockPos>()).add(pos.immutable());
			}
		}
		
		return true;
	}
	
	public static void onCampfireBreak(Level world, Player player, BlockPos ppos, BlockState blockState, BlockEntity blockEntity, ItemStack itemStack) {
		if (world.isClientSide) {
			return;
		}
		
		if (!ConfigHandler.burnHostilesAroundWhenPlaced) {
			return;
		}
		
		Block block = blockState.getBlock();
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

		int r = (int)(ConfigHandler.preventHostilesRadius * ConfigHandler.burnHostilesRadiusModifier);
		List<Entity> entities = world.getEntities(null, new AABB(ppos.getX()-r, ppos.getY()-r, ppos.getZ()-r, ppos.getX()+r, ppos.getY()+r, ppos.getZ()+r));
		for (Entity entity : entities) {
			if (Util.entityIsHostile(entity)) {
				if (entity.isOnFire()) {
					entity.clearFire();
					entity.setRemainingFireTicks(40);
				}
			}
		}	
	}
}