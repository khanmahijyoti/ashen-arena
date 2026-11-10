package arena;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Coordinates relative to the hall centre at its southern entrance. No external assets. */
public final class Hall {
    public static final int HALF = 24, LENGTH = 64, HEIGHT = 30;
    public static final int VOLUME = (HALF * 2 + 1) * (LENGTH + 1) * (HEIGHT + 1);

    public static int[] coordinate(int index) {
        int x = index % 49 - HALF;
        index /= 49;
        int z = index % 65;
        return new int[]{x, index / 65, z};
    }

    public static BlockState block(int x, int y, int z) {
        int ax = Math.abs(x);
        if (y == 0) {
            if (Math.floorMod(x,8)==0 && z%8==0) return Blocks.SHROOMLIGHT.defaultBlockState();
            if (ax == 23 || z == 1 || z == 63 || (ax == 5 && z > 3 && z < 59)) return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            if (ax < 5 && z > 2 && z < 59) return Blocks.RED_NETHER_BRICKS.defaultBlockState();
            if ((x * x + (z - 33) * (z - 33)) % 41 < 3) return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
            return Blocks.DEEPSLATE_TILES.defaultBlockState();
        }
        if (y == HEIGHT) return ((ax <= 1 && z % 8 < 3) ? Blocks.SHROOMLIGHT : Blocks.DEEPSLATE_TILES).defaultBlockState();
        // Recessed gold windows alternate with severe dark masonry.
        if (ax == HALF || z == LENGTH || z == 0) {
            if (z == 0 && ax <= 2 && y <= 6) return Blocks.STAINED_GLASS.yellow().defaultBlockState();
            if (ax == HALF && z % 10 >= 3 && z % 10 <= 6 && y >= 9 && y <= 23)
                return (y == 9 || y == 23 ? Blocks.SHROOMLIGHT : Blocks.STAINED_GLASS.orange()).defaultBlockState();
            if (y == 1 || y == 7 || y == 25 || y == 29) return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
            return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        }
        // Two rows of columns and raised arch ribs leave a broad combat floor.
        if ((ax == 16 || ax == 17) && z >= 8 && z <= 58 && z % 10 <= 1) {
            return (y <= 2 || y >= 24 ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS).defaultBlockState();
        }
        if (z >= 8 && z <= 58 && z % 10 <= 1 && y >= 27 - ax / 5) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        // Red hanging standards, inset lighting, dais and empty throne.
        if (ax == 22 && z % 10 == 5 && y >= 12 && y <= 23) return Blocks.WOOL.red().defaultBlockState();
        if (ax == 20 && z % 10 == 0 && y == 2) return Blocks.SHROOMLIGHT.defaultBlockState();
        if (z >= 56 && z <= 62 && ax <= 7 && y <= 2) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (z >= 58 && z <= 61 && ax <= 2 && y <= 3) return Blocks.RED_NETHER_BRICKS.defaultBlockState();
        if (z == 61 && ax <= 2 && y <= 8) return (ax == 2 || y == 8 ? Blocks.GILDED_BLACKSTONE : Blocks.CHISELED_POLISHED_BLACKSTONE).defaultBlockState();
        if (z == 59 && ax == 3 && y <= 5) return Blocks.GILDED_BLACKSTONE.defaultBlockState();
        return null;
    }

    public static BlockState gate(int x, int y, int z) {
        if (y == -1) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (z != 0) return null;
        if (Math.abs(x) == 3 || y == 7) return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        if (Math.abs(x) <= 2 && y <= 6) return Blocks.STAINED_GLASS.yellow().defaultBlockState();
        return null;
    }
}
