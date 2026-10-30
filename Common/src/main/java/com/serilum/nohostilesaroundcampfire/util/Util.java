package com.serilum.nohostilesaroundcampfire.util;

import com.natamus.collective.data.BlockEntityData;
import com.natamus.collective.functions.CampfireFunctions;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.EntityFunctions;
import com.serilum.nohostilesaroundcampfire.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Util {
	private static final List<String> hostileSpecialEntities = new ArrayList<String>(Arrays.asList("FoxHound"));
	private static final List<String> hostileSpecialResourceLocations = new ArrayList<String>(Arrays.asList("lycanitesmobs"));
	private static final List<MobSpawnType> preventableSpawnReasons = new ArrayList<>(Arrays.asList(MobSpawnType.NATURAL, MobSpawnType.CHUNK_GENERATION, MobSpawnType.SPAWNER, MobSpawnType.TRIAL_SPAWNER, MobSpawnType.PATROL, MobSpawnType.EVENT, MobSpawnType.REINFORCEMENT, MobSpawnType.STRUCTURE));
	private static final HashMap<String, HashMap<EntityType<?>, Boolean>> entityListResults = new HashMap<>();

	public static boolean entityIsHostile(Entity entity) {
		EntityType<?> entityType = entity.getType();
		if (isInEntityList(ConfigHandler.entitiesToIgnore, entityType)) {
			return false;
		}

		if (!entityType.getCategory().isFriendly()) {
			return true;
		}

		if (isInEntityList(ConfigHandler.entitiesToTreatAsHostile, entityType)) {
			return true;
		}

		String entitystring = EntityFunctions.getEntityString(entity);
		if (hostileSpecialEntities.contains(entitystring)) {
			return true;
		}

		ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
		if (rl != null) {
			return hostileSpecialResourceLocations.contains(rl.toString().split(":")[0]);
		}

		return false;
	}

	public static boolean isInEntityList(String entityList, EntityType<?> entityType) {
		if (entityList.isBlank()) {
			return false;
		}

		HashMap<EntityType<?>, Boolean> results = entityListResults.get(entityList);
		if (results == null) {
			results = new HashMap<>();
			entityListResults.put(entityList, results);
		}

		Boolean isInList = results.get(entityType);
		if (isInList == null) {
			isInList = matchesEntityList(entityList, entityType);
			results.put(entityType, isInList);
		}
		return isInList;
	}

	private static boolean matchesEntityList(String entityList, EntityType<?> entityType) {
		for (String rawEntry : entityList.split(",")) {
			String entry = rawEntry.strip();
			if (!entry.startsWith("#")) {
				continue;
			}

			ResourceLocation tagLocation = ResourceLocation.tryParse(entry.substring(1));
			if (tagLocation == null) {
				continue;
			}

			if (entityType.is(TagKey.create(Registries.ENTITY_TYPE, tagLocation))) {
				return true;
			}
		}

		return ConfigFunctions.isInIdList(entityList, EntityType.getKey(entityType).toString());
	}

	public static boolean isPreventableSpawnReason(MobSpawnType spawnReason) {
		return preventableSpawnReasons.contains(spawnReason);
	}

	public static boolean isSpawnerSpawn(Mob mob, MobSpawnType spawnReason) {
		if (MobSpawnType.isSpawner(spawnReason)) {
			return true;
		}
		return EntityFunctions.isEntityFromSpawner(mob);
	}

	public static boolean isRaidMob(Mob mob) {
		if (!(mob instanceof Raider raider)) {
			return false;
		}
		return raider.getCurrentRaid() != null;
	}

	public static boolean isActiveCampfire(Level level, BlockPos campfirePos, BlockState campfireState) {
		if (!CampfireFunctions.isCampfire(campfireState)) {
			return false;
		}

		if (CampfireFunctions.isSoulCampfire(campfireState)) {
			if (!ConfigHandler.enableEffectForSoulCampfires) {
				return false;
			}
		}
		else if (!ConfigHandler.enableEffectForNormalCampfires) {
			return false;
		}

		if (ConfigHandler.campfireMustBeLit) {
			if (!CampfireFunctions.isLit(campfireState)) {
				return false;
			}
		}
		if (ConfigHandler.campfireMustBeSignalling) {
			if (!CampfireFunctions.isSignalling(campfireState)) {
				return false;
			}
		}
		if (ConfigHandler.campfireMustHaveSkyAccess) {
			if (!level.canSeeSky(campfirePos)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isNearActiveCampfire(ServerLevel level, BlockPos pos) {
		for (BlockEntity campfireBlockEntity : BlockEntityData.getCachedCampfires(level)) {
			BlockPos campfirePos = campfireBlockEntity.getBlockPos();
			if (!campfirePos.closerThan(pos, ConfigHandler.preventHostilesRadius)) {
				continue;
			}

			if (isActiveCampfire(level, campfirePos, campfireBlockEntity.getBlockState())) {
				return true;
			}
		}
		return false;
	}
}
