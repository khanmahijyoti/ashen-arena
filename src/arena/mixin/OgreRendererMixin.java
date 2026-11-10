package arena.mixin;

import arena.OgreEncounter;
import arena.client.*;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieRenderer.class)
public class OgreRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/monster/zombie/Zombie;Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;F)V",at=@At("RETURN"))
    private void arena$extractPose(Zombie entity,ZombieRenderState state,float partial,CallbackInfo ci) {
        var action=OgreAnimations.action(entity);
        ((OgrePose)state).arena$ogrePose(OgreEncounter.isOgre(entity),action==null?0:action.code(),action==null?0:entity.level().getGameTime()-action.started()+partial);
    }
}
