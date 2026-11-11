package arena.mixin;

import arena.ArenaService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerExplosion.class)
public class ProtectedExplosionMixin {
    @Shadow @Final private ServerLevel level;
    @ModifyVariable(method={"interactWithBlocks","createFire"},at=@At("HEAD"),argsOnly=true)
    private List<BlockPos> arena$filterBlast(List<BlockPos> positions) {
        List<BlockPos> allowed=new ArrayList<>(positions.size());
        for(BlockPos pos:positions) if(!ArenaService.protectedBlock(level,pos)) allowed.add(pos);
        return allowed;
    }
}
