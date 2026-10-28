package com.serilum.nohostilesaroundcampfire.config;

import com.natamus.collective.config.DuskConfig;
import com.serilum.nohostilesaroundcampfire.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry(min = 1, max = 128) public static int preventHostilesRadius = 48;
	@Entry public static boolean burnHostilesAroundWhenPlaced = true;
	@Entry(min = 0, max = 1.0) public static double burnHostilesRadiusModifier = 0.5;
	@Entry public static boolean preventBurningNamedHostiles = true;
	@Entry public static boolean preventMobSpawnerSpawns = false;
	@Entry public static boolean campfireMustBeLit = true;
	@Entry public static boolean campfireMustBeSignalling = false;
	@Entry public static boolean campfireMustHaveSkyAccess = false;
	@Entry public static boolean enableEffectForNormalCampfires = true;
	@Entry public static boolean enableEffectForSoulCampfires = true;
	@Entry(registry = "entity_type", filter = "mobs") public static String entitiesToIgnore = "";
	@Entry(registry = "entity_type", filter = "mobs") public static String entitiesToTreatAsHostile = "";

	public static void initConfig() {
		configMetaData.put("preventHostilesRadius", Arrays.asList(
			"The radius around the campfire in blocks where hostile mob spawns will be blocked."
		));
		configMetaData.put("burnHostilesAroundWhenPlaced", Arrays.asList(
			"If enabled, burns all hostile mobs around the campfire within the radius whenever a player places a campfire."
		));
		configMetaData.put("burnHostilesRadiusModifier", Arrays.asList(
			"By default set to 0.5. This means that if the radius is 16, the campfire burns prior mobs in a radius of 8."
		));
		configMetaData.put("preventBurningNamedHostiles", Arrays.asList(
			"When enabled, hostile mobs with a custom name, like one given with a name tag, are not set on fire when a campfire is placed."
		));
		configMetaData.put("preventMobSpawnerSpawns", Arrays.asList(
			"When enabled, the mob spawners spawns are also disabled when a campfire is within the radius."
		));
		configMetaData.put("campfireMustBeLit", Arrays.asList(
			"When enabled, the campfire only has an effect when the block is lit up."
		));
		configMetaData.put("campfireMustBeSignalling", Arrays.asList(
			"When enabled, the campfire only has an effect when the block is signalling."
		));
		configMetaData.put("campfireMustHaveSkyAccess", Arrays.asList(
			"When enabled, the campfire only has an effect when nothing above it blocks the sky. This keeps campfires from clearing out caves."
		));
		configMetaData.put("enableEffectForNormalCampfires", Arrays.asList(
			"When enabled, the mod will work with normal campfires."
		));
		configMetaData.put("enableEffectForSoulCampfires", Arrays.asList(
			"When enabled, the mod will work with soul campfires."
		));
		configMetaData.put("entitiesToIgnore", Arrays.asList(
			"Mobs that campfires never block or burn, comma separated. For example 'minecraft:pillager'. A '*' works as a wildcard, like 'alexsmobs:*'. Entity tags work too, starting with a '#', like '#minecraft:raiders'."
		));
		configMetaData.put("entitiesToTreatAsHostile", Arrays.asList(
			"Mobs that count as hostile even though their mod doesn't register them as monsters, comma separated. For example 'alexsmobs:crocodile'. A '*' works as a wildcard. Entity tags work too, starting with a '#'."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}