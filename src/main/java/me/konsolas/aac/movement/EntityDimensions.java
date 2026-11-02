package me.konsolas.aac.movement;

import me.konsolas.aac.compat.NativeAccessProvider;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.entity.Entity;

public class EntityDimensions {

    public static final EntityDimensions DEFAULT = new EntityDimensions(0.6, 1.8);

    private final double width;

    private final double height;

    public EntityDimensions(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public static EntityDimensions create(Entity entity) {
        AxisAlignedBoundingBox boundingBox = NativeAccessProvider.getInstance().getBoundingBox(entity);
        return new EntityDimensions(Math.max(boundingBox.getMaxX() - boundingBox.getMinX(), boundingBox.getMaxZ() - boundingBox.getMinZ()), boundingBox.getMaxY() - boundingBox.getMinY());
    }

    public EntityDimensions withValue(double value) {
        return new EntityDimensions(this.width * value, this.height * value);
    }

    public EntityDimensions withValue(double value, double value2) {
        return new EntityDimensions(this.width + value, this.height + value2);
    }

    public EntityDimensions withEntityDimensions(EntityDimensions entityDimensions) {
        return new EntityDimensions(Math.max(this.width, entityDimensions.width), Math.max(this.height, entityDimensions.height));
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }
}
