package me.konsolas.aac.movement;

import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.entity.EntityType;

public interface EntityView {

    ProtocolVersion getProtocolVersion();

    EntityType getEntityType();

    boolean isSlowFalling();

    float getWalkSpeed();

    float getFlySpeed();

    boolean hasLevitation();

    int getLevitationAmplifier();

    boolean hasJumpBoost();

    int getJumpBoostAmplifier();

    boolean isInWater();

    boolean isInFlowingWater();

    boolean isInLava();

    boolean isInFlowingLava();

    boolean isPreviousInWater();

    boolean isPreviousInLava();

    boolean isPreviousFlowingFluid();

    double getWaterDepth();

    double getLavaDepth();

    int getDepthStriderLevel();

    boolean isDolphinsGrace();

    float getBlockFriction();

    double getJumpStrength();

    double getStepHeight();

    boolean isClimbing();

    boolean isInScaffolding();

    boolean isInCobweb();

    boolean isInSweetBerryBush();

    boolean isTouchingHoney();

    boolean isInBubbleColumn();

    boolean isHeadUnderwater();

    int getSoulSandBlockCount();

    boolean isOnHoney();

    boolean isAboveShulkerBox();

    AxisAlignedBoundingBox getBoundingBox();

    PlayerPose getPose();
}
