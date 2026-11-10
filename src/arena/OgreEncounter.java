package arena;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** One arena-scoped actor. Native navigation/physics handle movement; this controls attack timings. */
public final class OgreEncounter {
    public static final String NAME="Ashen Ogre",TAG="ashen_arena_ogre";
    public static final float HEALTH=140;
    public static final byte GRAB=120,HOLD=121,KICK=122,STRIKE=123,IDLE=124;
    public static final byte MUSIC_START=125,MUSIC_STOP=126;
    public static final ResourceKey<DamageType> GRAB_DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("ashen_arena","ogre_grab"));
    public enum Phase { INTRO,CHASE,GRAB_WINDUP,GRAB_HOLD,KICK_WINDUP,RECOVER,DEFEATED }
    public record Snapshot(UUID id,Phase phase,int age,int attacks) { }
    private static final Map<MinecraftServer,State> STATES=new WeakHashMap<>();
    private static final class State {
        Husk boss;
        ServerBossEvent bar;
        Phase phase=Phase.INTRO;
        int age,attacks;
        UUID target;
        Vec3 aim=Vec3.ZERO;
        boolean music;
    }
    public static boolean isOgre(Entity e) { return e instanceof Husk && e.getCustomName()!=null && NAME.equals(e.getCustomName().getString()); }
    public static Husk boss(MinecraftServer server) { State s=STATES.get(server); return s==null?null:s.boss; }
    public static Snapshot snapshot(MinecraftServer server) { State s=STATES.get(server); return s==null || s.boss==null?null:new Snapshot(s.boss.getUUID(),s.phase,s.age,s.attacks); }
    private static boolean inside(ServerPlayer p,ArenaService.View v) {
        return p.isAlive() && !p.isSpectator() && p.level().dimension().equals(Level.OVERWORLD)
            && Math.abs(p.getX()-(v.ax()+.5))<Hall.HALF-1 && p.getY()>=v.ay() && p.getY()<v.ay()+Hall.HEIGHT
            && p.getZ()>v.az()+1 && p.getZ()<v.az()+Hall.LENGTH-1;
    }
    public static void tick(MinecraftServer server) {
        if(Boolean.getBoolean("ashen_arena.disableBoss")) return;
        var v=ArenaService.view(server);
        if(v==null || !v.ready()) return;
        var visitors=server.getPlayerList().getPlayers().stream().filter(p->inside(p,v)).toList();
        if(visitors.isEmpty()) { clear(server); return; }
        State s=STATES.get(server);
        if(s==null) {
            s=spawn(server,v);
            if(s==null) return;
            STATES.put(server,s);
            for(var p:visitors) p.sendSystemMessage(Component.literal("Ashen Ogre: dodge the red grab cue; face the kick and hold right-click to block. The gate remains available.").withStyle(ChatFormatting.GOLD));
        }
        if(s.phase==Phase.DEFEATED) return;
        for(var p:visitors) s.bar.addPlayer(p);
        for(var p:new ArrayList<>(s.bar.getPlayers())) if(!visitors.contains(p)) s.bar.removePlayer(p);
        Husk boss=s.boss;
        if(boss.isDeadOrDying() || boss.isRemoved()) {
            stopMusic(s);
            release(s,false);
            s.phase=Phase.DEFEATED; s.bar.setProgress(0); s.bar.removeAllPlayers();
            for(var p:visitors) p.sendSystemMessage(Component.literal("Ashen Ogre defeated. Leave the hall and re-enter for a rematch.").withStyle(ChatFormatting.GREEN));
            return;
        }
        s.bar.setProgress(boss.getHealth()/HEALTH);
        var targets=visitors.stream().filter(p->!p.isCreative()).toList();
        if(targets.isEmpty()) { stopMusic(s); boss.getNavigation().stop(); if(s.phase!=Phase.INTRO) { release(s,false); phase(s,Phase.INTRO); } return; }
        ServerPlayer target=s.target==null?null:server.getPlayerList().getPlayer(s.target);
        if(target==null || !targets.contains(target)) {
            release(s,false);
            target=targets.stream().min(Comparator.comparingDouble(boss::distanceToSqr)).orElseThrow();
            s.target=target.getUUID(); phase(s,Phase.INTRO);
        }
        s.age++;
        // Allow the spawn packet to reach clients, then begin the score when a live target is noticed.
        if(!s.music && s.age>=2) { s.music=true; boss.level().broadcastEntityEvent(boss,MUSIC_START); }
        else if(s.music && server.getTickCount()%40==0) boss.level().broadcastEntityEvent(boss,MUSIC_START);
        switch(s.phase) {
            case INTRO -> { if(s.age>=60) phase(s,Phase.CHASE); }
            case CHASE -> {
                boss.lookAt(target,30,30);
                if(s.age%10==1) boss.getNavigation().moveTo(target,1.0);
                if(boss.distanceToSqr(target)<=3.2*3.2 && boss.hasLineOfSight(target)) {
                    boss.getNavigation().stop();
                    s.aim=target.position().subtract(boss.position()).multiply(1,0,1).normalize();
                    float yaw=(float)Math.toDegrees(Math.atan2(-s.aim.x,s.aim.z));
                    boss.setYRot(yaw); boss.setYHeadRot(yaw); boss.setYBodyRot(yaw);
                    boolean grab=s.attacks++%2==0;
                    phase(s,grab?Phase.GRAB_WINDUP:Phase.KICK_WINDUP);
                    boss.setAggressive(true);
                    target.sendSystemMessage(Component.literal(grab?"GRAB — DODGE!":"KICK — BLOCK OR DODGE!").withStyle(grab?ChatFormatting.RED:ChatFormatting.GOLD),true);
                    boss.playSound(grab?SoundEvents.HUSK_AMBIENT:SoundEvents.IRON_GOLEM_ATTACK,1,.7f);
                }
            }
            case GRAB_WINDUP -> {
                trackWindup(s,target,20);
                cue(s,true);
                if(s.age>=24) {
                    if(inReach(s,target,3.1,100) && target.hurtServer(target.level(),new DamageSource(target.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(GRAB_DAMAGE),boss),8)) {
                        target.stopUsingItem();
                        if(target.isAlive() && target.startRiding(boss,true,true)) phase(s,Phase.GRAB_HOLD);
                        else phase(s,Phase.RECOVER);
                    } else phase(s,Phase.RECOVER);
                }
            }
            case GRAB_HOLD -> {
                if(target.getVehicle()!=boss || s.age>=12) { release(s,true); phase(s,Phase.RECOVER); }
            }
            case KICK_WINDUP -> {
                trackWindup(s,target,13);
                cue(s,false);
                if(s.age>=16) {
                    boss.level().broadcastEntityEvent(boss,STRIKE);
                    if(inReach(s,target,3.6,120)) {
                        float health=target.getHealth();
                        target.hurtServer(target.level(),target.level().damageSources().mobAttack(boss),6);
                        if(target.getHealth()<health) launch(target,s.aim,1.1,.3);
                    }
                    phase(s,Phase.RECOVER,false);
                }
            }
            case RECOVER -> {
                // Continue visually following the same victim through the kick's follow-through.
                if(s.attacks%2==0 && s.age<10) faceTarget(s,target);
                if(s.age>=24) phase(s,Phase.CHASE);
            }
            default -> { }
        }
    }
    private static State spawn(MinecraftServer server,ArenaService.View v) {
        var level=server.overworld();
        var bounds=new AABB(v.ax()-24,v.ay(),v.az(),v.ax()+25,v.ay()+31,v.az()+65);
        for(var stale:level.getEntitiesOfClass(Husk.class,bounds,e->e.entityTags().contains(TAG))) { stale.ejectPassengers(); stale.discard(); }
        var boss=(Husk)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("husk")).create(level,EntitySpawnReason.COMMAND);
        if(boss==null) return null;
        boss.addTag(TAG); boss.setCustomName(Component.literal(NAME).withStyle(ChatFormatting.DARK_RED));
        boss.setCustomNameVisible(true); boss.setPersistenceRequired(); boss.setCanPickUpLoot(false); boss.setBaby(false);
        ((OgreMobAccess)boss).arena$clearGoals();
        boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH);
        boss.getAttribute(Attributes.SCALE).setBaseValue(1.9);
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.25);
        boss.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(.85);
        var reinforcements=boss.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if(reinforcements!=null) reinforcements.setBaseValue(0);
        boss.setHealth(HEALTH); boss.refreshDimensions(); boss.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        boss.setPos(v.ax()+.5,v.ay()+1,v.az()+43.5);
        level.addFreshEntity(boss);
        State s=new State(); s.boss=boss;
        s.bar=new ServerBossEvent(UUID.randomUUID(),Component.literal(NAME),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
        return s;
    }
    private static boolean inReach(State s,ServerPlayer target,double range,double arc) {
        Vec3 delta=target.position().subtract(s.boss.position()).multiply(1,0,1);
        return delta.lengthSqr()<=range*range && Math.abs(target.getY()-s.boss.getY())<2.5
            && (delta.lengthSqr()<.01 || delta.normalize().dot(s.aim)>=Math.cos(Math.toRadians(arc/2))) && s.boss.hasLineOfSight(target);
    }
    private static void faceTarget(State s,ServerPlayer target) {
        Vec3 delta=target.position().subtract(s.boss.position()).multiply(1,0,1);
        if(delta.lengthSqr()>.01) s.aim=delta.normalize();
        faceAim(s);
    }
    private static void faceAim(State s) {
        float yaw=(float)Math.toDegrees(Math.atan2(-s.aim.x,s.aim.z));
        s.boss.setYRot(yaw); s.boss.setYHeadRot(yaw); s.boss.setYBodyRot(yaw);
    }
    private static void trackWindup(State s,ServerPlayer target,int commitAge) {
        if(s.age<commitAge) {
            faceTarget(s,target);
            // A short approach keeps a circling player in reach; use native navigation for collisions.
            if(s.age<commitAge-4 && s.boss.distanceToSqr(target)>2.2*2.2 && s.boss.hasLineOfSight(target)) {
                if(s.age%4==1) s.boss.getNavigation().moveTo(target,1.35);
            } else s.boss.getNavigation().stop();
        } else s.boss.getNavigation().stop();
        // The final 0.2s of a grab / 0.15s of a kick commit to this aim, allowing a late dodge.
        faceAim(s);
    }
    private static void cue(State s,boolean grab) {
        if(s.age%6==0) ((net.minecraft.server.level.ServerLevel)s.boss.level()).sendParticles(grab?ParticleTypes.ANGRY_VILLAGER:ParticleTypes.CRIT,
            s.boss.getX(),s.boss.getY()+2.7,s.boss.getZ(),4,.45,.35,.45,0);
    }
    private static void phase(State s,Phase next) { phase(s,next,true); }
    private static void phase(State s,Phase next,boolean animate) {
        s.phase=next; s.age=0;
        if(animate) s.boss.level().broadcastEntityEvent(s.boss,switch(next) { case GRAB_WINDUP->GRAB; case GRAB_HOLD->HOLD; case KICK_WINDUP->KICK; default->IDLE; });
        if(next==Phase.RECOVER || next==Phase.INTRO) { s.boss.getNavigation().stop(); s.boss.setAggressive(false); }
    }
    private static void launch(ServerPlayer player,Vec3 aim,double speed,double up) {
        player.fallDistance=0; player.setDeltaMovement(aim.x*speed,up,aim.z*speed);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
    private static void release(State s,boolean throwing) {
        for(var rider:new ArrayList<>(s.boss.getPassengers())) {
            rider.stopRiding();
            if(rider instanceof ServerPlayer p) { p.fallDistance=0; if(throwing && p.isAlive()) launch(p,s.aim,.95,.28); }
        }
    }
    public static void clear(MinecraftServer server) {
        State s=STATES.remove(server);
        if(s!=null) { stopMusic(s); release(s,false); s.bar.removeAllPlayers(); if(!s.boss.isRemoved()) s.boss.discard(); }
    }
    private static void stopMusic(State s) {
        if(s.music) { s.music=false; s.boss.level().broadcastEntityEvent(s.boss,MUSIC_STOP); }
    }
}
