package arena.mixin;

import arena.ArenaService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ProtectedMiningMixin {
    @Shadow protected ServerLevel level;
    @Inject(method="destroyBlock",at=@At("HEAD"),cancellable=true)
    private void arena$noMining(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(ArenaService.protectedBlock(level,pos)) cir.setReturnValue(false);
    }
}
