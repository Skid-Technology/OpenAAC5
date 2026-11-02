package me.konsolas.aac.movement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.util.Vector;

public enum PlayerPose {

    STANDING(result -> PlayerDimensions.STANDING, result -> 1.62F), SNEAKING(result -> {
        ProtocolVersion version = ProtocolVersion.V1_14;
        if (result.isAtLeast(version)) {
            return PlayerDimensions.SNEAKING;
        } else {
            ProtocolVersion version2 = ProtocolVersion.V1_9;
            return result.isAtLeast(version2) ? PlayerDimensions.SNEAKING_LEGACY : PlayerDimensions.STANDING;
        }
    }, result -> {
        ProtocolVersion version = ProtocolVersion.V1_14;
        return result.isAtLeast(version) ? 1.27F : 1.54F;
    }), FALL_FLYING(result -> PlayerDimensions.SWIMMING, result -> 0.4F), SWIMMING(result -> PlayerDimensions.SWIMMING, result -> 0.4F);

    public static List<PlayerPose> POSES = Collections.unmodifiableList(Arrays.asList(values()));

    private final Function<ProtocolVersion, EntityDimensions> dimensionsFunction;

    private final Function<ProtocolVersion, Float> eyeHeightFunction;

    private PlayerPose(Function<ProtocolVersion, EntityDimensions> dimensionsFunction, Function<ProtocolVersion, Float> eyeHeightFunction) {
        this.dimensionsFunction = dimensionsFunction;
        this.eyeHeightFunction = eyeHeightFunction;
    }

    public EntityDimensions findEntityDimensions(ProtocolVersion version) {
        return (EntityDimensions) this.dimensionsFunction.apply(version);
    }

    public float calculateValue(ProtocolVersion version) {
        return (Float) this.eyeHeightFunction.apply(version);
    }

    public boolean checkPlayerPose(Vector vector, MovementEnvironment movementEnvironment) {
        AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(vector, this.findEntityDimensions(movementEnvironment.getProtocolVersion()));
        return movementEnvironment.collides(box);
    }
}
