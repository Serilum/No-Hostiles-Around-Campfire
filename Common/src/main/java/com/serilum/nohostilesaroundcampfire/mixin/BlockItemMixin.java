package com.serilum.nohostilesaroundcampfire.mixin;

import com.serilum.nohostilesaroundcampfire.events.CampfireEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockItem.class, priority = 1001)
public class BlockItemMixin {
	@Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "RETURN"))
	private void place(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
		if (!cir.getReturnValue().consumesAction()) {
			return;
		}

		CampfireEvent.onCampfirePlaced(placeContext.getLevel(), placeContext.getClickedPos());
	}
}
