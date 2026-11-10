package arena;

import net.fabricmc.api.ModInitializer;

public final class ArenaMod implements ModInitializer {
    @Override public void onInitialize() { ArenaItems.initialize(); }
}
