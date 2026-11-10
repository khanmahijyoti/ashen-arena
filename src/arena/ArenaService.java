package arena;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

/** All mutable state and world operations are confined to the server thread. */
public final class ArenaService {
    private static final Map<MinecraftServer, State> STATES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final System.Logger LOG = System.getLogger("AshenArena");
    public record View(int gx, int gy, int gz, int ax, int ay, int az, boolean ready, int progress) {
        public boolean near(double x, double y, double z, boolean overworld) {
            return overworld && (close(x,y,z,gx,gy,gz) || close(x,y,z,ax,ay+1,az+1));
        }
        private static boolean close(double x,double y,double z,int bx,int by,int bz) {
            return Math.abs(y-by) < 3 && Math.abs(x-(bx+.5)) < 3.5 && Math.abs(z-(bz+.5)) < 5;
        }
        public boolean protects(BlockPos pos) {
            int x=pos.getX(), y=pos.getY(), z=pos.getZ();
            return (x>=gx-3 && x<=gx+3 && y>=gy-1 && y<=gy+7 && z>=gz-4 && z<=gz+2)
                || (x>=ax-Hall.HALF && x<=ax+Hall.HALF && y>=ay && y<=ay+Hall.HEIGHT && z>=az && z<=az+Hall.LENGTH);
        }
    }
    private static final class State {
        int gx,gy,gz,ax,ay,az,cursor,attempts;
        String phase = "none";
        volatile View view;
        boolean loaded;
        boolean writing;
        final Map<UUID,Integer> cooldown = new HashMap<>();
        void publish() {
            view = phase.equals("none") || phase.equals("error") ? null : new View(gx,gy,gz,ax,ay,az,phase.equals("ready"),phase.equals("verify") ? cursor*50/Hall.VOLUME : phase.equals("build") ? 50+cursor*50/Hall.VOLUME : 100);
        }
    }
    public static View view(MinecraftServer server) {
        State s = STATES.get(server);
        return s == null ? null : s.view;
    }
    public static boolean protectedBlock(Level level, BlockPos pos) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl) || !level.dimension().equals(Level.OVERWORLD)) return false;
        State s=STATES.get(sl.getServer());
        return s!=null && !s.writing && s.view!=null && s.view.protects(pos);
    }
    private static void buildBlock(State s, Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flags) {
        s.writing=true;
        try { level.setBlock(pos,block,flags); }
        finally { s.writing=false; }
    }
    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("ashen-arena.properties");
    }
    private static State state(MinecraftServer server) {
        State s;
        synchronized (STATES) { s = STATES.computeIfAbsent(server, key -> new State()); }
        if (s.loaded) return s;
        s.loaded = true;
        if (!Files.exists(file(server))) return s;
        try (Reader r = Files.newBufferedReader(file(server))) {
            Properties p = new Properties(); p.load(r);
            s.gx = Integer.parseInt(p.getProperty("gate.x"));
            s.gy = Integer.parseInt(p.getProperty("gate.y"));
            s.gz = Integer.parseInt(p.getProperty("gate.z"));
            s.ax = Integer.parseInt(p.getProperty("hall.x"));
            s.ay = Integer.parseInt(p.getProperty("hall.y"));
            s.az = Integer.parseInt(p.getProperty("hall.z"));
            s.phase = p.getProperty("phase");
            if (!Set.of("verify","build","ready").contains(s.phase)) throw new IOException("Invalid phase");
            if (s.ay != 256 || Math.abs((long)s.ax) > 29999000 || Math.abs((long)s.az) > 29999000) throw new IOException("Invalid hall coordinates");
            // A reserved build can be replayed safely after a crash. Verification never writes blocks.
            s.publish();
        } catch (Exception e) { s.phase = "error"; LOG.log(System.Logger.Level.ERROR,"Cannot read arena state; edits disabled",e); }
        return s;
    }
    private static void save(MinecraftServer server, State s) throws IOException {
        Properties p = new Properties();
        p.setProperty("gate.x",""+s.gx); p.setProperty("gate.y",""+s.gy); p.setProperty("gate.z",""+s.gz);
        p.setProperty("hall.x",""+s.ax); p.setProperty("hall.y",""+s.ay); p.setProperty("hall.z",""+s.az);
        p.setProperty("phase",s.phase);
        Path target = file(server), temp = target.resolveSibling("ashen-arena.properties.tmp");
        try (Writer w = Files.newBufferedWriter(temp)) { p.store(w,"Ashen Arena — reserved region and construction state"); }
        try { Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException e) { Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING); }
    }
    private static void tell(ServerPlayer p,String text) { p.sendSystemMessage(Component.literal("§6[Ashen Arena] §f"+text)); }
    public static void setup(MinecraftServer server, ServerPlayer player) {
        State s = state(server);
        if (!s.phase.equals("none")) {
            tell(player,s.phase.equals("error") ? "Arena state needs repair; see the game log." : "Entrance already exists at "+s.gx+", "+s.gy+", "+s.gz+". Approach the golden mist and press E."); return;
        }
        if (!player.level().dimension().equals(Level.OVERWORLD)) { tell(player,"Place the entrance in the Overworld."); return; }
        if (player.isPassenger() || !player.onGround()) { tell(player,"Stand on solid ground first."); return; }
        int gx = player.blockPosition().getX()+7, gy = player.blockPosition().getY(), gz = player.blockPosition().getZ();
        var level = server.overworld();
        if (gy < -63 || gy > 306 || Math.abs((long)gx)>29998000 || Math.abs((long)gz)>29998000) { tell(player,"Choose a spot away from world limits."); return; }
        for(int x=-3;x<=3;x++) for(int y=0;y<=7;y++) for(int z=-4;z<=2;z++) {
            BlockPos pos = new BlockPos(gx+x,gy+y,gz+z);
            if (!level.isEmptyBlock(pos) || !level.getWorldBorder().isWithinBounds(pos)) { tell(player,"Need clear space: 7 blocks wide, 7 deep, 8 tall, east of you. Try an open field."); return; }
        }
        s.gx=gx; s.gy=gy; s.gz=gz; s.ax=(gx+512)&~15; s.ay=256; s.az=gz&~15;
        s.phase="verify"; s.cursor=0;
        try { save(server,s); }
        catch(IOException e) { s.phase="none"; tell(player,"Could not save the entrance; no blocks changed."); LOG.log(System.Logger.Level.ERROR,"Save failed",e); return; }
        for(int x=-3;x<=3;x++) for(int y=-1;y<=7;y++) for(int z=-4;z<=2;z++) {
            var block = Hall.gate(x,y,z); BlockPos pos = new BlockPos(gx+x,gy+y,gz+z);
            if(block!=null && level.isEmptyBlock(pos)) buildBlock(s,level,pos,block,3);
        }
        s.publish();
        tell(player,"Entrance placed east of you. Preparing the secluded hall; progress appears near the mist. Your inventory stays with you.");
    }
    public static void tick(MinecraftServer server) {
        State s=state(server);
        if (!s.phase.equals("verify") && !s.phase.equals("build")) return;
        var level=server.overworld();
        int budget=s.phase.equals("verify") ? 4096 : 2048;
        try {
            for(int n=0;n<budget && s.cursor<Hall.VOLUME;n++,s.cursor++) {
                int[] c=Hall.coordinate(s.cursor);
                BlockPos pos=new BlockPos(s.ax+c[0],s.ay+c[1],s.az+c[2]);
                if(s.phase.equals("verify")) {
                    if(!level.isEmptyBlock(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
                        // Leave existing structures untouched; try a different reserved sky location.
                        if(++s.attempts>8) throw new IOException("No clear hall location found after eight attempts");
                        s.ax+=128; s.cursor=0;
                        if(Math.abs((long)s.ax)>29998000) throw new IOException("No clear space within world border");
                        save(server,s); s.publish(); return;
                    }
                } else {
                    var block=Hall.block(c[0],c[1],c[2]);
                    if(block!=null) buildBlock(s,level,pos,block,2);
                }
            }
            if(s.cursor==Hall.VOLUME) {
                s.phase=s.phase.equals("verify") ? "build" : "ready";
                s.cursor=0; save(server,s);
                if(s.phase.equals("ready")) {
                    for(var player:server.getPlayerList().getPlayers()) tell(player,"The Ashen Hall is ready. Approach the golden entrance and press E. Press E at the inside gate to return.");
                }
            }
            s.publish();
        } catch(Exception e) {
            s.phase="error"; s.publish(); LOG.log(System.Logger.Level.ERROR,"Arena construction stopped",e);
            for(var player:server.getPlayerList().getPlayers()) tell(player,"Construction stopped safely: "+e.getMessage()+". See the game log.");
        }
    }
    public static void traverse(MinecraftServer server,ServerPlayer player) {
        State s=state(server); View v=s.view;
        if(v==null || !v.near(player.getX(),player.getY(),player.getZ(),player.level().dimension().equals(Level.OVERWORLD))) return;
        if(!v.ready()) { tell(player,"Hall is still preparing. Please wait."); return; }
        int now=server.getTickCount();
        if(now-s.cooldown.getOrDefault(player.getUUID(),now-30)<20) return;
        if(player.isPassenger() || player.isSleeping()) { tell(player,"Dismount or wake up first."); return; }
        boolean inside=View.close(player.getX(),player.getY(),player.getZ(),s.ax,s.ay+1,s.az+1);
        double x=inside?s.gx+.5:s.ax+.5, y=inside?s.gy:s.ay+1, z=inside?s.gz-3.5:s.az+4.5;
        var level=server.overworld();
        BlockPos feet=BlockPos.containing(x,y,z);
        // Do not send a player into a broken floor, obstructed exit, or fluid.
        if(!level.isEmptyBlock(feet) || !level.isEmptyBlock(feet.above()) || !level.getBlockState(feet.below()).isSolidRender()) {
            tell(player,"Destination is obstructed. Clear the doorway and repair its floor first."); return;
        }
        if(player.teleportTo(level,x,y,z,Set.of(),inside?180:0,0,true)) {
            player.fallDistance=0; s.cooldown.put(player.getUUID(),now);
            tell(player,inside?"Returned from the Ashen Hall.":"ASHEN HALL — approach the golden mist behind you and press E to return.");
        }
    }
}
