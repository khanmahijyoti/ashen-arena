package arena.mixin;

import arena.client.OgreMusic;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class OgreMusicVolumeMixin {
    @Inject(method="calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F",at=@At("RETURN"),cancellable=true)
    private void arena$battleMusicGain(SoundInstance sound,CallbackInfoReturnable<Float> ci) {
        if(OgreMusic.isBattleTrack(sound)) ci.setReturnValue(ci.getReturnValue()*15.848932f);
    }
}
