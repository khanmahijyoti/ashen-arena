package arena.mixin;

import arena.ArenaClient;
import arena.DodgeClient;
import arena.client.OgreAnimations;
import arena.client.OgreMusic;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void arena$tick(CallbackInfo ci) { DodgeClient.tick(); ArenaClient.tick(); OgreAnimations.tick(); OgreMusic.tick(); }
}
