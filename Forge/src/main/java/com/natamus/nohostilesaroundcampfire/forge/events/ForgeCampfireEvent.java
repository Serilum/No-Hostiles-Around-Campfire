package com.natamus.nohostilesaroundcampfire.forge.events;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.nohostilesaroundcampfire.events.CampfireEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeCampfireEvent {
	@SubscribeEvent
	public void onEntityCheckSpawn(MobSpawnEvent.FinalizeSpawn e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		Mob mob = e.getEntity();
		if (!CampfireEvent.onEntityCheckSpawn(mob, (ServerLevel)level, null, e.getSpawnType())) {
			e.setCanceled(true);

			if (!mob.isAddedToWorld()) {
				e.setSpawnCancelled(true);
			}
			else {
				mob.remove(Entity.RemovalReason.DISCARDED);
			}
		}
	}
}