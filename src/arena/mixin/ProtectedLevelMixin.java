package arena.mixin;

import arena.ArenaService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class ProtectedLevelMixin {
    @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",at=@At("HEAD"),cancellable=true)
    private void arena$immutable(BlockPos pos, BlockState state, int flags, int depth, CallbackInfoReturnable<Boolean> cir) {
        if(ArenaService.protectedBlock((Level)(Object)this,pos)) cir.setReturnValue(false);
    }
    @Inject(method="destroyBlock",at=@At("HEAD"),cancellable=true)
    private void arena$noDrops(BlockPos pos, boolean drops, Entity entity, int depth, CallbackInfoReturnable<Boolean> cir) {
        if(ArenaService.protectedBlock((Level)(Object)this,pos)) cir.setReturnValue(false);
    }
}
