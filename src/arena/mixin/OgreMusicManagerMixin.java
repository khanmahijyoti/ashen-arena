package arena.mixin;

import arena.client.OgreMusic;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class OgreMusicManagerMixin {
    @Shadow private float currentGain;
    private boolean arena$overrodeGain;
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void arena$preventMusicOverlap(CallbackInfo ci) {
        if(OgreMusic.suppressAmbient()) {
            Minecraft.getInstance().getSoundManager().updateCategoryVolume(SoundSource.MUSIC,1);
            arena$overrodeGain=true;
            ci.cancel();
        } else if(arena$overrodeGain) {
            Minecraft.getInstance().getSoundManager().updateCategoryVolume(SoundSource.MUSIC,currentGain);
            arena$overrodeGain=false;
        }
    }
}
