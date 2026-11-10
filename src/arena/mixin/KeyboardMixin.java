package arena.mixin;

import arena.ArenaClient;
import arena.DodgeClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void arena$key(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (DodgeClient.key(action,event) || ArenaClient.key(action, event.key())) ci.cancel();
    }
}
