package arena;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

public final class ArenaLoadout {
    private static final Map<MinecraftServer,State> STATES=new WeakHashMap<>();
    private static final class State {
        final Properties given=new Properties();
        final Set<UUID> warned=new HashSet<>();
        boolean failed;
    }
    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("ashen-arena-loadout.properties");
    }
    private static State state(MinecraftServer server) {
        Path file=file(server);
        State s=STATES.get(server);
        if(s==null) {
            s=new State(); STATES.put(server,s);
            if(Files.exists(file)) try(Reader r=Files.newBufferedReader(file)) { s.given.load(r); }
            catch(IOException e) { s.failed=true; System.getLogger("AshenArena").log(System.Logger.Level.ERROR,"Could not read starter sword records",e); }
        }
        return s;
    }
    public static void tick(MinecraftServer server) {
        if(server.getTickCount()%20!=0) return;
        State s=state(server);
        if(s.failed) return;
        for(var player:server.getPlayerList().getPlayers()) if(player.isAlive()) issue(server,player,s);
    }
    public static void afterDeathRespawn(ServerPlayer player) {
        if(player.getInventory().contains(stack->stack.getItem()==ArenaItems.ASHEN_KATANA)) return;
        var server=player.level().getServer();
        State s=state(server);
        if(s.failed) return;
        s.given.remove(player.getUUID().toString());
        s.warned.remove(player.getUUID());
        save(server,s);
        issue(server,player,s);
    }
    private static void issue(MinecraftServer server,ServerPlayer player,State s) {
            String id=player.getUUID().toString();
            if(s.given.containsKey(id)) return;
            boolean existing=player.getInventory().contains(stack->stack.getItem()==ArenaItems.ASHEN_KATANA);
            if(!existing) {
                player.getInventory().add(new ItemStack(ArenaItems.ASHEN_KATANA));
                if(!player.getInventory().contains(stack->stack.getItem()==ArenaItems.ASHEN_KATANA)) {
                    if(s.warned.add(player.getUUID())) player.sendSystemMessage(Component.literal("[Ashen Arena] Free one inventory slot to receive your Ashen Katana."));
                    return;
                }
                player.sendSystemMessage(Component.literal("§6[Ashen Arena] §fAshen Katana added to your inventory. Left-click to attack."));
            }
            s.given.setProperty(id,"ashen_katana");
            save(server,s);
    }
    private static void save(MinecraftServer server,State s) {
            Path file=file(server),temp=file.resolveSibling("ashen-arena-loadout.properties.tmp");
            try {
                try(Writer w=Files.newBufferedWriter(temp)) { s.given.store(w,"Starter swords already issued"); }
                try { Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
                catch(AtomicMoveNotSupportedException e) { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING); }
            } catch(IOException e) { System.getLogger("AshenArena").log(System.Logger.Level.ERROR,"Could not save starter sword record",e); }
    }
}
