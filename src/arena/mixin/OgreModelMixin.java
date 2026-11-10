package arena.mixin;

import arena.OgreEncounter;
import arena.client.OgrePose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class OgreModelMixin {
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("TAIL"))
    private void arena$attackPose(HumanoidRenderState state,CallbackInfo ci) {
        if(!(state instanceof OgrePose pose) || !pose.arena$isOgre()) return;
        body.xScale=1.3f; head.xScale=1.15f;
        rightArm.xScale=1.25f; leftArm.xScale=1.25f; rightLeg.xScale=1.15f; leftLeg.xScale=1.15f;
        int action=pose.arena$action(); float age=pose.arena$age();
        if(action==OgreEncounter.GRAB || action==OgreEncounter.HOLD) {
            float reach=action==OgreEncounter.HOLD?1:Math.min(1,age/24);
            rightArm.xRot=leftArm.xRot=-1.1f-.5f*reach;
            rightArm.yRot=-.4f; leftArm.yRot=.4f; body.xRot=.15f;
        } else if(action==OgreEncounter.KICK || (action==OgreEncounter.STRIKE && age<10)) {
            float raised=action==OgreEncounter.STRIKE?1:Math.min(1,age/16);
            rightLeg.xRot=-1.65f*raised; rightLeg.y=12-2*raised;
            leftLeg.xRot=.15f; body.xRot=.1f;
            rightArm.xRot=leftArm.xRot=-.4f; rightArm.zRot=.4f; leftArm.zRot=-.4f;
        }
    }
}
