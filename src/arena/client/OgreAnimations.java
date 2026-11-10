package arena.client;

import arena.OgreEncounter;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class OgreAnimations {
    public record Action(int code,long started) { }
    private static final Map<Integer,Action> ACTIONS=new HashMap<>();
    private static ClientLevel level;
    public static void tick() {
        var current=Minecraft.getInstance().level;
        if(current!=level) { ACTIONS.clear(); level=current; }
        if(level!=null && level.getGameTime()%20==0) ACTIONS.values().removeIf(a->level.getGameTime()-a.started()>80);
    }
    public static boolean event(Entity entity,byte code) {
        if(!OgreEncounter.isOgre(entity) || code<OgreEncounter.GRAB || code>OgreEncounter.IDLE) return false;
        if(code==OgreEncounter.IDLE) ACTIONS.remove(entity.getId());
        else ACTIONS.put(entity.getId(),new Action(code,entity.level().getGameTime()));
        return true;
    }
    public static Action action(Entity entity) { return ACTIONS.get(entity.getId()); }
}
