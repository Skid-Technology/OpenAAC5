package me.konsolas.aac.world.collision;

import java.util.List;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class CollisionResolver {

    public static AxisAlignedBoundingBox findAxisAlignedBoundingBox(AxisAlignedBoundingBox box, Vector vector, List<AxisAlignedBoundingBox> values) {
        for (AxisAlignedBoundingBox box2 : values) {
            double y = vector.getY();
            vector.setY(box2.clipYMovement(box, y));
        }
        box = box.offset(0.0, vector.getY(), 0.0);
        boolean matches = Math.abs(vector.getX()) < Math.abs(vector.getZ());
        if (matches && vector.getZ() != 0.0) {
            for (AxisAlignedBoundingBox box3 : values) {
                double z = vector.getZ();
                vector.setZ(box3.clipZMovement(box, z));
            }
            box = box.offset(0.0, 0.0, vector.getZ());
        }
        if (vector.getX() != 0.0) {
            for (AxisAlignedBoundingBox box4 : values) {
                double x = vector.getX();
                vector.setX(box4.clipXMovement(box, x));
            }
            box = box.offset(vector.getX(), 0.0, 0.0);
        }
        if (!matches && vector.getZ() != 0.0) {
            for (AxisAlignedBoundingBox box5 : values) {
                double z2 = vector.getZ();
                vector.setZ(box5.clipZMovement(box, z2));
            }
            box = box.offset(0.0, 0.0, vector.getZ());
        }
        return box;
    }

    public static List findList(Entity entity, Location location, Vector vector) {
        EntityDimensions entityDimensions = EntityDimensions.create(entity);
        AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(location.toVector(), entityDimensions);
        AxisAlignedBoundingBox box2 = box.expandTowards(vector);
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        AxisAlignedBoundingBox box3 = box2;
        List values = BlockCollisionCollector.findList(entity, box3, serverVersion);
        findAxisAlignedBoundingBox(box, vector, values);
        return values;
    }
}
