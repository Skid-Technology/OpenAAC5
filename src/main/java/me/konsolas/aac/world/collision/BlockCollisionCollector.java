package me.konsolas.aac.world.collision;

import java.util.ArrayList;
import java.util.List;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;

public class BlockCollisionCollector {

    public static List findList(Entity entity, AxisAlignedBoundingBox box, ProtocolVersion version) {
        ArrayList values = new ArrayList();
        int value = NumberConversions.floor(box.getMinX());
        int value2 = NumberConversions.floor(box.getMinY() - 1.0);
        int value3 = NumberConversions.floor(box.getMinZ());
        int value4 = NumberConversions.ceil(box.getMaxX());
        int value5 = NumberConversions.ceil(box.getMaxY());
        int value6 = NumberConversions.ceil(box.getMaxZ());
        for (int value7 = value; value7 < value4; value7++) {
            for (int value8 = value2; value8 < value5; value8++) {
                for (int value9 = value3; value9 < value6; value9++) {
                    Block blockAt = entity.getWorld().getBlockAt(value7, value8, value9);
                    Material type = blockAt.getType();
                    ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
                    ProtocolVersion version2 = ProtocolVersion.V1_14;
                    if (!serverVersion.isAtLeast(version2) || type != Material.SCAFFOLDING) {
                        for (AxisAlignedBoundingBox box2 : BlockCollisionShapes.findCollisionShape(entity, blockAt, type, version).getBoxesAt(value7, value8, value9)) {
                            if (box2.intersects(box)) {
                                values.add(box2);
                            }
                        }
                    }
                }
            }
        }
        return values;
    }
}
