package me.konsolas.aac.movement;

import java.util.Set;
import java.util.function.Predicate;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.world.MaterialAlias;
import me.konsolas.aac.world.fluid.FluidContact;
import me.konsolas.aac.world.fluid.FluidType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class EntityState implements EntityView {

    private static final FluidContact MOVEMENT_COLLISION_RESULT = new FluidContact(false, false, 0.0);

    private static final FluidContact MOVEMENT_COLLISION_RESULT2 = new FluidContact(true, false, 0.0);

    private final ProtocolVersion protocolVersion;

    private final EntityType entityType;

    private final float walkSpeed;

    private final float flySpeed;

    private final double jumpStrength;

    private final double stepHeight;

    private final int levitationAmplifier;

    private final int jumpBoostAmplifier;

    private final boolean slowFalling;

    private final boolean dolphinsGrace;

    private final int depthStriderLevel;

    private final float blockFriction;

    private final AxisAlignedBoundingBox boundingBox;

    private final PlayerPose pose;

    private final boolean climbing;

    private final boolean inScaffolding;

    private final boolean inCobweb;

    private final boolean inSweetBerryBush;

    private final boolean touchingHoney;

    private final boolean inBubbleColumn;

    private final boolean headUnderwater;

    private final FluidContact waterContact;

    private final FluidContact lavaContact;

    private final boolean previousInWater;

    private final boolean previousInLava;

    private final boolean previousFlowingFluid;

    private final int soulSandBlockCount;

    private final boolean onHoney;

    private final boolean aboveShulkerBox;

    public EntityState(Vector vector, EntityView entityView, MovementState movementState, MovementEnvironment movementEnvironment) {
        this.protocolVersion = movementEnvironment.getProtocolVersion();
        this.entityType = movementState.getEntityType();
        this.walkSpeed = movementState.getWalkSpeed();
        this.flySpeed = movementState.getFlySpeed();
        this.jumpStrength = movementState.getJumpStrength();
        this.stepHeight = movementState.getStepHeight();
        this.levitationAmplifier = movementState.getLevitationAmplifier();
        this.jumpBoostAmplifier = movementState.getJumpBoostAmplifier();
        this.slowFalling = movementState.isSlowFalling();
        this.dolphinsGrace = movementState.isDolphinsGrace();
        this.depthStriderLevel = movementState.getDepthStriderLevel();
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();
        double value = y;
        double value2 = x;
        this.blockFriction = movementEnvironment.getBlockFriction(value2, value, z);
        if (this.entityType == EntityType.PLAYER) {
            if (movementState.getStableGliding() != null && !movementState.getStableGliding()) {
                ProtocolVersion version = ProtocolVersion.V1_13;
                if (this.protocolVersion.isAtLeast(version)) {
                    if (PlayerPose.SWIMMING.checkPlayerPose(vector, movementEnvironment)) {
                        if (entityView == null) {
                            this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.STANDING).findEntityDimensions(this.protocolVersion));
                        } else {
                            this.pose = entityView.getPose();
                            this.boundingBox = new AxisAlignedBoundingBox(vector, this.pose.findEntityDimensions(this.protocolVersion));
                        }
                    } else {
                        decision: {
                            decision2: {
                                if (entityView != null && entityView.isInWater() && movementState.isSprinting()) {
                                    if (entityView.getPose() == PlayerPose.SWIMMING) {
                                        break decision2;
                                    }
                                    Set values = MaterialAlias.WATER;
                                    x = vector.getX();
                                    y = vector.getY() + movementState.getEyeHeight();
                                    double z2 = vector.getZ();
                                    double value3 = z2;
                                    double value4 = y;
                                    double value5 = x;
                                    if (values.contains(movementEnvironment.getMaterial(value5, value4, value3))) {
                                        break decision2;
                                    }
                                }
                                if (!PlayerPose.SNEAKING.checkPlayerPose(vector, movementEnvironment)) {
                                    if (movementState.getStableSneaking() != null && !movementState.getStableSneaking() && !PlayerPose.STANDING.checkPlayerPose(vector, movementEnvironment)) {
                                        this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.STANDING).findEntityDimensions(this.protocolVersion));
                                    } else {
                                        this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.SNEAKING).findEntityDimensions(this.protocolVersion));
                                    }
                                    break decision;
                                }
                            }
                            this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.SWIMMING).findEntityDimensions(this.protocolVersion));
                        }
                    }
                } else if (movementState.getStableSneaking() != null && !movementState.getStableSneaking()) {
                    this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.STANDING).findEntityDimensions(this.protocolVersion));
                } else {
                    this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.SNEAKING).findEntityDimensions(this.protocolVersion));
                }
            } else {
                this.boundingBox = new AxisAlignedBoundingBox(vector, (this.pose = PlayerPose.FALL_FLYING).findEntityDimensions(this.protocolVersion));
            }
        } else {
            this.pose = PlayerPose.STANDING;
            this.boundingBox = new AxisAlignedBoundingBox(vector, movementState.getEntityDimensions());
        }
        boolean matches;
        decision3: {
            AxisAlignedBoundingBox box = this.boundingBox.expand(0.001, 0.001, 0.001);
            double x2 = vector.getX();
            y = vector.getY();
            double z3 = vector.getZ();
            double value6 = z3;
            double value7 = y;
            double value8 = x2;
            this.climbing = movementEnvironment.checkMovementEnvironment(value8, value7, value6);
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version2 = ProtocolVersion.V1_14;
            if (serverVersion.isAtLeast(version2)) {
                Predicate predicate = result -> result == Material.SCAFFOLDING;
                Predicate predicate2 = predicate;
                if (movementEnvironment.checkMovementEnvironment(box, predicate2)) {
                    matches = true;
                    break decision3;
                }
            }
            matches = false;
        }
        AxisAlignedBoundingBox box2;
        decision4: {
            this.inScaffolding = matches;
            box2 = this.boundingBox.expand(-0.001, -0.001, -0.001);
            Predicate predicate3 = result -> result == MaterialAlias.COBWEB.getMaterial();
            Predicate predicate4 = predicate3;
            this.inCobweb = movementEnvironment.checkMovementEnvironment(box2, predicate4);
            ProtocolVersion serverVersion2 = ProtocolVersion.getServerVersion();
            ProtocolVersion version3 = ProtocolVersion.V1_14;
            if (serverVersion2.isAtLeast(version3)) {
                ProtocolVersion version4 = ProtocolVersion.V1_14;
                if (this.protocolVersion.isAtLeast(version4)) {
                    Predicate predicate5 = result -> result == Material.SWEET_BERRY_BUSH;
                    Predicate predicate6 = predicate5;
                    if (movementEnvironment.checkMovementEnvironment(box2, predicate6)) {
                        matches = true;
                        break decision4;
                    }
                }
            }
            matches = false;
        }
        decision5: {
            this.inSweetBerryBush = matches;
            AxisAlignedBoundingBox box3 = this.boundingBox.withMinY(this.boundingBox.getMinY() + 0.0625).expand(-0.001, -0.001, -0.001);
            ProtocolVersion serverVersion3 = ProtocolVersion.getServerVersion();
            ProtocolVersion version5 = ProtocolVersion.V1_15;
            if (serverVersion3.isAtLeast(version5)) {
                ProtocolVersion version6 = ProtocolVersion.V1_15;
                if (this.protocolVersion.isAtLeast(version6)) {
                    Predicate predicate7 = result -> result == Material.HONEY_BLOCK;
                    Predicate predicate8 = predicate7;
                    if (movementEnvironment.checkMovementEnvironment(box3, predicate8)) {
                        matches = true;
                        break decision5;
                    }
                }
            }
            matches = false;
        }
        decision6: {
            this.touchingHoney = matches;
            ProtocolVersion serverVersion4 = ProtocolVersion.getServerVersion();
            ProtocolVersion version7 = ProtocolVersion.V1_13;
            if (serverVersion4.isAtLeast(version7)) {
                AxisAlignedBoundingBox box4 = this.boundingBox;
                Predicate predicate9 = result -> result == Material.BUBBLE_COLUMN;
                Predicate predicate10 = predicate9;
                AxisAlignedBoundingBox box5 = box4;
                if (movementEnvironment.checkMovementEnvironment(box5, predicate10)) {
                    matches = true;
                    break decision6;
                }
            }
            matches = false;
        }
        this.inBubbleColumn = matches;
        x = vector.getX();
        y = vector.getY() + 1.0 - 0.1;
        double z4 = vector.getZ();
        double value10 = z4;
        double value11 = y;
        double value12 = x;
        this.headUnderwater = movementEnvironment.getFluidType(value12, value11, value10) == FluidType.WATER;
        AxisAlignedBoundingBox box6 = this.boundingBox.expand(-0.001, -0.001, -0.001);
        ProtocolVersion version8 = ProtocolVersion.V1_13;
        if (!this.protocolVersion.isAtLeast(version8)) {
            box6 = box6.expand(0.0, -0.4, 0.0);
        }
        FluidType fluidType = FluidType.WATER;
        this.waterContact = movementEnvironment.sampleFluidContact(box6, fluidType);
        ProtocolVersion version9 = ProtocolVersion.V1_13;
        if (this.protocolVersion.isAtLeast(version9)) {
            box6 = this.boundingBox.expand(-0.001, -0.001, -0.001);
        } else {
            box6 = this.boundingBox.expand(-0.1F, -0.4F, -0.1F);
        }
        ProtocolVersion version10 = ProtocolVersion.V1_16;
        if (this.protocolVersion.isAtLeast(version10)) {
            FluidType fluidType2 = FluidType.LAVA;
            this.lavaContact = movementEnvironment.sampleFluidContact(box6, fluidType2);
        } else {
            Predicate predicate11 = MaterialAlias.LAVA::contains;
            Predicate predicate12 = predicate11;
            this.lavaContact = movementEnvironment.checkMovementEnvironment(box6, predicate12) ? MOVEMENT_COLLISION_RESULT2 : MOVEMENT_COLLISION_RESULT;
        }
        Material material;
        decision7: {
            this.previousInWater = entityView != null && entityView.isInWater();
            this.previousInLava = entityView != null && entityView.isInLava();
            this.previousFlowingFluid = entityView != null && (entityView.isInFlowingWater() || entityView.isInFlowingLava());
            double x3 = vector.getX();
            double value13 = vector.getY() - 0.5;
            double z5 = vector.getZ();
            double value14 = z5;
            double value15 = value13;
            double value16 = x3;
            material = movementEnvironment.getMaterial(value16, value15, value14);
            if (movementState.getSoulSpeedLevel() > 0) {
                double x4 = vector.getX();
                double value17 = vector.getY() - 0.5;
                double z6 = vector.getZ();
                double value18 = z6;
                double value19 = value17;
                double value20 = x4;
                Material material2;
                if ((material2 = movementEnvironment.getMaterial(value20, value19, value18)) == Material.SOUL_SAND || material2 == Material.SOUL_SOIL) {
                    this.soulSandBlockCount = 0;
                    break decision7;
                }
            }
            ProtocolVersion serverVersion5 = ProtocolVersion.getServerVersion();
            ProtocolVersion version11 = ProtocolVersion.V1_15;
            if (serverVersion5.isAtLeast(version11)) {
                this.soulSandBlockCount = material == Material.SOUL_SAND ? 1 : 0;
            } else {
                Predicate predicate13 = result -> result == Material.SOUL_SAND;
                this.soulSandBlockCount = movementEnvironment.calculateCount(box2, predicate13);
            }
        }
        decision8: {
            ProtocolVersion serverVersion6 = ProtocolVersion.getServerVersion();
            ProtocolVersion version12 = ProtocolVersion.V1_15;
            if (serverVersion6.isAtLeast(version12)) {
                ProtocolVersion version13 = ProtocolVersion.V1_15;
                if (this.protocolVersion.isAtLeast(version13) && material == Material.HONEY_BLOCK) {
                    matches = true;
                    break decision8;
                }
            }
            matches = false;
        }
        this.onHoney = matches;
        AxisAlignedBoundingBox box7 = this.boundingBox.offset(0.0, -1.2, 0.0);
        Predicate predicate14 = MaterialAlias.SHULKER_BOXES::contains;
        Predicate predicate15 = predicate14;
        AxisAlignedBoundingBox box8 = box7;
        this.aboveShulkerBox = movementEnvironment.checkMovementEnvironment(box8, predicate15);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public EntityType getEntityType() {
        return this.entityType;
    }

    @Override
    public boolean isSlowFalling() {
        return this.slowFalling;
    }

    @Override
    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    @Override
    public float getFlySpeed() {
        return this.flySpeed;
    }

    @Override
    public boolean hasLevitation() {
        return this.levitationAmplifier >= 0;
    }

    @Override
    public int getLevitationAmplifier() {
        return this.levitationAmplifier;
    }

    @Override
    public boolean hasJumpBoost() {
        return this.jumpBoostAmplifier >= 0;
    }

    @Override
    public int getJumpBoostAmplifier() {
        return this.jumpBoostAmplifier;
    }

    @Override
    public boolean isInWater() {
        return this.waterContact.isTouching();
    }

    @Override
    public boolean isInFlowingWater() {
        return this.waterContact.isFlowing();
    }

    @Override
    public boolean isInLava() {
        return this.lavaContact.isTouching();
    }

    @Override
    public boolean isInFlowingLava() {
        return this.lavaContact.isFlowing();
    }

    @Override
    public boolean isPreviousInWater() {
        return this.previousInWater;
    }

    @Override
    public boolean isPreviousInLava() {
        return this.previousInLava;
    }

    @Override
    public boolean isPreviousFlowingFluid() {
        return this.previousFlowingFluid;
    }

    @Override
    public double getWaterDepth() {
        return this.waterContact.getDepth();
    }

    @Override
    public double getLavaDepth() {
        return this.lavaContact.getDepth();
    }

    @Override
    public int getDepthStriderLevel() {
        return this.depthStriderLevel;
    }

    @Override
    public boolean isDolphinsGrace() {
        return this.dolphinsGrace;
    }

    @Override
    public float getBlockFriction() {
        return this.blockFriction;
    }

    @Override
    public double getJumpStrength() {
        return this.jumpStrength;
    }

    @Override
    public double getStepHeight() {
        return this.stepHeight;
    }

    @Override
    public boolean isClimbing() {
        return this.climbing;
    }

    @Override
    public boolean isInScaffolding() {
        return this.inScaffolding;
    }

    @Override
    public boolean isInCobweb() {
        return this.inCobweb;
    }

    @Override
    public boolean isInSweetBerryBush() {
        return this.inSweetBerryBush;
    }

    @Override
    public boolean isTouchingHoney() {
        return this.touchingHoney;
    }

    @Override
    public boolean isInBubbleColumn() {
        return this.inBubbleColumn;
    }

    @Override
    public boolean isHeadUnderwater() {
        return this.headUnderwater;
    }

    @Override
    public int getSoulSandBlockCount() {
        return this.soulSandBlockCount;
    }

    @Override
    public boolean isOnHoney() {
        return this.onHoney;
    }

    @Override
    public boolean isAboveShulkerBox() {
        return this.aboveShulkerBox;
    }

    @Override
    public AxisAlignedBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public PlayerPose getPose() {
        return this.pose;
    }
}
