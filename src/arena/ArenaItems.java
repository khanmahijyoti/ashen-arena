package arena;

import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.sounds.SoundEvents;
import java.util.Optional;
import java.util.List;
import net.minecraft.tags.DamageTypeTags;

public final class ArenaItems {
    public static final Identifier KATANA_ID=Identifier.fromNamespaceAndPath("ashen_arena","ashen_katana");
    public static final ResourceKey<Item> KATANA_KEY=ResourceKey.create(Registries.ITEM,KATANA_ID);
    private static BlocksAttacks katanaGuard(HolderLookup.Provider registries) {
        return new BlocksAttacks(0f,1f,
            List.of(new BlocksAttacks.DamageReduction(90f,Optional.empty(),0f,1f)),
            new BlocksAttacks.ItemDamageFunction(1f,0f,1f),
            Optional.of(registries.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
            Optional.of(Holder.direct(SoundEvents.IRON_HIT)),Optional.of(SoundEvents.SHIELD_BREAK));
    }
    public static final Item ASHEN_KATANA=Registry.register(BuiltInRegistries.ITEM,KATANA_KEY,
        new Item(new Item.Properties().setId(KATANA_KEY).sword(ToolMaterial.IRON,4f,-2.2f)
            .delayedComponent(DataComponents.BLOCKS_ATTACKS,ArenaItems::katanaGuard)
            .component(DataComponents.USE_EFFECTS,new UseEffects(false,false,.35f))
            .durability(640).rarity(Rarity.UNCOMMON)));
    public static void initialize() { }
}
