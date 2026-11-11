package arena.mixin;

import arena.DodgeClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class DodgeTravelMixin {
    @Inject(method="travel",at=@At("HEAD"))
    private void arena$quickstepVelocity(Vec3 input,CallbackInfo ci) {
        if((Object)this instanceof LocalPlayer player) DodgeClient.beforeTravel(player);
    }
    @ModifyVariable(method="travel",at=@At("HEAD"),argsOnly=true)
    private Vec3 arena$lockDirection(Vec3 input) {
        return (Object)this instanceof LocalPlayer player?DodgeClient.travelInput(player,input):input;
    }
    @Inject(method="travel",at=@At("RETURN"))
    private void arena$finishStep(Vec3 input,CallbackInfo ci) {
        if((Object)this instanceof LocalPlayer player) DodgeClient.afterTravel(player);
    }
}
