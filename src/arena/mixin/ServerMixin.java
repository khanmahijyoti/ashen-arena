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
    private static boolean arena$bossFailed;
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void arena$tick(CallbackInfo ci) {
        MinecraftServer server=(MinecraftServer)(Object)this;
        ArenaService.tick(server);
        ArenaLoadout.tick(server);
        if(arena$bossFailed) return;
        try { OgreEncounter.tick(server); }
        catch(RuntimeException e) {
            arena$bossFailed=true;
            System.getLogger("AshenArena").log(System.Logger.Level.ERROR,"Ashen Ogre encounter failed; boss disabled until restart",e);
            try { OgreEncounter.clear(server); }
            catch(RuntimeException cleanup) { System.getLogger("AshenArena").log(System.Logger.Level.ERROR,"Could not clean up the Ashen Ogre",cleanup); }
        }
    }
    @Inject(method="stopServer",at=@At("HEAD"))
    private void arena$stopBoss(CallbackInfo ci) { OgreEncounter.clear((MinecraftServer)(Object)this); }
}
