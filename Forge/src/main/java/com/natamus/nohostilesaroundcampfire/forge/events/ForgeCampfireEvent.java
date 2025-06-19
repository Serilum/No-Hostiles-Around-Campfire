package com.natamus.nohostilesaroundcampfire.forge.events;

import com.natamus.collective.functions.WorldFunctions;
import com.natamus.nohostilesaroundcampfire.events.CampfireEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class ForgeCampfireEvent {
	public static void registerEventsInBus() {
		// BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeCampfireEvent.class);

		MobSpawnEvent.FinalizeSpawn.BUS.addListener(ForgeCampfireEvent::onEntityCheckSpawn);
	}

	@SubscribeEvent
	public static boolean onEntityCheckSpawn(MobSpawnEvent.FinalizeSpawn e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return false;
		}

		Mob mob = e.getEntity();
		if (!CampfireEvent.onEntityCheckSpawn(mob, (ServerLevel)level, null, e.getSpawnReason())) {
			if (!mob.isAddedToWorld()) {
				e.setSpawnCancelled(true);
			}
			else {
				mob.remove(Entity.RemovalReason.DISCARDED);
			}
			return true;
		}
		return false;
	}
}