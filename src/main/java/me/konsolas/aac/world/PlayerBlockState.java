package me.konsolas.aac.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class PlayerBlockState {

    private static final Set LIQUID_MATERIALS = Collections.unmodifiableSet(EnumSet.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST));

    private static final List<BlockFace> INTERACTABLE_FACES = Collections.unmodifiableList(Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN));

    public static boolean checkPlayerBlockState(Player player, Block block, Block block2) {
        Location location = player.getLocation();
        if (!(location.getY() <= block.getY()) && block.getType().isSolid() && !(location.distanceSquared(block.getLocation()) >= 4.0) && LIQUID_MATERIALS.contains(block.getFace(block2)) && block.getType() != Material.LADDER && block.getType() != Material.VINE) {
            boolean matches = false;
            for (BlockFace blockFace : INTERACTABLE_FACES) {
                if (block.getRelative(blockFace).getType().isBlock()) {
                    if (matches) {
                        return false;
                    }
                    matches = true;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
