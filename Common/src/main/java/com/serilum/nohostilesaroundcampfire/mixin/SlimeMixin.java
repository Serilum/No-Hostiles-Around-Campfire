package com.serilum.nohostilesaroundcampfire.mixin;

import com.serilum.nohostilesaroundcampfire.events.CampfireEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Slime.class, priority = 1001)
public class SlimeMixin {
	@Inject(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At(value = "TAIL"))
	private void remove(Entity.RemovalReason reason, CallbackInfo ci) {
		CampfireEvent.onCubeMobRemoved((Mob)(Object)this);
	}
}
