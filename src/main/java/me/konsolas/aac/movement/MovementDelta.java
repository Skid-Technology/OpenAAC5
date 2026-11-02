package me.konsolas.aac.movement;

import java.util.Objects;
import org.bukkit.util.Vector;

public class MovementDelta {

    private final Vector expectedMovement;

    private final Vector actualMovement;

    private final boolean horizontalCollision;

    private final boolean verticalCollision;

    private final boolean onGround;

    public MovementDelta(Vector expectedMovement, Vector actualMovement, boolean horizontalCollision, boolean verticalCollision, boolean onGround) {
        this.expectedMovement = expectedMovement;
        this.actualMovement = actualMovement;
        this.horizontalCollision = horizontalCollision;
        this.verticalCollision = verticalCollision;
        this.onGround = onGround;
    }

    public Vector getExpectedMovement() {
        return this.expectedMovement;
    }

    public Vector getActualMovement() {
        return this.actualMovement;
    }

    public boolean isHorizontalCollision() {
        return this.horizontalCollision;
    }

    public boolean isVerticalCollision() {
        return this.verticalCollision;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public boolean equals(Object result) {
        if (this == result) {
            return true;
        } else if (result != null && this.getClass() == result.getClass()) {
            MovementDelta movementDelta = (MovementDelta) result;
            return this.horizontalCollision == movementDelta.horizontalCollision && this.verticalCollision == movementDelta.verticalCollision && Objects.equals(this.expectedMovement, movementDelta.expectedMovement) && Objects.equals(this.actualMovement, movementDelta.actualMovement);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.expectedMovement, this.actualMovement, this.horizontalCollision, this.verticalCollision);
    }
}
