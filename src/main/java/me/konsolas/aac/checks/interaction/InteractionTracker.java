package me.konsolas.aac.checks.interaction;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

public interface InteractionTracker {

    default void updatePlayer(Player player, Location location) {
    }

    default void computeTick() {
    }

    default void tick() {
    }

    default void updateEnumWrappersPlayerDigType(PlayerDigType playerDigType, BlockPosition blockPosition) {
    }

    default void findTick() {
    }

    default void updateBlockPosition(BlockPosition blockPosition, BlockPosition blockPosition2, Hand hand) {
    }

    default void processTick() {
    }

    default void collectTick() {
    }

    default void inspectTick() {
    }

    default void updateVector(Vector vector) {
    }

    default void updateAxisAlignedBoundingBox(AxisAlignedBoundingBox box) {
    }

    default void readTick() {
    }

    default void measureTick() {
    }

    default void evaluateTick() {
    }

    default void updateBlock(Block block) {
    }

    default void updateAction(Action action, Block block, BlockFace blockFace) {
    }

    default void resolveTick() {
    }

    default void updateMaterial(Material material, double value) {
    }

    default void deriveTick() {
    }
}
