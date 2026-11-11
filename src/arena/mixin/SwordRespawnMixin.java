package arena.mixin;

import arena.ArenaLoadout;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class SwordRespawnMixin {
    @Inject(method="respawn",at=@At("RETURN"))
    private void arena$respawnSword(ServerPlayer previous,boolean alive,Entity.RemovalReason reason,CallbackInfoReturnable<ServerPlayer> cir) {
        if(!alive) ArenaLoadout.afterDeathRespawn(cir.getReturnValue());
    }
}
