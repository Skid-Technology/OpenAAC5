package me.konsolas.aac.util.math;

import java.util.Collection;
import java.util.Objects;
import me.konsolas.aac.movement.EntityDimensions;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class AxisAlignedBoundingBox {

    private final double minX;

    private final double minY;

    private final double minZ;

    private final double maxX;

    private final double maxY;

    private final double maxZ;

    public AxisAlignedBoundingBox(Vector vector, double value, double value2) {
        double value3 = value / 2.0;
        this.minX = vector.getX() - value3;
        this.minY = vector.getY();
        this.minZ = vector.getZ() - value3;
        this.maxX = vector.getX() + value3;
        this.maxY = vector.getY() + value2;
        this.maxZ = vector.getZ() + value3;
    }

    public AxisAlignedBoundingBox(Vector vector, EntityDimensions entityDimensions) {
        this(vector, entityDimensions.getWidth(), entityDimensions.getHeight());
    }

    public static AxisAlignedBoundingBox forBlock(int value, int value2, int value3) {
        return new AxisAlignedBoundingBox(value, value2, value3, value + 1, value2 + 1, value3 + 1);
    }

    public static AxisAlignedBoundingBox forChunk(int value, int value2) {
        int value3 = value << 4;
        int value4 = value2 << 4;
        return new AxisAlignedBoundingBox(value3, 0.0, value4, value3 + 16, 256.0, value4 + 16);
    }

    public static AxisAlignedBoundingBox forChunkSection(int value, int value2, int value3) {
        int value4 = value << 4;
        int value5 = value2 << 4;
        int value6 = value3 << 4;
        return new AxisAlignedBoundingBox(value4, value5, value6, value4 + 16, value5 + 16, value6 + 16);
    }

    public static AxisAlignedBoundingBox fromBukkit(BoundingBox box) {
        return new AxisAlignedBoundingBox(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    public AxisAlignedBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AxisAlignedBoundingBox clampY(double minimumY, double maximumY) {
        return new AxisAlignedBoundingBox(this.minX, Math.max(minimumY, this.minY), this.minZ, this.maxX, Math.min(maximumY, this.maxY), this.maxZ);
    }

    public static AxisAlignedBoundingBox enclosing(Vector vector, Vector vector2) {
        return new AxisAlignedBoundingBox(Math.min(vector.getX(), vector2.getX()), Math.min(vector.getY(), vector2.getY()), Math.min(vector.getZ(), vector2.getZ()), Math.max(vector.getX(), vector2.getX()), Math.max(vector.getY(), vector2.getY()), Math.max(vector.getZ(), vector2.getZ()));
    }

    public AxisAlignedBoundingBox expandTowards(double dx, double dy, double dz) {
        return new AxisAlignedBoundingBox(this.minX + (dx < 0.0 ? dx : 0.0), this.minY + (dy < 0.0 ? dy : 0.0), this.minZ + (dz < 0.0 ? dz : 0.0), this.maxX + (dx > 0.0 ? dx : 0.0), this.maxY + (dy > 0.0 ? dy : 0.0), this.maxZ + (dz > 0.0 ? dz : 0.0));
    }

    public AxisAlignedBoundingBox expandTowards(Vector vector) {
        double x = vector.getX();
        double z = vector.getZ();
        double y = vector.getY();
        double value = x;
        return this.expandTowards(value, y, z);
    }

    public double distanceTo(Vector vector) {
        Vector vector2 = new Vector(clamp(vector.getX(), this.minX, this.maxX), clamp(vector.getY(), this.minY, this.maxY), clamp(vector.getZ(), this.minZ, this.maxZ));
        return vector2.distance(vector);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.min(Math.max(value, minimum), maximum);
    }

    public AxisAlignedBoundingBox expand(Vector vector) {
        return this.expand(vector.getX(), vector.getY(), vector.getZ());
    }

    public AxisAlignedBoundingBox expand(double dx, double dy, double dz) {
        return new AxisAlignedBoundingBox(this.minX - dx, this.minY - dy, this.minZ - dz, this.maxX + dx, this.maxY + dy, this.maxZ + dz);
    }

    public AxisAlignedBoundingBox offset(Vector vector) {
        return this.offset(vector.getX(), vector.getY(), vector.getZ());
    }

    public AxisAlignedBoundingBox offset(double dx, double dy, double dz) {
        return new AxisAlignedBoundingBox(this.minX + dx, this.minY + dy, this.minZ + dz, this.maxX + dx, this.maxY + dy, this.maxZ + dz);
    }

    public Vector getBottomCenter() {
        return new Vector((this.minX + this.maxX) / 2.0, this.minY, (this.minZ + this.maxZ) / 2.0);
    }

    public AxisAlignedBoundingBox withMinX(double minX) {
        return new AxisAlignedBoundingBox(minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AxisAlignedBoundingBox withMinY(double minY) {
        return new AxisAlignedBoundingBox(this.minX, minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AxisAlignedBoundingBox withMinZ(double minZ) {
        return new AxisAlignedBoundingBox(this.minX, this.minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AxisAlignedBoundingBox withMaxX(double maxX) {
        return new AxisAlignedBoundingBox(this.minX, this.minY, this.minZ, maxX, this.maxY, this.maxZ);
    }

    public AxisAlignedBoundingBox withMaxY(double maxY) {
        return new AxisAlignedBoundingBox(this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    public AxisAlignedBoundingBox withMaxZ(double maxZ) {
        return new AxisAlignedBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, maxZ);
    }

    public double clipYMovement(AxisAlignedBoundingBox box, double value) {
        if (box.maxX > this.minX && box.minX < this.maxX && box.maxZ > this.minZ && box.minZ < this.maxZ) {
            if (value > 0.0 && box.maxY <= this.minY) {
                double value2 = this.minY - box.maxY;
                if (value2 < value) {
                    value = value2;
                }
            } else if (value < 0.0 && box.minY >= this.maxY) {
                double value3 = this.maxY - box.minY;
                if (value3 > value) {
                    value = value3;
                }
            }
            return value;
        } else {
            return value;
        }
    }

    public double clipXMovement(AxisAlignedBoundingBox box, double value) {
        if (box.maxY > this.minY && box.minY < this.maxY && box.maxZ > this.minZ && box.minZ < this.maxZ) {
            if (value > 0.0 && box.maxX <= this.minX) {
                double value2 = this.minX - box.maxX;
                if (value2 < value) {
                    value = value2;
                }
            } else if (value < 0.0 && box.minX >= this.maxX) {
                double value3 = this.maxX - box.minX;
                if (value3 > value) {
                    value = value3;
                }
            }
            return value;
        } else {
            return value;
        }
    }

    public double clipZMovement(AxisAlignedBoundingBox box, double value) {
        if (box.maxX > this.minX && box.minX < this.maxX && box.maxY > this.minY && box.minY < this.maxY) {
            if (value > 0.0 && box.maxZ <= this.minZ) {
                double value2 = this.minZ - box.maxZ;
                if (value2 < value) {
                    value = value2;
                }
            } else if (value < 0.0 && box.minZ >= this.maxZ) {
                double value3 = this.maxZ - box.minZ;
                if (value3 > value) {
                    value = value3;
                }
            }
            return value;
        } else {
            return value;
        }
    }

    public AxisAlignedBoundingBox resolvePenetration(Collection<AxisAlignedBoundingBox> values) {
        AxisAlignedBoundingBox box = this;
        for (AxisAlignedBoundingBox box2 : values) {
            if (box2.intersects(box)) {
                double value = box2.minX - box.maxX;
                double value2 = box2.maxX - box.minX;
                double value3;
                if (Math.abs(value) > Math.abs(value2)) {
                    value3 = value2;
                } else {
                    value3 = value;
                }
                value2 = box2.minY - box.maxY;
                double value4 = box2.maxY - box.minY;
                if (Math.abs(value2) > Math.abs(value4)) {
                    value = value4;
                } else {
                    value = value2;
                }
                value4 = box2.minZ - box.maxZ;
                double value5 = box2.maxZ - box.minZ;
                if (Math.abs(value4) > Math.abs(value5)) {
                    value2 = value5;
                } else {
                    value2 = value4;
                }
                boolean matches = Math.abs(value) < Math.abs(value2);
                boolean matches2 = Math.abs(value3) < Math.abs(value2);
                boolean matches3 = Math.abs(value3) < Math.abs(value);
                if (matches3 && matches2) {
                    box = box.offset(value3, 0.0, 0.0);
                } else if (!matches3 && matches) {
                    box = box.offset(0.0, value, 0.0);
                } else {
                    box = box.offset(0.0, 0.0, value2);
                }
            }
        }
        return box;
    }

    public boolean intersectsRay(Vector vector, Vector vector2) {
        double[] values = this.raySlabIntersections(vector, vector2);
        return Math.min(Math.min(Math.max(values[0], values[1]), Math.max(values[2], values[3])), Math.max(values[4], values[5])) >= 0.0 && Math.max(Math.max(Math.min(values[0], values[1]), Math.min(values[2], values[3])), Math.min(values[4], values[5])) <= Math.min(Math.min(Math.max(values[0], values[1]), Math.max(values[2], values[3])), Math.max(values[4], values[5]));
    }

    public double rayIntersectionDistance(Vector vector, Vector vector2) {
        double[] values = this.raySlabIntersections(vector, vector2);
        double value = Math.max(Math.max(Math.min(values[0], values[1]), Math.min(values[2], values[3])), Math.min(values[4], values[5]));
        double value2 = Math.min(Math.min(Math.max(values[0], values[1]), Math.max(values[2], values[3])), Math.max(values[4], values[5]));
        return !(value2 < 0.0) && !(value > value2) ? value : Double.POSITIVE_INFINITY;
    }

    private double[] raySlabIntersections(Vector vector, Vector vector2) {
        Vector vector3 = new Vector(1.0 / vector2.getX(), 1.0 / vector2.getY(), 1.0 / vector2.getZ());
        return new double[] { (this.minX - vector.getX()) * vector3.getX(), (this.maxX - vector.getX()) * vector3.getX(), (this.minY - vector.getY()) * vector3.getY(), (this.maxY - vector.getY()) * vector3.getY(), (this.minZ - vector.getZ()) * vector3.getZ(), (this.maxZ - vector.getZ()) * vector3.getZ() };
    }

    public AxisAlignedBoundingBox union(AxisAlignedBoundingBox box) {
        return box == null ? this : new AxisAlignedBoundingBox(Math.min(this.minX, box.minX), Math.min(this.minY, box.minY), Math.min(this.minZ, box.minZ), Math.max(this.maxX, box.maxX), Math.max(this.maxY, box.maxY), Math.max(this.maxZ, box.maxZ));
    }

    public AxisAlignedBoundingBox intersection(AxisAlignedBoundingBox box) {
        return box != null && this.intersects(box) ? new AxisAlignedBoundingBox(Math.max(this.minX, box.minX), Math.max(this.minY, box.minY), Math.max(this.minZ, box.minZ), Math.min(this.maxX, box.maxX), Math.min(this.maxY, box.maxY), Math.min(this.maxZ, box.maxZ)) : null;
    }

    public AxisAlignedBoundingBox alignToBlocks() {
        return new AxisAlignedBoundingBox(NumberConversions.floor(this.minX), NumberConversions.floor(this.minY), NumberConversions.floor(this.minZ), NumberConversions.ceil(this.maxX), NumberConversions.ceil(this.maxY), NumberConversions.ceil(this.maxZ));
    }

    public boolean isClearOf(Collection<AxisAlignedBoundingBox> values) {
        for (AxisAlignedBoundingBox box : values) {
            if (this.intersects(box)) {
                return false;
            }
        }
        return true;
    }

    public boolean intersects(AxisAlignedBoundingBox box) {
        return this.minX < box.maxX && this.maxX > box.minX && this.minY < box.maxY && this.maxY > box.minY && this.minZ < box.maxZ && this.maxZ > box.minZ;
    }

    public boolean intersectsOffset(AxisAlignedBoundingBox box, double value, double value2, double value3) {
        return this.minX < box.maxX + value && this.maxX > box.minX + value && this.minY < box.maxY + value2 && this.maxY > box.minY + value2 && this.minZ < box.maxZ + value3 && this.maxZ > box.minZ + value3;
    }

    public boolean contains(Vector vector) {
        return this.minX < vector.getX() && this.maxX > vector.getX() && this.minY < vector.getY() && this.maxY > vector.getY() && this.minZ < vector.getZ() && this.maxZ > vector.getZ();
    }

    public boolean containsInclusive(Vector vector) {
        return this.minX <= vector.getX() && this.maxX >= vector.getX() && this.minY <= vector.getY() && this.maxY >= vector.getY() && this.minZ <= vector.getZ() && this.maxZ >= vector.getZ();
    }

    public boolean contains(AxisAlignedBoundingBox box) {
        return this.minX <= box.minX && this.maxX >= box.maxX && this.minY <= box.minY && this.maxY >= box.maxY && this.minZ <= box.minZ && this.maxZ >= box.maxZ;
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getMinZ() {
        return this.minZ;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public double getMaxZ() {
        return this.maxZ;
    }

    public boolean isEmpty() {
        return this.minX == this.maxX || this.minY == this.maxY || this.minZ == this.maxZ;
    }

    @Override
    public boolean equals(Object result) {
        if (this == result) {
            return true;
        } else if (result != null && this.getClass() == result.getClass()) {
            AxisAlignedBoundingBox box = (AxisAlignedBoundingBox) result;
            return Double.compare(box.minX, this.minX) == 0 && Double.compare(box.minY, this.minY) == 0 && Double.compare(box.minZ, this.minZ) == 0 && Double.compare(box.maxX, this.maxX) == 0 && Double.compare(box.maxY, this.maxY) == 0 && Double.compare(box.maxZ, this.maxZ) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public String toString() {
        return "AABB{minX=" + this.minX + ", minY=" + this.minY + ", minZ=" + this.minZ + ", maxX=" + this.maxX + ", maxY=" + this.maxY + ", maxZ=" + this.maxZ + '}';
    }
}
