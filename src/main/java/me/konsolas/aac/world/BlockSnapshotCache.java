package me.konsolas.aac.world;

import java.util.HashMap;
import java.util.Map;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.time.ExpiringCounter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockSnapshotCache {

    public static final BlockSnapshot AIR = new BlockSnapshot(Material.AIR, 0.6F, null, -1, null, false);

    private static final ExpiringCounter evictionTimer = new ExpiringCounter(300000L);

    private static final Map snapshots = new HashMap();

    private static BlockState lastBlockState = null;

    private static BlockState findBlockState(ProtocolVersion version, Block block) {
        if (lastBlockState != null) {
            lastBlockState.updateProtocolVersion(version, block);
        } else {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version2 = ProtocolVersion.V1_13;
            lastBlockState = (BlockState) (serverVersion.isAtLeast(version2) ? new ModernBlockState(version, block) : new LegacyBlockState(version, block));
        }
        return lastBlockState;
    }

    public static BlockSnapshot findBlockSnapshot(Player player, Block block, ProtocolVersion version) {
        if (block.isEmpty()) {
            return AIR;
        } else {
            BlockState blockState = findBlockState(version, block);
            BlockSnapshot blockSnapshot = (BlockSnapshot) snapshots.get(blockState);
            if (blockSnapshot != null) {
                return blockSnapshot;
            } else {
                blockSnapshot = new BlockSnapshot(player, block, version);
                snapshots.put(blockState.copy(), blockSnapshot);
                if (evictionTimer.tryAcquire()) {
                    Bukkit.getLogger().info("[AAC5] Cached " + snapshots.size() + " blocks. (newest: " + blockState + ")");
                }
                return blockSnapshot;
            }
        }
    }

    static {
        evictionTimer.tryAcquire();
    }
}
