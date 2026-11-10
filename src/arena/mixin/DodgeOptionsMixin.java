package arena.mixin;

import arena.DodgeControls;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class DodgeOptionsMixin {
    @Shadow @Final @Mutable public KeyMapping[] keyMappings;
    @Inject(method="load",at=@At("HEAD"))
    private void arena$registerDodge(CallbackInfo ci) { keyMappings=DodgeControls.register(keyMappings); }
}
