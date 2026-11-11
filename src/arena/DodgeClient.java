package arena;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class DodgeClient {
    private static final double[] SPEED={.72,.66,.58,.48,.38,.26};
    public static final int COOLDOWN=14;
    private static LocalPlayer owner;
    private static int lastForward=1,lastRight=0,step=-1,cooldown;
    private static Vec3 direction=Vec3.ZERO,lastPosition=Vec3.ZERO;

    public static void tick() {
        var mc=Minecraft.getInstance();
        DodgeControls.initialize(mc);
        if(owner!=mc.player) {
            owner=mc.player; step=-1; cooldown=0; lastForward=1; lastRight=0;
        }
        if(cooldown>0) cooldown--;
        if(mc.player==null || mc.getSingleplayerServer()==null || mc.gui.screen()!=null || !mc.player.isAlive()) cancel(true);
    }
    public static boolean key(int action,KeyEvent event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.getSingleplayerServer()==null || mc.gui.screen()!=null) return false;
        if(action==InputConstants.PRESS) {
            if(mc.options.keyUp.matches(event)) { lastForward=1; lastRight=0; }
            if(mc.options.keyDown.matches(event)) { lastForward=-1; lastRight=0; }
            if(mc.options.keyLeft.matches(event)) { lastForward=0; lastRight=-1; }
            if(mc.options.keyRight.matches(event)) { lastForward=0; lastRight=1; }
        }
        if(DodgeControls.dodge==null || !DodgeControls.dodge.matches(event)) return false;
        if(action==InputConstants.PRESS) {
            int forward=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
            int right=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0);
            if(forward==0 && right==0) { forward=lastForward; right=lastRight; }
            start(mc.player,forward,right);
        }
        return true;
    }
    public static Vec3 direction(float yaw,int forward,int right) {
        double angle=Math.toRadians(yaw);
        return new Vec3(-Math.sin(angle)*forward-Math.cos(angle)*right,0,
            Math.cos(angle)*forward-Math.sin(angle)*right).normalize();
    }
    public static boolean start(LocalPlayer player,int forward,int right) {
        if(player!=owner || cooldown>0 || active(player) || !eligible(player) || !player.onGround()) return false;
        direction=direction(player.getYRot(),forward,right);
        if(direction.lengthSqr()<.5) return false;
        lastPosition=player.position(); step=0; cooldown=COOLDOWN;
        player.setSprinting(false);
        return true;
    }
    private static boolean eligible(LocalPlayer player) {
        return player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.isSleeping()
            && !player.isFallFlying() && !player.getAbilities().flying && !player.isInWater()
            && !player.isInLava() && !player.onClimbable() && !player.isBlocking() && !crouching(player);
    }
    private static boolean crouching(LocalPlayer player) {
        return Minecraft.getInstance().options.keyShift.isDown() || player.isShiftKeyDown()
            || player.isCrouching() || player.getPose()==net.minecraft.world.entity.Pose.CROUCHING;
    }
    public static boolean active(LocalPlayer player) { return player==owner && step>=0 && step<SPEED.length; }
    public static void beforeTravel(LocalPlayer player) {
        if(!active(player)) return;
        var mc=Minecraft.getInstance();
        if(!eligible(player) || mc.gui.screen()!=null || player.position().distanceToSqr(lastPosition)>16) { cancel(true); return; }
        player.setSprinting(false);
        Vec3 velocity=player.getDeltaMovement();
        double speed=SPEED[step];
        player.setDeltaMovement(direction.x*speed,velocity.y,direction.z*speed);
    }
    public static Vec3 travelInput(LocalPlayer player,Vec3 input) { return active(player)?Vec3.ZERO:input; }
    public static void afterTravel(LocalPlayer player) {
        if(!active(player)) return;
        lastPosition=player.position();
        if(player.horizontalCollision || ++step>=SPEED.length) cancel(true);
    }
    private static void cancel(boolean stopMomentum) {
        if(step>=0 && owner!=null && stopMomentum) {
            var velocity=owner.getDeltaMovement();
            owner.setDeltaMovement(0,velocity.y,0);
        }
        step=-1;
    }
}
