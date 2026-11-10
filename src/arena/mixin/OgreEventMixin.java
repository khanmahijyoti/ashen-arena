package arena.mixin;

import arena.client.OgreAnimations;
import arena.client.OgreMusic;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class OgreEventMixin {
    @Inject(method="handleEntityEvent",at=@At("HEAD"),cancellable=true)
    private void arena$ogreAction(byte code,CallbackInfo ci) {
        var entity=(LivingEntity)(Object)this;
        if(entity.level().isClientSide() && (OgreMusic.event(entity,code) || OgreAnimations.event(entity,code))) ci.cancel();
    }
}
