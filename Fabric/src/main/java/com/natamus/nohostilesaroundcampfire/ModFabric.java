package com.natamus.nohostilesaroundcampfire;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.fabric.callbacks.CollectiveSpawnEvents;
import com.natamus.nohostilesaroundcampfire.events.CampfireEvent;
import com.natamus.nohostilesaroundcampfire.util.Reference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ModFabric implements ModInitializer {
	
	@Override
	public void onInitialize() {
		setGlobalConstants();
		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadEvents() {
		ServerTickEvents.START_WORLD_TICK.register((ServerLevel world) -> {
			CampfireEvent.onWorldTick(world);
		});

		CollectiveSpawnEvents.MOB_CHECK_SPAWN.register((Mob entity, ServerLevel world, BlockPos spawnerPos, MobSpawnType spawnReason) -> {
			return CampfireEvent.onEntityCheckSpawn(entity, world, spawnerPos, spawnReason);
		});

		CollectiveBlockEvents.BLOCK_RIGHT_CLICK.register((world, player, hand, pos, hitVec) -> {
			return CampfireEvent.onRightClickCampfireBlock(world, player, hand, pos, hitVec);
		});

		CollectiveBlockEvents.BLOCK_PLACE.register((Level world, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) -> {
			CampfireEvent.onCampfirePlace(world, blockPos, blockState, livingEntity, itemStack);
			return true;
		});

		CollectiveBlockEvents.BLOCK_DESTROY.register((Level world, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, ItemStack itemStack) -> {
			CampfireEvent.onCampfireBreak(world, player, blockPos, blockState, blockEntity, itemStack);
			return true;
		});
	}

	private static void setGlobalConstants() {

	}
}
