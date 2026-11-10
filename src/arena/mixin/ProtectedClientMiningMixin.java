package arena.mixin;

import arena.ArenaService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ProtectedClientMiningMixin {
    private static boolean arena$protected(BlockPos pos) {
        var mc=Minecraft.getInstance(); var server=mc.getSingleplayerServer();
        var view=server==null?null:ArenaService.view(server);
        return view!=null && mc.level!=null && mc.level.dimension().equals(Level.OVERWORLD) && view.protects(pos);
    }
    @Inject(method={"startDestroyBlock","continueDestroyBlock"},at=@At("HEAD"),cancellable=true)
    private void arena$noCracks(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(arena$protected(pos)) cir.setReturnValue(false);
    }
    @Inject(method="destroyBlock",at=@At("HEAD"),cancellable=true)
    private void arena$noGhostBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(arena$protected(pos)) cir.setReturnValue(false);
    }
}
