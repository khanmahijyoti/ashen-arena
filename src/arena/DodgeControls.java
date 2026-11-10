package arena;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.*;
import java.util.Arrays;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class DodgeControls {
    public static final String NAME="key.ashen_arena.dodge";
    public static KeyMapping dodge;
    private static boolean initialized;

    /** Called before Options.load so the normal controls screen and options persistence include Dodge. */
    public static KeyMapping[] register(KeyMapping[] mappings) {
        for(var mapping:mappings) if(mapping.getName().equals(NAME)) { dodge=mapping; return mappings; }
        dodge=new KeyMapping(NAME,InputConstants.KEY_LSHIFT,KeyMapping.Category.MOVEMENT);
        var result=Arrays.copyOf(mappings,mappings.length+1);
        result[mappings.length]=dodge;
        return result;
    }
    public static void initialize(Minecraft mc) {
        if(initialized) return;
        initialized=true;
        Path root=mc.gameDirectory.toPath();
        Path marker=root.resolve("config/ashen-arena-dodge-controls-v1.marker");
        if(Files.exists(marker)) return;
        try {
            Path options=root.resolve("options.txt"), backup=root.resolve("options.before-ashen-dodge.txt");
            if(Files.exists(options) && !Files.exists(backup)) Files.copy(options,backup);
            mc.options.keyShift.setKey(InputConstants.getKey("key.keyboard.left.control"));
            // Left Ctrl is now crouch; retain dedicated sprint on Right Ctrl.
            if(mc.options.keySprint.saveString().equals("key.keyboard.left.control"))
                mc.options.keySprint.setKey(InputConstants.getKey("key.keyboard.right.control"));
            mc.options.keyShift.setDown(false);
            mc.options.keySprint.setDown(false);
            KeyMapping.resetMapping();
            mc.options.save();
            Files.createDirectories(marker.getParent());
            Files.writeString(marker,"Dodge defaults applied once. Further control changes are kept.\n");
        } catch(Exception e) {
            System.getLogger("AshenArena").log(System.Logger.Level.WARNING,"Could not persist dodge control defaults",e);
        }
    }
}
