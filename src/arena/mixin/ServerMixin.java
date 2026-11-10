package arena.mixin;

import arena.ArenaService;
import arena.ArenaLoadout;
import arena.OgreEncounter;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ServerMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void arena$tick(CallbackInfo ci) {
        ArenaService.tick((MinecraftServer)(Object)this);
        ArenaLoadout.tick((MinecraftServer)(Object)this);
        OgreEncounter.tick((MinecraftServer)(Object)this);
    }
    @Inject(method="stopServer",at=@At("HEAD"))
    private void arena$stopBoss(CallbackInfo ci) { OgreEncounter.clear((MinecraftServer)(Object)this); }
}
