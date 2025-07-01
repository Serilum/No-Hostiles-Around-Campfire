package com.natamus.nohostilesaroundcampfire.util;

import com.natamus.collective.functions.EntityFunctions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
	private static final List<String> hostileSpecialEntities = new ArrayList<String>(Arrays.asList("FoxHound"));
	private static final List<String> hostileSpecialResourceLocations = new ArrayList<String>(Arrays.asList("lycanitesmobs"));
	
	public static boolean entityIsHostile(Entity entity) {
		if (entity.getType().getCategory().equals(MobCategory.MONSTER)) {
			return true;
		}
		
		String entitystring = EntityFunctions.getEntityString(entity);
		if (hostileSpecialEntities.contains(entitystring)) {
			return true;
		}
		
		ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		if (rl != null) {
			return hostileSpecialResourceLocations.contains(rl.toString().split(":")[0]);
		}
		
		return false;
	}
}
