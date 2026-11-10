package arena.mixin;

import arena.client.OgrePose;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.*;

@Mixin(ZombieRenderState.class)
public class OgreRenderStateMixin implements OgrePose {
    @Unique private boolean arena$ogre;
    @Unique private int arena$action;
    @Unique private float arena$age;
    @Override public void arena$ogrePose(boolean ogre,int action,float age) { arena$ogre=ogre; arena$action=action; arena$age=age; }
    @Override public boolean arena$isOgre() { return arena$ogre; }
    @Override public int arena$action() { return arena$action; }
    @Override public float arena$age() { return arena$age; }
}
