package com.natamus.nohostilesaroundcampfire;

import com.natamus.collective.data.BlockEntityData;
import com.natamus.collective.globalcallbacks.CachedBlockEntityCallback;
import com.natamus.nohostilesaroundcampfire.config.ConfigHandler;
import com.natamus.nohostilesaroundcampfire.events.CampfireEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModCommon {

	public static void init() {
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		BlockEntityData.addBlockEntityToCache(BlockEntityType.CAMPFIRE);

		CachedBlockEntityCallback.BLOCK_ENTITY_ADDED.register((level, blockEntity, blockEntityType) -> {
			CampfireEvent.onCampfireAdded(level, blockEntity, blockEntityType);
		});

		CachedBlockEntityCallback.BLOCK_ENTITY_REMOVED.register((level, blockEntity, blockEntityType) -> {
			CampfireEvent.onCampfireRemoved(level, blockEntity, blockEntityType);
		});
	}
}