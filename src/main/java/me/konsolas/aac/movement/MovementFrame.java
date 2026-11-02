package me.konsolas.aac.movement;

import java.util.Objects;
import org.bukkit.util.Vector;

public class MovementFrame {

    private final PlayerInputState input;

    private final RotationDelta rotationDelta;

    private final Vector position;

    private final MovementDelta movementDelta;

    private final TimedVelocity velocity;

    private final int tick;

    public MovementFrame(PlayerInputState playerInputState, TimedVelocity timedVelocity, Vector vector, RotationDelta rotationDelta, MovementDelta movementDelta) {
        this(playerInputState, timedVelocity, vector, rotationDelta, movementDelta, 0);
    }

    private MovementFrame(PlayerInputState input, TimedVelocity velocity, Vector position, RotationDelta rotationDelta, MovementDelta movementDelta, int tick) {
        this.input = input;
        this.rotationDelta = rotationDelta;
        this.position = position;
        this.movementDelta = movementDelta;
        this.velocity = velocity;
        this.tick = tick;
    }

    public PlayerInputState getInput() {
        return this.input;
    }

    public RotationDelta getRotationDelta() {
        return this.rotationDelta;
    }

    public Vector getVector() {
        return this.position.clone();
    }

    public MovementDelta getMovementDelta() {
        return this.movementDelta;
    }

    public TimedVelocity getVelocity() {
        return this.velocity;
    }

    public int getTick() {
        return this.tick;
    }

    public MovementFrame withVector(Vector vector) {
        Vector vector2 = vector.clone();
        return new MovementFrame(this.input, this.velocity, this.position, this.rotationDelta, new MovementDelta(vector2, vector2, this.movementDelta.isHorizontalCollision(), this.movementDelta.isVerticalCollision(), this.movementDelta.isOnGround()), this.tick);
    }

    public MovementFrame withVectorWithInput(Vector vector) {
        return new MovementFrame(this.input, this.velocity, this.position, this.rotationDelta, new MovementDelta(this.movementDelta.getExpectedMovement(), vector, this.movementDelta.isHorizontalCollision(), this.movementDelta.isVerticalCollision(), this.movementDelta.isOnGround()), this.tick);
    }

    public MovementFrame withValue(double value) {
        Vector actualMovement = this.movementDelta.getActualMovement();
        actualMovement.setY(value);
        return this.withVectorWithInput(actualMovement);
    }

    public MovementFrame copyWithInput() {
        return new MovementFrame(this.input, this.velocity, this.position, this.rotationDelta, new MovementDelta(this.movementDelta.getExpectedMovement(), this.movementDelta.getActualMovement(), true, this.movementDelta.isVerticalCollision(), this.movementDelta.isOnGround()), this.tick);
    }

    public MovementFrame copy() {
        return new MovementFrame(this.input, this.velocity, this.position, this.rotationDelta, new MovementDelta(this.movementDelta.getExpectedMovement(), this.movementDelta.getActualMovement(), this.movementDelta.isHorizontalCollision(), true, this.movementDelta.isOnGround()), this.tick);
    }

    @Override
    public boolean equals(Object result) {
        if (this == result) {
            return true;
        } else if (result != null && this.getClass() == result.getClass()) {
            MovementFrame frame = (MovementFrame) result;
            return Objects.equals(this.rotationDelta, frame.rotationDelta) && Objects.equals(this.movementDelta, frame.movementDelta) && Objects.equals(this.velocity, frame.velocity);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.rotationDelta, this.movementDelta, this.velocity);
    }
}
