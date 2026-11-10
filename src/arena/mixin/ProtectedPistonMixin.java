package arena.mixin;

import arena.ArenaService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public class ProtectedPistonMixin {
    @Inject(method="moveBlocks",at=@At("HEAD"),cancellable=true)
    private void arena$noPistons(Level level, BlockPos pos, Direction direction, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if(ArenaService.protectedBlock(level,pos) || ArenaService.protectedBlock(level,pos.relative(direction)) || ArenaService.protectedBlock(level,pos.relative(direction,2))) {
            cir.setReturnValue(false); return;
        }
        var resolver=new PistonStructureResolver(level,pos,direction,extending);
        if(!resolver.resolve()) return;
        for(BlockPos moved:resolver.getToPush()) {
            if(ArenaService.protectedBlock(level,moved) || ArenaService.protectedBlock(level,moved.relative(resolver.getPushDirection()))) {
                cir.setReturnValue(false); return;
            }
        }
        for(BlockPos broken:resolver.getToDestroy()) if(ArenaService.protectedBlock(level,broken)) { cir.setReturnValue(false); return; }
    }
}
