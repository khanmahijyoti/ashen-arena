package arena;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

public final class ArenaClient {
    private static IntegratedServer seen;
    private static int ticks;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) { seen = null; return; }
        if (seen != server) {
            seen = server;
            mc.player.sendSystemMessage(Component.literal("§6Ashen Arena §7— press F8 in an open area to place your entrance. E enters or leaves at the fog gate."));
        }
        if (++ticks % 10 != 0 || mc.gui.screen() != null) return;
        ArenaService.View view = ArenaService.view(server);
        if (view == null) return;
        if (view.near(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD))) {
            mc.gui.hud.setOverlayMessage(Component.literal(view.ready() ? "§e[E] §fTraverse the golden mist" : "§6Preparing the Ashen Hall… " + view.progress() + "%"), false);
        }
    }

    public static boolean key(int action, int key) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null || mc.gui.screen() != null) return false;
        boolean setup = key == InputConstants.KEY_F8;
        ArenaService.View view = ArenaService.view(server);
        boolean enter = key == InputConstants.KEY_E && view != null && view.near(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD));
        if (!setup && !enter) return false;
        if (action == InputConstants.PRESS) {
            var uuid = mc.player.getUUID();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    if (setup) ArenaService.setup(server, player);
                    else ArenaService.traverse(server, player);
                }
            });
        }
        return true;
    }
}

