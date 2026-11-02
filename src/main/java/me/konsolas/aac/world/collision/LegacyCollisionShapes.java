package me.konsolas.aac.world.collision;

import java.util.ArrayList;
import java.util.List;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.MaterialAlias;
import me.konsolas.aac.world.MaterialSupplier;
import org.bukkit.Material;
import org.bukkit.util.NumberConversions;

public class LegacyCollisionShapes {

    private static final Material GRASS_PATH;

    public static List findList(ProtocolVersion version, MaterialSupplier materialSupplier, List<AxisAlignedBoundingBox> values) {
        ArrayList values2 = new ArrayList(values.size());
        for (AxisAlignedBoundingBox box : values) {
            NumberConversions.floor(box.getMinX());
            NumberConversions.floor(box.getMinY());
            NumberConversions.floor(box.getMinZ());
            Material material = materialSupplier.getMaterial();
            AxisAlignedBoundingBox box2 = findAxisAlignedBoundingBox(version, box, material);
            values2.add(box2);
        }
        return values2;
    }

    public static AxisAlignedBoundingBox findAxisAlignedBoundingBox(ProtocolVersion version, AxisAlignedBoundingBox box, Material material) {
        AxisAlignedBoundingBox box2;
        if (material == Material.LADDER) {
            box2 = resolveFindAxisAlignedBoundingBox(version, box);
        } else if (material == MaterialAlias.LILY_PAD.getMaterial()) {
            box2 = deriveFindAxisAlignedBoundingBox(version, box);
        } else if (material == MaterialAlias.FARMLAND.getMaterial()) {
            box2 = findAxisAlignedBoundingBoxWithV1_9(version, box);
        } else if (MaterialAlias.CARPET.contains(material)) {
            box2 = computeFindAxisAlignedBoundingBox(version, box);
        } else {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version2 = ProtocolVersion.V1_9;
            if (serverVersion.isAtLeast(version2) && material == GRASS_PATH) {
                box2 = evaluateFindAxisAlignedBoundingBox(version, box);
            } else if (material != Material.CHEST && material != Material.TRAPPED_CHEST) {
                box2 = box;
            } else {
                box2 = findAxisAlignedBoundingBox(version, box);
            }
        }
        return box2;
    }

    private static AxisAlignedBoundingBox resolveFindAxisAlignedBoundingBox(ProtocolVersion version, AxisAlignedBoundingBox box) {
        ProtocolVersion version2 = ProtocolVersion.V1_9;
        double value;
        if (version.isAtLeast(version2)) {
            value = 0.1875;
        } else {
            value = 0.125;
        }
        double minX = box.getMinX();
        if (checkLegacyCollisionShapes(minX)) {
            return box.withMinX(box.getMaxX() - value);
        } else {
            double minZ = box.getMinZ();
            if (checkLegacyCollisionShapes(minZ)) {
                return box.withMinZ(box.getMaxZ() - value);
            } else {
                double maxX = box.getMaxX();
                if (checkLegacyCollisionShapes(maxX)) {
                    return box.withMaxX(box.getMinX() + value);
                } else {
                    double maxZ = box.getMaxZ();
                    if (checkLegacyCollisionShapes(maxZ)) {
                        return box.withMaxZ(box.getMinZ() + value);
                    } else {
                        throw new IllegalArgumentException("unrecognised ladder hitbox " + box);
                    }
                }
            }
        }
    }

    private static AxisAlignedBoundingBox deriveFindAxisAlignedBoundingBox(ProtocolVersion version, AxisAlignedBoundingBox box) {
        ProtocolVersion version2 = ProtocolVersion.V1_9;
        double value;
        double value2;
        if (version.isAtLeast(version2)) {
            value = 0.09375;
            value2 = 0.0625;
        } else {
            value = 0.015625;
            value2 = 0.0;
        }
        return new AxisAlignedBoundingBox(Math.floor(box.getMinX()) + value2, box.getMinY(), Math.floor(box.getMinZ()) + value2, Math.ceil(box.getMaxX()) - value2, box.getMinY() + value, Math.ceil(box.getMaxZ()) - value2);
    }

    private static AxisAlignedBoundingBox findAxisAlignedBoundingBoxWithV1_9(ProtocolVersion version, AxisAlignedBoundingBox box) {
        ProtocolVersion version2 = ProtocolVersion.V1_9;
        double value;
        if (version.isAtLeast(version2)) {
            value = 0.9375;
        } else {
            value = 1.0;
        }
        return box.withMaxY(box.getMinY() + value);
    }

    private static AxisAlignedBoundingBox computeFindAxisAlignedBoundingBox(ProtocolVersion version, AxisAlignedBoundingBox box) {
        return box.withMaxY(box.getMinY() + (double) 0.0625);
    }

    private static AxisAlignedBoundingBox evaluateFindAxisAlignedBoundingBox(ProtocolVersion version, AxisAlignedBoundingBox box) {
        ProtocolVersion version2 = ProtocolVersion.V1_9;
        return version.isAtLeast(version2) ? box : box.withMaxY(box.getMinY() + 1.0);
    }

    private static AxisAlignedBoundingBox findAxisAlignedBoundingBox(ProtocolVersion version, AxisAlignedBoundingBox box) {
        return box;
    }

    private static boolean checkLegacyCollisionShapes(double value) {
        return value != (int) value;
    }

    static {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_17;
        Material material;
        if (serverVersion.isAtLeast(version)) {
            material = Material.DIRT_PATH;
        } else {
            serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version2 = ProtocolVersion.V1_9;
            material = serverVersion.isAtLeast(version2) ? Material.getMaterial("GRASS_PATH") : null;
        }
        GRASS_PATH = material;
    }
}
