package org.mcsebi.whereismyshulker.client.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.mcsebi.whereismyshulker.client.ShulkerBoxTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Unique
    private static final ThreadLocal<String> whereismyshulker$pendingCustomName = new ThreadLocal<>();

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"))
    private void whereismyshulker$captureName(BlockPlaceContext context,
                                              CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        if (!level.isClientSide()) return;

        // We only care about shulker boxes
        BlockItem self = (BlockItem) (Object) this;
        Block block = self.getBlock();
        if (!(block instanceof ShulkerBoxBlock)) return;

        String customName = "";

        // Capture BEFORE decrement happens
        if (context.getItemInHand().getCustomName() != null) {
            customName = context.getItemInHand().getCustomName().getString();
        }
        whereismyshulker$pendingCustomName.set(customName);
    }

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"))
    private void whereismyshulker$onPlace(BlockPlaceContext context,
                                          CallbackInfoReturnable<InteractionResult> cir) {
        try {
            InteractionResult result = cir.getReturnValue();
            if (result == null || !result.consumesAction()) return;

            Level level = context.getLevel();
            if (!level.isClientSide()) return;

            // discard everything besides shulker boxes
            BlockItem self = (BlockItem) (Object) this;
            Block block = self.getBlock();
            if (!(block instanceof ShulkerBoxBlock)) return;

            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ShulkerBoxBlock)) return;

            String customName = whereismyshulker$pendingCustomName.get();

            ShulkerBoxTracker.getInstance()
                    .onShulkerBoxPlaced(pos, state.getBlock(), level, customName);

        } finally {
            // Always clear to avoid leaks / wrong names on later placements
            whereismyshulker$pendingCustomName.remove();
        }
    }


}
