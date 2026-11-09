package xyz.kajitsy.thaumaturgetinker.mixin;
/** Run only after the native break event and adventure/operator restrictions accepted the center. */
@org.spongepowered.asm.mixin.Mixin(net.minecraft.server.level.ServerPlayerGameMode.class)
abstract class AdvancedMiningHookMixin {
 @org.spongepowered.asm.mixin.Shadow protected net.minecraft.server.level.ServerPlayer player;
 @org.spongepowered.asm.mixin.injection.Inject(method="destroyBlock",at=@org.spongepowered.asm.mixin.injection.At(value="INVOKE",target="Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"),cancellable=true)
 private void mine(net.minecraft.core.BlockPos pos,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci){if(!xyz.kajitsy.thaumaturgetinker.item.AdvancedToolMining.breaking(player)&&player.getMainHandItem().getItem() instanceof xyz.kajitsy.thaumaturgetinker.item.AdvancedTool tool){var before=player.level().getBlockState(pos);tool.startMining(player,pos);if(player.level().getBlockState(pos)!=before)ci.setReturnValue(true);}}
}
