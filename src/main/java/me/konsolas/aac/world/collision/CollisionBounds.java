package me.konsolas.aac.world.collision;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import me.konsolas.aac.movement.EntityView;
import me.konsolas.aac.movement.MovementDelta;
import me.konsolas.aac.movement.MovementEnvironment;
import me.konsolas.aac.movement.MovementFrame;
import me.konsolas.aac.movement.PlayerInputState;
import me.konsolas.aac.movement.PlayerPose;
import me.konsolas.aac.movement.RotationDelta;
import me.konsolas.aac.movement.TimedVelocity;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.util.math.MinecraftMath;
import me.konsolas.aac.world.MaterialAlias;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class CollisionBounds {

    private static final Vector offset = new Vector(0, 0, 0);

    private final ProtocolVersion protocolVersion;

    private final AxisAlignedBoundingBox boundingBox;

    public CollisionBounds(ProtocolVersion protocolVersion, AxisAlignedBoundingBox boundingBox) {
        this.protocolVersion = protocolVersion;
        this.boundingBox = boundingBox;
    }

    private Collection findCollection(MovementEnvironment movementEnvironment, AxisAlignedBoundingBox box) {
        Collection collisionBoxes = movementEnvironment.getCollisionBoxes(box);
        if (this.boundingBox != null) {
            AxisAlignedBoundingBox box2 = this.boundingBox;
            if (box.intersects(box2)) {
                collisionBoxes.add(this.boundingBox);
            }
        }
        return collisionBoxes;
    }

    private boolean checkCollisionBounds(MovementEnvironment movementEnvironment, AxisAlignedBoundingBox box, double value, double value2, double value3) {
        return movementEnvironment.checkMovementEnvironment(box, value, value2, value3) && (this.boundingBox == null || !this.boundingBox.intersectsOffset(box, value, value2, value3));
    }

    public MovementFrame findMovementFrame(Vector vector, EntityView entityView, MovementEnvironment movementEnvironment, MovementFrame frame, PlayerInputState playerInputState, TimedVelocity timedVelocity) {
        double y = this.getY(entityView, frame);
        boolean horizontalCollision = frame.getMovementDelta().isHorizontalCollision();
        RotationDelta rotationDelta = this.findRotationDelta(entityView, horizontalCollision, playerInputState);
        Vector vector2 = frame.getMovementDelta().isOnGround() && timedVelocity == null ? offset : this.findVector(entityView, movementEnvironment, frame, playerInputState, timedVelocity, y);
        Vector vector3 = vector2.clone();
        Vector vector4 = this.findVector(entityView, movementEnvironment, frame, playerInputState, rotationDelta, vector3, y);
        boolean horizontalCollision2 = frame.getMovementDelta().isHorizontalCollision();
        boolean sneaking = playerInputState.isSneaking();
        boolean matches = horizontalCollision2;
        MovementDelta movementDelta = this.findMovementDelta(entityView, movementEnvironment, vector, vector4, matches, sneaking);
        return new MovementFrame(playerInputState, timedVelocity, vector2, rotationDelta, movementDelta);
    }

    public double getY(EntityView entityView, MovementFrame frame) {
        ProtocolVersion version = ProtocolVersion.V1_13;
        return this.protocolVersion.isAtLeast(version) && entityView.isSlowFalling() && frame.getMovementDelta().getActualMovement().getY() <= 0.0 ? 0.01 : 0.08;
    }

    public MovementFrame findMovementFrame(EntityView entityView, Location location, boolean matches) {
        PlayerInputState playerInputState = new PlayerInputState(0.0F, 0.0F, false, false, false, false, false, false, location.getPitch(), location.getYaw(), 0);
        PlayerInputState playerInputState2 = playerInputState;
        RotationDelta rotationDelta = this.findRotationDelta(entityView, true, playerInputState2);
        MovementDelta movementDelta = new MovementDelta(offset, offset, matches, false, true);
        return new MovementFrame(PlayerInputState.EMPTY, null, offset, rotationDelta, movementDelta);
    }

    private double calculateValue(EntityView entityView) {
        double jumpStrength = entityView.getJumpStrength();
        if (entityView.isOnHoney()) {
            jumpStrength *= 0.5;
        }
        if (entityView.hasJumpBoost()) {
            jumpStrength += (entityView.getJumpBoostAmplifier() + 1) * 0.1;
        }
        return jumpStrength;
    }

    private static Vector findVector(ProtocolVersion version, float value, float value2) {
        ProtocolVersion version2 = ProtocolVersion.V1_13;
        if (version.isAtLeast(version2)) {
            float value3 = value * (float) (Math.PI / 180.0);
            float value4 = -value2 * (float) (Math.PI / 180.0);
            float value5 = MinecraftMath.cos(value4);
            float value6 = MinecraftMath.sin(value4);
            float value7 = MinecraftMath.cos(value3);
            float value8 = MinecraftMath.sin(value3);
            return new Vector(value6 * value7, -value8, value5 * value7);
        } else {
            float value9 = MinecraftMath.cos(-value2 * (float) (Math.PI / 180.0) - (float) Math.PI);
            float value10 = MinecraftMath.sin(-value2 * (float) (Math.PI / 180.0) - (float) Math.PI);
            float value11 = -MinecraftMath.cos(-value * (float) (Math.PI / 180.0));
            float value12 = MinecraftMath.sin(-value * (float) (Math.PI / 180.0));
            return new Vector(value10 * value11, value12, value9 * value11);
        }
    }

    private Vector findVector(EntityView entityView, MovementEnvironment movementEnvironment, MovementFrame frame, PlayerInputState playerInputState, RotationDelta rotationDelta, Vector vector, double value) {
        if (entityView.getEntityType() != EntityType.PLAYER) {
            vector.multiply(0.98);
        }
        for (int index = 0; index < playerInputState.getAttackedEntityId(); index++) {
            vector.setX(vector.getX() * 0.6);
            vector.setZ(vector.getZ() * 0.6);
        }
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (this.protocolVersion.isAtLeast(version) && entityView.isInWater() && playerInputState.isSneaking() && !playerInputState.isFlying()) {
            vector.setY(vector.getY() - 0.04F);
        }
        if (playerInputState.isFlying()) {
            int value2 = 0;
            if (playerInputState.isSneaking()) {
                value2--;
            }
            if (playerInputState.isJumping()) {
                value2++;
            }
            if (value2 != 0) {
                vector.setY(vector.getY() + value2 * entityView.getFlySpeed() * 3.0F);
            }
        }
        ProtocolVersion version2 = ProtocolVersion.V1_9;
        if (this.protocolVersion.isAtLeast(version2)) {
            if (Math.abs(vector.getX()) < 0.003) {
                vector.setX(0);
            }
            if (Math.abs(vector.getY()) < 0.003) {
                vector.setY(0);
            }
            if (Math.abs(vector.getZ()) < 0.003) {
                vector.setZ(0);
            }
        } else {
            if (Math.abs(vector.getX()) < 0.005) {
                vector.setX(0);
            }
            if (Math.abs(vector.getY()) < 0.005) {
                vector.setY(0);
            }
            if (Math.abs(vector.getZ()) < 0.005) {
                vector.setZ(0);
            }
        }
        float value3 = playerInputState.getForward() * 0.98F;
        float value4 = playerInputState.getStrafe() * 0.98F;
        if (entityView.getEntityType() != EntityType.PLAYER) {
            value4 *= 0.5F;
            if (value3 < 0.0F) {
                value3 *= 0.25F;
            }
        }
        if (playerInputState.isUsingItem()) {
            value3 *= 0.2F;
            value4 *= 0.2F;
        }
        boolean sneaking = playerInputState.isSneaking();
        if (entityView.isInWater()) {
            ProtocolVersion version3 = ProtocolVersion.V1_15;
            if (this.protocolVersion.isAtLeast(version3) && playerInputState.isSprinting()) {
                sneaking = false;
            }
        } else {
            ProtocolVersion version4 = ProtocolVersion.V1_13;
            if (this.protocolVersion.isAtLeast(version4) && entityView.getPose() == PlayerPose.SWIMMING) {
                sneaking = true;
            }
        }
        if (sneaking && !playerInputState.isFlying()) {
            value3 = (float) (value3 * 0.3);
            value4 = (float) (value4 * 0.3);
        }
        if (playerInputState.isJumping() && !playerInputState.isFlying()) {
            ProtocolVersion version5 = ProtocolVersion.V1_13;
            if (this.protocolVersion.isAtLeast(version5)) {
                double waterDepth = entityView.getWaterDepth();
                if (!(waterDepth > 0.0) || frame.getMovementDelta().isHorizontalCollision() && waterDepth <= 0.4) {
                    if (entityView.isInLava()) {
                        vector.setY(vector.getY() + 0.04F);
                    } else if (frame.getMovementDelta().isHorizontalCollision() || waterDepth > 0.0 && waterDepth <= 0.4) {
                        this.updateEntityView(entityView, playerInputState, vector);
                    }
                } else {
                    vector.setY(vector.getY() + 0.04F);
                }
            } else if (entityView.isInWater() || entityView.isInLava()) {
                vector.setY(vector.getY() + 0.04F);
            } else if (frame.getMovementDelta().isHorizontalCollision()) {
                this.updateEntityView(entityView, playerInputState, vector);
            }
        }
        ProtocolVersion version6 = ProtocolVersion.V1_13;
        if (this.protocolVersion.isAtLeast(version6) && entityView.isInWater() && playerInputState.isSprinting() && !playerInputState.isFlying()) {
            double value5 = -MinecraftMath.sin((float) Math.toRadians(playerInputState.getYaw()));
            double value6 = value5 < -0.2 ? 0.085 : 0.06;
            if (value5 <= 0.0 || playerInputState.isJumping() || entityView.isHeadUnderwater()) {
                vector.setY(vector.getY() + (value5 - vector.getY()) * value6);
            }
        }
        if (playerInputState.isElytra() && !entityView.isInWater() && !entityView.isInLava()) {
            ProtocolVersion version7 = ProtocolVersion.V1_13;
            if (this.protocolVersion.isAtLeast(version7)) {
                ProtocolVersion version8 = this.protocolVersion;
                float yaw = playerInputState.getYaw();
                float pitch = playerInputState.getPitch();
                float value7 = yaw;
                ProtocolVersion version9 = version8;
                Vector vector2 = findVector(version9, value7, pitch);
                float value8 = playerInputState.getYaw() * (float) (Math.PI / 180.0);
                double value9 = Math.sqrt(vector2.getX() * vector2.getX() + vector2.getZ() * vector2.getZ());
                double value10 = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
                double size = vector2.length();
                float value11 = MinecraftMath.cos(value8);
                value11 = (float) ((double) value11 * value11 * Math.min(1.0, size / 0.4));
                vector.setY(vector.getY() + value * (-1.0 + value11 * 0.75));
                if (vector.getY() < 0.0 && value9 > 0.0) {
                    double value12 = vector.getY() * -0.1 * value11;
                    vector.setY(vector.getY() + value12);
                    vector.setX(vector.getX() + vector2.getX() * value12 / value9);
                    vector.setZ(vector.getZ() + vector2.getZ() * value12 / value9);
                }
                if (value8 < 0.0F && value9 > 0.0) {
                    double value13 = value10 * -MinecraftMath.sin(value8) * 0.04;
                    vector.setY(vector.getY() + value13 * 3.2);
                    vector.setX(vector.getX() - vector2.getX() * value13 / value9);
                    vector.setZ(vector.getZ() - vector2.getZ() * value13 / value9);
                }
                if (value9 > 0.0) {
                    vector.setX(vector.getX() + (vector2.getX() / value9 * value10 - vector.getX()) * 0.1);
                    vector.setZ(vector.getZ() + (vector2.getZ() / value9 * value10 - vector.getZ()) * 0.1);
                }
                vector.setY(vector.getY() * 0.98F);
                vector.setX(vector.getX() * 0.99F);
                vector.setZ(vector.getZ() * 0.99F);
            } else {
                ProtocolVersion version10 = this.protocolVersion;
                float yaw2 = playerInputState.getYaw();
                float pitch2 = playerInputState.getPitch();
                float value14 = yaw2;
                ProtocolVersion version11 = version10;
                Vector vector3 = findVector(version11, value14, pitch2);
                float value15 = playerInputState.getYaw() * (float) (Math.PI / 180.0);
                double value16 = Math.sqrt(vector3.getX() * vector3.getX() + vector3.getZ() * vector3.getZ());
                double value17 = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
                double size2 = vector3.length();
                float value18 = MinecraftMath.cos(value15);
                value18 = (float) ((double) value18 * value18 * Math.min(1.0, size2 / 0.4));
                vector.setY(vector.getY() - 0.08 + value18 * 0.06);
                if (vector.getY() < 0.0 && value16 > 0.0) {
                    double value19 = vector.getY() * -0.1 * value18;
                    vector.setY(vector.getY() + value19);
                    vector.setX(vector.getX() + vector3.getX() * value19 / value16);
                    vector.setZ(vector.getZ() + vector3.getZ() * value19 / value16);
                }
                if (value15 < 0.0F) {
                    double value20 = value17 * -MinecraftMath.sin(value15) * 0.04;
                    vector.setY(vector.getY() + value20 * 3.2);
                    vector.setX(vector.getX() - vector3.getX() * value20 / value16);
                    vector.setZ(vector.getZ() - vector3.getZ() * value20 / value16);
                }
                if (value16 > 0.0) {
                    vector.setX(vector.getX() + (vector3.getX() / value16 * value17 - vector.getX()) * 0.1);
                    vector.setZ(vector.getZ() + (vector3.getZ() / value16 * value17 - vector.getZ()) * 0.1);
                }
                vector.setY(vector.getY() * 0.98F);
                vector.setX(vector.getX() * 0.99F);
                vector.setZ(vector.getZ() * 0.99F);
            }
        } else {
            float yaw3 = rotationDelta.getYaw();
            float pitch3 = playerInputState.getPitch();
            float value21 = yaw3;
            vector.add(findVector(value4, value3, value21, pitch3));
        }
        if (!entityView.isInWater() && !entityView.isInLava() && !playerInputState.isElytra() && entityView.isClimbing()) {
            float value22 = 0.15F;
            double x = vector.getX();
            double value24 = -value22;
            double value25 = value22;
            double value26 = value24;
            double value27 = x;
            vector.setX(MinecraftMath.clamp(value27, value26, value25));
            x = vector.getZ();
            value24 = -value22;
            double value28 = value22;
            double value29 = value24;
            double value30 = x;
            vector.setZ(MinecraftMath.clamp(value30, value29, value28));
            if (vector.getY() < -0.15) {
                vector.setY(-0.15);
            }
            if (playerInputState.isSneaking() && vector.getY() < 0.0) {
                vector.setY(0.0);
            }
        }
        return vector;
    }

    public Vector findVector(EntityView entityView, MovementEnvironment movementEnvironment, MovementFrame frame, PlayerInputState playerInputState, TimedVelocity timedVelocity, double value) {
        Vector vector;
        if (timedVelocity == null) {
            vector = frame.getMovementDelta().getActualMovement().clone();
            double y = vector.getY();
            if (entityView.isOnHoney()) {
                vector.setX(vector.getX() * 0.4);
                vector.setZ(vector.getZ() * 0.4);
            }
            if (entityView.getSoulSandBlockCount() > 0) {
                double value2 = Math.pow(0.4, entityView.getSoulSandBlockCount());
                vector.setX(vector.getX() * value2);
                vector.setZ(vector.getZ() * value2);
            }
            if (entityView.isTouchingHoney() && !frame.getMovementDelta().isHorizontalCollision() && frame.getMovementDelta().getActualMovement().getY() < -0.08) {
                if (frame.getMovementDelta().getActualMovement().getY() < -0.13) {
                    double value3 = -0.05 / frame.getMovementDelta().getActualMovement().getY();
                    vector.setX(vector.getX() * value3);
                    vector.setZ(vector.getZ() * value3);
                }
                vector.setY(-0.05);
            }
            if (entityView.isPreviousInWater() && !frame.getInput().isFlying()) {
                vector.setX(vector.getX() * frame.getRotationDelta().getPitch());
                vector.setY(vector.getY() * 0.8);
                vector.setZ(vector.getZ() * frame.getRotationDelta().getPitch());
                ProtocolVersion version = ProtocolVersion.V1_13;
                if (this.protocolVersion.isAtLeast(version)) {
                    boolean matches = frame.getVector().getY() <= 0.0;
                    this.updatePlayerInputState(playerInputState, value, vector);
                } else {
                    vector.setY(vector.getY() - 0.02);
                }
                if (frame.getMovementDelta().isVerticalCollision()) {
                    AxisAlignedBoundingBox box = entityView.getBoundingBox().offset(vector.getX(), vector.getY() + 0.6 - frame.getMovementDelta().getActualMovement().getY(), vector.getZ());
                    Predicate predicate = MaterialAlias.WATER::contains;
                    Predicate predicate2 = predicate;
                    AxisAlignedBoundingBox box2 = box;
                    if (!movementEnvironment.checkMovementEnvironment(box2, predicate2)) {
                        vector.setY(0.3F);
                    }
                }
            } else if (entityView.isPreviousInLava() && !frame.getInput().isFlying()) {
                ProtocolVersion version2 = ProtocolVersion.V1_16;
                if (this.protocolVersion.isAtLeast(version2) && entityView.getLavaDepth() <= 0.4) {
                    vector.setX(vector.getX() * 0.5);
                    vector.setY(vector.getY() * 0.8F);
                    vector.setZ(vector.getZ() * 0.5);
                    boolean matches2 = frame.getVector().getY() <= 0.0;
                    this.updatePlayerInputState(playerInputState, value, vector);
                } else {
                    vector.multiply(0.5);
                }
                ProtocolVersion version3 = ProtocolVersion.V1_13;
                if (this.protocolVersion.isAtLeast(version3)) {
                    vector.setY(vector.getY() - value / 4.0);
                } else {
                    vector.setY(vector.getY() - 0.02);
                }
                if (frame.getMovementDelta().isVerticalCollision()) {
                    AxisAlignedBoundingBox box3 = entityView.getBoundingBox().offset(vector.getX(), vector.getY() + 0.6 - frame.getMovementDelta().getActualMovement().getY(), vector.getZ());
                    Predicate predicate3 = MaterialAlias.LAVA::contains;
                    Predicate predicate4 = predicate3;
                    AxisAlignedBoundingBox box4 = box3;
                    if (!movementEnvironment.checkMovementEnvironment(box4, predicate4)) {
                        vector.setY(0.3F);
                    }
                }
            } else if (!frame.getInput().isElytra()) {
                ProtocolVersion version4 = ProtocolVersion.V1_14;
                if (this.protocolVersion.isAtLeast(version4)) {
                    if ((frame.getMovementDelta().isVerticalCollision() || playerInputState.isJumping()) && entityView.isClimbing()) {
                        vector.setY(0.2);
                    }
                } else if (frame.getMovementDelta().isVerticalCollision() && entityView.isClimbing()) {
                    vector.setY(0.2);
                }
                ProtocolVersion version5 = ProtocolVersion.V1_9;
                if (this.protocolVersion.isAtLeast(version5) && entityView.hasLevitation()) {
                    vector.setY(vector.getY() + (0.05 * (entityView.getLevitationAmplifier() + 1) - vector.getY()) * 0.2);
                } else {
                    vector.setY(vector.getY() - value);
                }
                vector.setY(vector.getY() * 0.98F);
                vector.setX(vector.getX() * frame.getRotationDelta().getPitch());
                vector.setZ(vector.getZ() * frame.getRotationDelta().getPitch());
            }
            if (frame.getInput().isFlying()) {
                vector.setY(y * 0.6);
            }
        } else {
            vector = timedVelocity.getVector();
        }
        return vector;
    }

    private void updatePlayerInputState(PlayerInputState playerInputState, double value, Vector vector) {
        if (!playerInputState.isSprinting()) {
            if (vector.getY() <= 0.0 && Math.abs(vector.getY() - 0.005) >= 0.003 && Math.abs(vector.getY() - value / 16.0) < 0.003) {
                vector.setY(-0.003);
            } else {
                vector.setY(vector.getY() - value / 16.0);
            }
        }
    }

    private void updateEntityView(EntityView entityView, PlayerInputState playerInputState, Vector vector) {
        float value = entityView.getEntityType() == EntityType.PLAYER ? 0.2F : 0.4F;
        float value2 = playerInputState.getPitch() * (float) (Math.PI / 180.0);
        Vector vector2 = playerInputState.isSprinting() ? new Vector(-MinecraftMath.sin(value2) * value, 0.0F, MinecraftMath.cos(value2) * value) : new Vector(0, 0, 0);
        double value3 = this.calculateValue(entityView);
        vector.add(vector2);
        vector.setY(value3);
    }

    private RotationDelta findRotationDelta(EntityView entityView, boolean matches, PlayerInputState playerInputState) {
        float walkSpeed = entityView.getWalkSpeed();
        if (playerInputState.isSprinting()) {
            walkSpeed = (float) (walkSpeed * 1.300000011920929);
        }
        float value;
        float value2;
        if (playerInputState.isFlying() && entityView.getEntityType() == EntityType.PLAYER) {
            value2 = entityView.getFlySpeed();
            if (playerInputState.isSprinting()) {
                value2 *= 2.0F;
            }
            value = 0.91F;
        } else if (entityView.isInWater()) {
            ProtocolVersion version = ProtocolVersion.V1_13;
            float value3;
            if (this.protocolVersion.isAtLeast(version) && playerInputState.isSprinting()) {
                value3 = 0.9F;
            } else {
                ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version2 = ProtocolVersion.V1_11;
                value3 = serverVersion.isAtLeast(version2) && entityView.getEntityType() == EntityType.SKELETON_HORSE ? 0.96F : 0.8F;
            }
            value = value3;
            value2 = 0.02F;
            float depthStriderLevel = entityView.getDepthStriderLevel();
            if (depthStriderLevel > 3.0F) {
                depthStriderLevel = 3.0F;
            }
            if (!matches) {
                depthStriderLevel *= 0.5F;
            }
            if (depthStriderLevel > 0.0F) {
                value = (float) (value + (0.546 - value) * depthStriderLevel / 3.0);
                value2 += (walkSpeed - value2) * depthStriderLevel / 3.0F;
            }
            ProtocolVersion version3 = ProtocolVersion.V1_13;
            if (this.protocolVersion.isAtLeast(version3) && entityView.isDolphinsGrace()) {
                value = 0.96F;
            }
        } else if (entityView.isInLava()) {
            value2 = 0.02F;
            value = 0.5F;
        } else {
            value = 0.91F;
            if (matches) {
                value = entityView.getBlockFriction() * 0.91F;
            }
            float value4 = 0.16277136F / (value * value * value);
            if (matches) {
                value2 = walkSpeed * value4;
            } else if (entityView.getEntityType() == EntityType.PLAYER) {
                value2 = 0.02F;
                if (playerInputState.isSprinting()) {
                    value2 += 0.006F;
                }
            } else {
                value2 = walkSpeed * 0.1F;
            }
        }
        return new RotationDelta(value2, value);
    }

    private static Vector findVector(float value, float value2, float value3, float value4) {
        float value5 = value * value + value2 * value2;
        if (value5 >= 1.0E-4F) {
            value5 = MinecraftMath.sqrt(value5);
            if (value5 < 1.0F) {
                value5 = 1.0F;
            }
            value5 = value3 / value5;
            value *= value5;
            value2 *= value5;
            float value6 = MinecraftMath.sin(value4 * (float) Math.PI / 180.0F);
            float value7 = MinecraftMath.cos(value4 * (float) Math.PI / 180.0F);
            return new Vector(value * value7 - value2 * value6, 0.0F, value2 * value7 + value * value6);
        } else {
            return new Vector(0, 0, 0);
        }
    }

    private MovementDelta findMovementDelta(EntityView entityView, MovementEnvironment movementEnvironment, Vector vector, Vector vector2, boolean matches, boolean matches2) {
        Vector vector3 = vector2.clone();
        if (entityView.isInCobweb()) {
            vector3.setX(vector2.getX() * 0.25);
            vector3.setY(vector2.getY() * 0.05F);
            vector3.setZ(vector2.getZ() * 0.25);
        } else if (entityView.isInSweetBerryBush()) {
            vector3.setX(vector2.getX() * 0.8F);
            vector3.setY(vector2.getY() * 0.75);
            vector3.setZ(vector2.getZ() * 0.8F);
        }
        Vector vector4 = vector3.clone();
        AxisAlignedBoundingBox box = entityView.getBoundingBox().expand(-1.0E-7, -1.0E-7, -1.0E-7);
        AxisAlignedBoundingBox box2 = box.expandTowards(vector2);
        Collection values = this.findCollection(movementEnvironment, box2);
        if (matches && matches2) {
            ProtocolVersion version = ProtocolVersion.V1_9;
            double value;
            for (value = this.protocolVersion.isAtLeast(version) ? -entityView.getStepHeight() : -1.0; vector3.getX() != 0.0; vector4.setX(vector3.getX())) {
                AxisAlignedBoundingBox boundingBox = entityView.getBoundingBox();
                double x = vector3.getX();
                double value2 = x;
                AxisAlignedBoundingBox box3 = boundingBox;
                if (!this.checkCollisionBounds(movementEnvironment, box3, value2, value, (double) 0.0)) {
                    break;
                }
                if (vector3.getX() < 0.05 && vector3.getX() >= -0.05) {
                    vector3.setX(0.0);
                } else if (vector3.getX() > 0.0) {
                    vector3.setX(vector3.getX() - 0.05);
                } else {
                    vector3.setX(vector3.getX() + 0.05);
                }
            }
            for (; vector3.getZ() != 0.0; vector4.setZ(vector3.getZ())) {
                AxisAlignedBoundingBox boundingBox2 = entityView.getBoundingBox();
                double z = vector3.getZ();
                AxisAlignedBoundingBox box4 = boundingBox2;
                if (!this.checkCollisionBounds(movementEnvironment, box4, (double) 0.0, value, z)) {
                    break;
                }
                if (vector3.getZ() < 0.05 && vector3.getZ() >= -0.05) {
                    vector3.setZ(0.0);
                } else if (vector3.getZ() > 0.0) {
                    vector3.setZ(vector3.getZ() - 0.05);
                } else {
                    vector3.setZ(vector3.getZ() + 0.05);
                }
            }
            for (; vector3.getX() != 0.0 && vector3.getZ() != 0.0; vector4.setZ(vector3.getZ())) {
                AxisAlignedBoundingBox boundingBox3 = entityView.getBoundingBox();
                double z2 = vector3.getZ();
                double x2 = vector3.getX();
                AxisAlignedBoundingBox box5 = boundingBox3;
                if (!this.checkCollisionBounds(movementEnvironment, box5, x2, value, z2)) {
                    break;
                }
                if (vector3.getX() < 0.05 && vector3.getX() >= -0.05) {
                    vector3.setX(0.0);
                } else if (vector3.getX() > 0.0) {
                    vector3.setX(vector3.getX() - 0.05);
                } else {
                    vector3.setX(vector3.getX() + 0.05);
                }
                vector4.setX(vector3.getX());
                if (vector3.getZ() < 0.05 && vector3.getZ() >= -0.05) {
                    vector3.setZ(0.0);
                } else if (vector3.getZ() > 0.0) {
                    vector3.setZ(vector3.getZ() - 0.05);
                } else {
                    vector3.setZ(vector3.getZ() + 0.05);
                }
            }
        }
        box = this.findAxisAlignedBoundingBox(vector3, box, values);
        boolean matches3 = matches || vector3.getY() != vector4.getY() && vector4.getY() < 0.0;
        if (matches3 && (vector3.getX() != vector4.getX() || vector3.getZ() != vector4.getZ())) {
            ProtocolVersion version2 = ProtocolVersion.V1_14;
            if (this.protocolVersion.isAtLeast(version2)) {
                Vector vector5 = new Vector(vector4.getX(), entityView.getStepHeight(), vector4.getZ());
                Vector vector6 = new Vector(0.0, entityView.getStepHeight(), 0.0);
                this.findAxisAlignedBoundingBox(vector5, box, values);
                Vector vector7 = new Vector(vector4.getX(), 0.0, vector4.getZ());
                AxisAlignedBoundingBox box6 = box.expandTowards(vector7);
                this.findAxisAlignedBoundingBox(vector6, box6, values);
                if (vector6.getY() < entityView.getStepHeight()) {
                    Vector vector8 = new Vector(vector4.getX(), 0.0, vector4.getZ());
                    AxisAlignedBoundingBox box7 = box.offset(vector6);
                    this.findAxisAlignedBoundingBox(vector8, box7, values);
                    if (vector8.getX() * vector8.getX() + vector8.getZ() * vector8.getZ() > vector5.getX() * vector5.getX() + vector5.getZ() * vector5.getZ()) {
                        vector5 = vector8;
                    }
                }
                if (vector5.getX() * vector5.getX() + vector5.getZ() * vector5.getZ() > vector3.getX() * vector3.getX() + vector3.getZ() * vector3.getZ()) {
                    Vector vector9 = new Vector(0.0, -vector5.getY() + vector4.getY(), 0.0);
                    AxisAlignedBoundingBox box8 = box.offset(vector5);
                    box = this.findAxisAlignedBoundingBox(vector9, box8, values);
                    vector5.add(vector9);
                    vector3 = vector5;
                }
            } else {
                double x3 = vector3.getX();
                double y = vector3.getY();
                double z3 = vector3.getZ();
                AxisAlignedBoundingBox box9 = box;
                vector3.setY(entityView.getStepHeight());
                Vector vector10 = new Vector(vector4.getX(), vector3.getY(), vector4.getZ());
                AxisAlignedBoundingBox box10 = box.expandTowards(vector10);
                Collection<AxisAlignedBoundingBox> values2 = this.findCollection(movementEnvironment, box10);
                vector10 = new Vector(vector4.getX(), 0.0, vector4.getZ());
                AxisAlignedBoundingBox box11 = box.expandTowards(vector10);
                double y2 = vector3.getY();
                for (AxisAlignedBoundingBox box12 : values2) {
                    y2 = box12.clipYMovement(box11, y2);
                }
                AxisAlignedBoundingBox box13 = box.offset(0.0, y2, 0.0);
                double x4 = vector4.getX();
                for (AxisAlignedBoundingBox box14 : values2) {
                    x4 = box14.clipXMovement(box13, x4);
                }
                box13 = box13.offset(x4, 0.0, 0.0);
                double z4 = vector4.getZ();
                for (AxisAlignedBoundingBox box15 : values2) {
                    z4 = box15.clipZMovement(box13, z4);
                }
                box13 = box13.offset(0.0, 0.0, z4);
                AxisAlignedBoundingBox box16 = box;
                double y3 = vector3.getY();
                for (AxisAlignedBoundingBox box17 : values2) {
                    y3 = box17.clipYMovement(box16, y3);
                }
                box16 = box16.offset(0.0, y3, 0.0);
                double x5 = vector4.getX();
                for (AxisAlignedBoundingBox box18 : values2) {
                    x5 = box18.clipXMovement(box16, x5);
                }
                box16 = box16.offset(x5, 0.0, 0.0);
                double z5 = vector4.getZ();
                for (AxisAlignedBoundingBox box19 : values2) {
                    z5 = box19.clipZMovement(box16, z5);
                }
                box16 = box16.offset(0.0, 0.0, z5);
                double value3 = x4 * x4 + z4 * z4;
                double value4 = x5 * x5 + z5 * z5;
                if (value3 > value4) {
                    vector3.setX(x4);
                    vector3.setZ(z4);
                    vector3.setY(-y2);
                    box = box13;
                } else {
                    vector3.setX(x5);
                    vector3.setZ(z5);
                    vector3.setY(-y3);
                    box = box16;
                }
                for (AxisAlignedBoundingBox box20 : values2) {
                    double y4 = vector3.getY();
                    vector3.setY(box20.clipYMovement(box, y4));
                }
                box = box.offset(0.0, vector3.getY(), 0.0);
                if (x3 * x3 + z3 * z3 >= vector3.getX() * vector3.getX() + vector3.getZ() * vector3.getZ()) {
                    vector3.setX(x3);
                    vector3.setY(y);
                    vector3.setZ(z3);
                    box = box9;
                }
            }
        }
        boolean matches4 = vector4.getX() != vector3.getX() || vector4.getZ() != vector3.getZ();
        boolean matches5 = vector4.getY() != vector3.getY();
        boolean matches6 = matches5 && vector4.getY() < 0.0;
        if (!entityView.isInCobweb() && !entityView.isInSweetBerryBush()) {
            if (matches5) {
                double value5 = vector.getX() + vector3.getX();
                double value6 = vector.getY() + vector3.getY() - 0.2;
                double value7 = vector.getZ() + vector3.getZ();
                double value8 = value7;
                double value9 = value6;
                double value10 = value5;
                if (movementEnvironment.getMaterial(value10, value9, value8) == Material.SLIME_BLOCK && !matches2 && vector2.getY() < 0.0) {
                    vector2.setY(-vector2.getY());
                    if (Math.abs(vector2.getY()) < 0.1) {
                        double value11 = 0.4 + Math.abs(vector2.getY()) * 0.2;
                        vector2.setX(value11 * vector2.getX());
                        vector2.setZ(value11 * vector2.getZ());
                    }
                    return new MovementDelta(box.getBottomCenter().subtract(vector), vector2, matches6, matches4, false);
                }
            }
            if (matches5) {
                Set values3 = MaterialAlias.BEDS;
                double value12 = vector.getX() + vector3.getX();
                double value13 = vector.getY() + vector3.getY() - 0.2;
                double value14 = vector.getZ() + vector3.getZ();
                double value15 = value14;
                double value16 = value13;
                double value17 = value12;
                if (values3.contains(movementEnvironment.getMaterial(value17, value16, value15)) && !matches2 && vector2.getY() < 0.0) {
                    ProtocolVersion version3 = ProtocolVersion.V1_12;
                    if (this.protocolVersion.isAtLeast(version3)) {
                        vector2.setY(-vector2.getY() * 0.66F);
                        return new MovementDelta(box.getBottomCenter().subtract(vector), vector2, matches6, matches4, false);
                    }
                }
            }
            if (vector4.getY() != vector3.getY()) {
                vector2.setY(0);
            }
            ProtocolVersion version4 = ProtocolVersion.V1_13;
            if (this.protocolVersion.isAtLeast(version4)) {
                if (vector4.getZ() != vector3.getZ()) {
                    vector2.setZ(0);
                } else if (vector4.getX() != vector3.getX()) {
                    vector2.setX(0);
                }
            } else {
                if (vector4.getX() != vector3.getX()) {
                    vector2.setX(0);
                }
                if (vector4.getZ() != vector3.getZ()) {
                    vector2.setZ(0);
                }
            }
        } else {
            vector2 = new Vector(0, 0, 0);
        }
        return new MovementDelta(box.getBottomCenter().subtract(vector), vector2, matches6, matches4, false);
    }

    private AxisAlignedBoundingBox findAxisAlignedBoundingBox(Vector vector, AxisAlignedBoundingBox box, Collection<AxisAlignedBoundingBox> values) {
        for (AxisAlignedBoundingBox box2 : values) {
            double y = vector.getY();
            vector.setY(box2.clipYMovement(box, y));
        }
        box = box.offset(0.0, vector.getY(), 0.0);
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (this.protocolVersion.isAtLeast(version)) {
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
        } else {
            for (AxisAlignedBoundingBox box6 : values) {
                double x2 = vector.getX();
                vector.setX(box6.clipXMovement(box, x2));
            }
            box = box.offset(vector.getX(), 0.0, 0.0);
            for (AxisAlignedBoundingBox box7 : values) {
                double z3 = vector.getZ();
                vector.setZ(box7.clipZMovement(box, z3));
            }
            box = box.offset(0.0, 0.0, vector.getZ());
        }
        return box;
    }
}
