package arena.client;

import arena.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public final class OgreMusic {
    private static ClientLevel world;
    private static UUID encounter;
    private static int entityId,ticks,lastAttempt=-100,starts;
    private static boolean wanted;
    private static BattleTrack track;
    public record Snapshot(boolean requested,boolean playing,boolean looping,String sound,SoundSource source,int starts) { }
    public static Snapshot snapshot() {
        return new Snapshot(wanted,track!=null && Minecraft.getInstance().getSoundManager().isActive(track),
            track!=null && track.isLooping(),track==null?"":track.getIdentifier().toString(),track==null?SoundSource.MUSIC:track.getSource(),starts);
    }
    public static boolean event(Entity actor,byte event) {
        if(!OgreEncounter.isOgre(actor) || (event!=OgreEncounter.MUSIC_START && event!=OgreEncounter.MUSIC_STOP)) return false;
        if(event==OgreEncounter.MUSIC_STOP) {
            if(actor.getUUID().equals(encounter)) wanted=false;
            return true;
        }
        var mc=Minecraft.getInstance();
        if(!actor.getUUID().equals(encounter) || world!=mc.level) {
            stopImmediately(); world=mc.level; encounter=actor.getUUID(); entityId=actor.getId();
        }
        wanted=validContext();
        if(wanted && track==null && ticks-lastAttempt>=40) play();
        return true;
    }
    public static void tick() {
        ticks++;
        var mc=Minecraft.getInstance();
        if(world!=null && world!=mc.level) { stopImmediately(); return; }
        if(wanted && !validContext()) wanted=false;
        if(track!=null && (track.isStopped() || ((!wanted || ticks-lastAttempt>40) && !mc.getSoundManager().isActive(track)))) {
            mc.getSoundManager().stop(track); track=null;
        }
        if(wanted && track==null && ticks-lastAttempt>=40) play();
        if(!wanted && track==null) { world=null; encounter=null; }
    }
    private static boolean validContext() {
        var mc=Minecraft.getInstance();
        if(world==null || world!=mc.level || mc.player==null || !mc.player.isAlive() || mc.player.isSpectator() || mc.player.isCreative()) return false;
        var actor=world.getEntity(entityId);
        if(actor==null || !actor.isAlive() || !actor.getUUID().equals(encounter)) return false;
        var server=mc.getSingleplayerServer(); var v=server==null?null:ArenaService.view(server);
        return v!=null && v.ready() && Math.abs(mc.player.getX()-(v.ax()+.5))<Hall.HALF-1
            && mc.player.getY()>=v.ay() && mc.player.getY()<v.ay()+Hall.HEIGHT
            && mc.player.getZ()>v.az()+1 && mc.player.getZ()<v.az()+Hall.LENGTH-1;
    }
    private static void play() {
        var mc=Minecraft.getInstance();
        lastAttempt=ticks;
        mc.getMusicManager().stopPlaying();
        mc.getSoundManager().updateCategoryVolume(SoundSource.MUSIC,1);
        BattleTrack next=new BattleTrack(encounter);
        if(mc.getSoundManager().play(next)!=SoundEngine.PlayResult.NOT_STARTED) { track=next; starts++; }
    }
    public static boolean suppressAmbient() { return wanted || (track!=null && !track.isStopped()); }
    public static boolean isBattleTrack(SoundInstance sound) { return sound instanceof BattleTrack; }
    private static void stopImmediately() {
        wanted=false;
        if(track!=null) Minecraft.getInstance().getSoundManager().stop(track);
        track=null; world=null; encounter=null;
    }
    private static final class BattleTrack extends AbstractTickableSoundInstance {
        private final UUID fight;
        BattleTrack(UUID fight) {
            super(SoundEvents.MUSIC_DRAGON.value(),SoundSource.MUSIC,RandomSource.create());
            this.fight=fight; volume=.01f; pitch=1; looping=true; delay=0;
            relative=true; attenuation=SoundInstance.Attenuation.NONE;
        }
        @Override public boolean canStartSilent() { return true; }
        @Override public void tick() {
            boolean active=wanted && fight.equals(encounter);
            volume=active?Math.min(.7f,volume+.035f):Math.max(0,volume-.07f);
            if(!active && volume<=0) stop();
        }
    }
}
