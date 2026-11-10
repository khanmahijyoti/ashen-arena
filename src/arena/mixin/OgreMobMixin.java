package arena.mixin;

import arena.OgreEncounter;
import arena.OgreMobAccess;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class OgreMobMixin implements OgreMobAccess {
    @Shadow @Final protected GoalSelector goalSelector;
    @Shadow @Final protected GoalSelector targetSelector;
    @Override public void arena$clearGoals() { goalSelector.removeAllGoals(g->true); targetSelector.removeAllGoals(g->true); }
    @Inject(method="checkDespawn",at=@At("HEAD"),cancellable=true)
    private void arena$persistentBoss(CallbackInfo ci) {
        if(((Mob)(Object)this).entityTags().contains(OgreEncounter.TAG)) ci.cancel();
    }
}
