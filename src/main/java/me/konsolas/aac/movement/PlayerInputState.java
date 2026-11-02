package me.konsolas.aac.movement;

import java.util.Locale;

public class PlayerInputState {

    public static final PlayerInputState EMPTY = new PlayerInputState(0.0F, 0.0F, false, false, false, false, false, false, 0.0F, 0.0F, 0);

    private final float forward;

    private final float strafe;

    private final boolean sprinting;

    private final boolean jumping;

    private final boolean sneaking;

    private final boolean usingItem;

    private final boolean elytra;

    private final boolean flying;

    private final float yaw;

    private final float pitch;

    private final int attackedEntityId;

    public static String UNKNOWN_DESCRIPTION = "(?) (?, ?) ?";

    public PlayerInputState(float forward, float strafe, boolean sprinting, boolean jumping, boolean sneaking, boolean usingItem, boolean elytra, boolean flying, float yaw, float pitch, int attackedEntityId) {
        this.forward = forward;
        this.strafe = strafe;
        this.sprinting = sprinting;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.usingItem = usingItem;
        this.elytra = elytra;
        this.flying = flying;
        this.yaw = yaw;
        this.pitch = pitch;
        this.attackedEntityId = attackedEntityId;
    }

    public float getForward() {
        return this.forward;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public boolean isSprinting() {
        return this.sprinting;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public boolean isSneaking() {
        return this.sneaking;
    }

    public boolean isUsingItem() {
        return this.usingItem;
    }

    public boolean isElytra() {
        return this.elytra;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getAttackedEntityId() {
        return this.attackedEntityId;
    }

    public PlayerInputState copy() {
        return this.isJumping() ? this : new PlayerInputState(this.forward, this.strafe, this.sprinting, true, this.sneaking, this.usingItem, this.elytra, this.flying, this.yaw, this.pitch, this.attackedEntityId);
    }

    @Override
    public boolean equals(Object result) {
        if (this == result) {
            return true;
        } else if (result != null && this.getClass() == result.getClass()) {
            PlayerInputState playerInputState = (PlayerInputState) result;
            if (Float.compare(playerInputState.forward, this.forward) != 0) {
                return false;
            } else if (Float.compare(playerInputState.strafe, this.strafe) != 0) {
                return false;
            } else if (this.sprinting != playerInputState.sprinting) {
                return false;
            } else if (this.jumping != playerInputState.jumping) {
                return false;
            } else if (this.sneaking != playerInputState.sneaking) {
                return false;
            } else if (this.usingItem != playerInputState.usingItem) {
                return false;
            } else if (this.elytra != playerInputState.elytra) {
                return false;
            } else if (this.flying != playerInputState.flying) {
                return false;
            } else if (Float.compare(playerInputState.yaw, this.yaw) != 0) {
                return false;
            } else {
                return Float.compare(playerInputState.pitch, this.pitch) != 0 ? false : this.attackedEntityId == playerInputState.attackedEntityId;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int value = this.forward != 0.0F ? Float.floatToIntBits(this.forward) : 0;
        value = 31 * value + (this.strafe != 0.0F ? Float.floatToIntBits(this.strafe) : 0);
        value = 31 * value + (this.sprinting ? 1 : 0);
        value = 31 * value + (this.jumping ? 1 : 0);
        value = 31 * value + (this.sneaking ? 1 : 0);
        value = 31 * value + (this.usingItem ? 1 : 0);
        value = 31 * value + (this.elytra ? 1 : 0);
        value = 31 * value + (this.flying ? 1 : 0);
        value = 31 * value + (this.yaw != 0.0F ? Float.floatToIntBits(this.yaw) : 0);
        value = 31 * value + (this.pitch != 0.0F ? Float.floatToIntBits(this.pitch) : 0);
        return 31 * value + this.attackedEntityId;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('(');
        if (this.forward > 0.0F) {
            stringBuilder.append('W');
        } else if (this.forward < 0.0F) {
            stringBuilder.append('S');
        }
        if (this.strafe > 0.0F) {
            stringBuilder.append('A');
        } else if (this.strafe < 0.0F) {
            stringBuilder.append('D');
        }
        stringBuilder.append(')');
        stringBuilder.append(String.format(Locale.UK, " (%.2f, %.2f)", this.yaw, this.pitch));
        if (this.sprinting) {
            stringBuilder.append(" sprint");
        }
        if (this.jumping) {
            stringBuilder.append(" jump");
        }
        if (this.sneaking) {
            stringBuilder.append(" sneak");
        }
        if (this.usingItem) {
            stringBuilder.append(" use");
        }
        if (this.elytra) {
            stringBuilder.append(" elytra");
        }
        if (this.flying) {
            stringBuilder.append(" fly");
        }
        if (this.attackedEntityId > 0) {
            stringBuilder.append(" attack:").append(this.attackedEntityId);
        }
        return stringBuilder.toString();
    }
}
