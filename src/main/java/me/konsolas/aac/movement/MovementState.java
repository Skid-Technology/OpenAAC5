package me.konsolas.aac.movement;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.konsolas.aac.compat.NativeAccessProvider;
import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MovementState {

    private final AtomicBoolean updated = new AtomicBoolean();

    private EntityType entityType;

    private EntityType rootVehicleType;

    private EntityType vehicleType;

    private EntityDimensions entityDimensions = EntityDimensions.DEFAULT;

    private boolean unsupportedEntity;

    private LivingEntity livingEntity;

    private float walkSpeed;

    private float flySpeed;

    private double jumpStrength;

    private double stepHeight;

    private int levitationAmplifier;

    private int jumpBoostAmplifier;

    private boolean slowFalling;

    private boolean dolphinsGrace;

    private boolean wearingElytra;

    private int depthStriderLevel;

    private int soulSpeedLevel;

    private double eyeHeight;

    private double entityPushMagnitude;

    private boolean nearFirework;

    private boolean nearBoat;

    private boolean nearShulker;

    private double borderMinX;

    private double borderMinZ;

    private double borderMaxX;

    private double borderMaxZ;

    private boolean sneaking;

    private boolean usingItem;

    private boolean gliding;

    private boolean flying;

    private boolean canSprint;

    private boolean sprinting;

    private float maximumRecentWalkSpeed;

    private final AtomicInteger sneakTransitionTicks = new AtomicInteger();

    private final AtomicInteger itemUseTransitionTicks = new AtomicInteger();

    private final AtomicInteger glideTransitionTicks = new AtomicInteger();

    private final AtomicInteger flightTransitionTicks = new AtomicInteger();

    private final AtomicInteger speedTransitionTicks = new AtomicInteger();

    private volatile boolean riptideReady;

    public void pollUpdateFlag() {
        this.updated.get();
    }

    public void markUpdated() {
        this.updated.set(true);
    }

    public void updateAttributes(Player player) {
        LivingEntity livingEntity2 = player;
        while (livingEntity2.getVehicle() instanceof LivingEntity) {
            livingEntity2 = (LivingEntity) livingEntity2.getVehicle();
        }
        if (livingEntity2 != player) {
            this.vehicleType = livingEntity2.getType();
        }
        this.livingEntity = (LivingEntity) livingEntity2;
        if (livingEntity2.getVehicle() != null) {
            Entity vehicle = livingEntity2.getVehicle();
            while (vehicle.getVehicle() != null) {
                vehicle = vehicle.getVehicle();
            }
            this.rootVehicleType = vehicle.getType();
        } else {
            this.rootVehicleType = null;
        }
        this.entityType = livingEntity2.getType();
        this.entityDimensions = EntityDimensions.create((Entity) livingEntity2);
        int value = -1;
        int value2 = -1;
        int value3 = -1;
        int value4 = -1;
        boolean matches = false;
        boolean matches2 = false;
        for (PotionEffect potionEffect : livingEntity2.getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.SPEED)) {
                value = potionEffect.getAmplifier();
            } else if (potionEffect.getType().equals(PotionEffectType.SLOW)) {
                value2 = potionEffect.getAmplifier();
            } else if (potionEffect.getType().equals(PotionEffectType.JUMP)) {
                value3 = potionEffect.getAmplifier();
            } else {
                ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version = ProtocolVersion.V1_9;
                if (serverVersion.isAtLeast(version) && potionEffect.getType().equals(PotionEffectType.LEVITATION)) {
                    value4 = potionEffect.getAmplifier();
                } else {
                    serverVersion = ProtocolVersion.getServerVersion();
                    ProtocolVersion version2 = ProtocolVersion.V1_13;
                    if (serverVersion.isAtLeast(version2) && potionEffect.getType().equals(PotionEffectType.SLOW_FALLING)) {
                        matches = true;
                    } else {
                        serverVersion = ProtocolVersion.getServerVersion();
                        ProtocolVersion version3 = ProtocolVersion.V1_13;
                        if (serverVersion.isAtLeast(version3) && potionEffect.getType().equals(PotionEffectType.DOLPHINS_GRACE)) {
                            matches2 = true;
                        }
                    }
                }
            }
        }
        this.levitationAmplifier = value4;
        this.jumpBoostAmplifier = value3;
        this.slowFalling = matches;
        this.dolphinsGrace = matches2;
        ProtocolVersion serverVersion2 = ProtocolVersion.getServerVersion();
        ProtocolVersion version4 = ProtocolVersion.V1_9;
        if (serverVersion2.isAtLeast(version4)) {
            float value5 = (float) Objects.requireNonNull(livingEntity2.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getValue();
            if (livingEntity2 instanceof Player && ((Player) livingEntity2).isSprinting()) {
                value5 = (float) (value5 / 1.300000011920929);
            }
            if (Math.abs(this.walkSpeed - value5) > 0.001) {
                this.speedTransitionTicks.set(5);
                this.maximumRecentWalkSpeed = Math.max(this.walkSpeed, value5);
            } else {
                this.speedTransitionTicks.updateAndGet(result -> Math.max(result - 1, 0));
            }
            this.walkSpeed = value5;
            this.flySpeed = player.getFlySpeed() / 2.0F;
        } else {
            float value6;
            switch(this.entityType) {
                case PLAYER:
                    value6 = ((Player) livingEntity2).getWalkSpeed() / 2.0F;
                    break;
                case PIG:
                    value6 = 0.25F;
                    break;
                case HORSE:
                case SKELETON_HORSE:
                case DONKEY:
                case MULE:
                case LLAMA:
                    value6 = 0.3375F;
                    break;
                default:
                    value6 = 0.0F;
            }
            if (value >= 0) {
                value6 = (float) (value6 * (1.0 + 0.2 * (value + 1)));
            }
            if (value2 >= 0) {
                value6 = (float) (value6 * (1.0 - 0.15 * (value2 + 1)));
            }
            this.walkSpeed = value6;
            this.flySpeed = player.getFlySpeed() / 2.0F;
        }
        switch(this.entityType) {
            case PLAYER:
            case PIG:
            case HORSE:
            case SKELETON_HORSE:
            case DONKEY:
            case MULE:
            case LLAMA:
            case STRIDER:
                this.unsupportedEntity = false;
                break;
            default:
                this.unsupportedEntity = true;
        }
        switch(this.entityType) {
            case PLAYER:
            case PIG:
                this.jumpStrength = 0.42F;
                break;
            case HORSE:
                this.jumpStrength = ((Horse) livingEntity2).getJumpStrength();
                break;
            case SKELETON_HORSE:
                this.jumpStrength = ((SkeletonHorse) livingEntity2).getJumpStrength();
                break;
            case DONKEY:
                this.jumpStrength = ((Donkey) livingEntity2).getJumpStrength();
                break;
            case MULE:
                this.jumpStrength = ((Mule) livingEntity2).getJumpStrength();
                break;
            case LLAMA:
                this.jumpStrength = ((Llama) livingEntity2).getJumpStrength();
                break;
            case STRIDER:
                this.jumpStrength = 0.42F;
                break;
            case ARMOR_STAND:
                this.jumpStrength = 0.0;
                break;
            default:
                this.jumpStrength = 0.0;
        }
        switch(this.entityType) {
            case PLAYER:
                this.stepHeight = 0.6;
                break;
            case PIG:
            case HORSE:
            case SKELETON_HORSE:
            case DONKEY:
            case MULE:
            case LLAMA:
                this.stepHeight = 1.0;
                break;
            case STRIDER:
                this.stepHeight = 1.0;
            default:
                this.stepHeight = 0.0;
                break;
            case ARMOR_STAND:
                this.stepHeight = 0.0;
        }
        if (livingEntity2 instanceof Player) {
            ItemStack boots = ((Player) livingEntity2).getInventory().getBoots();
            ItemStack chestplate = ((Player) livingEntity2).getInventory().getChestplate();
            this.depthStriderLevel = boots != null ? boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER) : 0;
            ProtocolVersion serverVersion3 = ProtocolVersion.getServerVersion();
            ProtocolVersion version5 = ProtocolVersion.V1_16;
            this.soulSpeedLevel = serverVersion3.isAtLeast(version5) && boots != null ? boots.getEnchantmentLevel(Enchantment.SOUL_SPEED) : 0;
            serverVersion3 = ProtocolVersion.getServerVersion();
            ProtocolVersion version6 = ProtocolVersion.V1_9;
            this.wearingElytra = serverVersion3.isAtLeast(version6) && chestplate != null && chestplate.getType() == Material.ELYTRA;
        }
        this.eyeHeight = player.getEyeHeight();
    }

    public void updateInputFlags(Player player) {
        boolean matches = false;
        boolean matches2 = false;
        boolean matches3 = false;
        for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
            if (entity.getType() == EntityType.FIREWORK) {
                matches = true;
            }
            if (entity.getType() == EntityType.BOAT) {
                matches2 = true;
            }
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_9;
            if (serverVersion.isAtLeast(version) && entity.getType() == EntityType.SHULKER) {
                matches3 = true;
            }
        }
        this.entityPushMagnitude = EntityPushCalculator.calculatePush(player).length();
        this.nearFirework = matches;
        this.nearBoat = matches2;
        this.nearShulker = matches3;
        WorldBorder worldBorder = player.getWorld().getWorldBorder();
        Location center = worldBorder.getCenter();
        double value = worldBorder.getSize() / 2.0;
        this.borderMinX = center.getX() - value;
        this.borderMinZ = center.getZ() - value;
        this.borderMaxX = center.getX() + value;
        this.borderMaxZ = center.getZ() + value;
    }

    public void updateNearbyEntities(Player player) {
        ItemStack activeItem = NativeAccessProvider.getInstance().getActiveItem(player);
        boolean sneaking2 = player.isSneaking();
        boolean matches = activeItem != null;
        boolean gliding2 = NativeAccessProvider.getInstance().isGliding(player);
        boolean flying2 = player.isFlying();
        player.getAllowFlight();
        if (sneaking2 != this.sneaking) {
            this.sneaking = sneaking2;
            this.sneakTransitionTicks.updateAndGet(result -> Math.max(result, 2));
        } else {
            this.sneakTransitionTicks.updateAndGet(result -> Math.max(result - 1, 0));
        }
        if (matches != this.usingItem) {
            this.usingItem = matches;
            this.itemUseTransitionTicks.updateAndGet(result -> Math.max(result, 2));
        } else {
            this.itemUseTransitionTicks.updateAndGet(result -> Math.max(result - 1, 0));
        }
        if (this.gliding != gliding2) {
            this.gliding = gliding2;
            this.glideTransitionTicks.updateAndGet(result -> Math.max(result, 1));
        } else {
            this.glideTransitionTicks.updateAndGet(result -> Math.max(result - 1, 0));
        }
        if (this.flying != flying2 && this.flightTransitionTicks.get() <= 0) {
            this.flying = flying2;
            this.flightTransitionTicks.updateAndGet(result -> Math.max(result, 10));
        } else {
            this.flightTransitionTicks.updateAndGet(result -> Math.max(result - 1, 0));
        }
        this.sprinting = player.isSprinting();
        this.canSprint = player.getFoodLevel() > 6 && !player.hasPotionEffect(PotionEffectType.BLINDNESS);
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (serverVersion.isAtLeast(version) && matches) {
            this.riptideReady = activeItem.getType() == Material.TRIDENT && activeItem.getEnchantmentLevel(Enchantment.RIPTIDE) > 0;
        }
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public EntityType getVehicleType() {
        return this.vehicleType;
    }

    public EntityType getRootVehicleType() {
        return this.rootVehicleType;
    }

    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    public EntityDimensions getEntityDimensions() {
        return this.entityDimensions;
    }

    public boolean isUnsupportedEntity() {
        return this.unsupportedEntity;
    }

    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    public float getFlySpeed() {
        return this.flySpeed;
    }

    public double getJumpStrength() {
        return this.jumpStrength;
    }

    public double getStepHeight() {
        return this.stepHeight;
    }

    public int getLevitationAmplifier() {
        return this.levitationAmplifier;
    }

    public int getJumpBoostAmplifier() {
        return this.jumpBoostAmplifier;
    }

    public boolean isSlowFalling() {
        return this.slowFalling;
    }

    public boolean isDolphinsGrace() {
        return this.dolphinsGrace;
    }

    public int getDepthStriderLevel() {
        return this.depthStriderLevel;
    }

    public int getSoulSpeedLevel() {
        return this.soulSpeedLevel;
    }

    public boolean isWearingElytra() {
        return this.wearingElytra;
    }

    public double getEyeHeight() {
        return this.eyeHeight;
    }

    public double getEntityPushMagnitude() {
        return this.entityPushMagnitude;
    }

    public boolean isNearFirework() {
        return this.nearFirework;
    }

    public boolean isNearBoat() {
        return this.nearBoat;
    }

    public boolean isNearShulker() {
        return this.nearShulker;
    }

    public Boolean getStableSneaking() {
        return this.sneakTransitionTicks.get() > 0 ? null : this.sneaking;
    }

    public Boolean getStableUsingItem() {
        return this.itemUseTransitionTicks.get() > 0 ? null : this.usingItem;
    }

    public Boolean getStableGliding() {
        return this.glideTransitionTicks.get() > 0 ? null : this.gliding;
    }

    public Boolean getFlyingState() {
        return this.flying;
    }

    public void deferSneakState(int value) {
        this.sneakTransitionTicks.updateAndGet(result -> Math.max(result, value));
    }

    public void deferItemUseState(int value) {
        this.itemUseTransitionTicks.updateAndGet(result -> Math.max(result, value));
    }

    public void deferGlideState(int value) {
        this.glideTransitionTicks.updateAndGet(result -> Math.max(result, value));
    }

    public void deferFlightState(int value, boolean matches) {
        this.flightTransitionTicks.updateAndGet(result -> Math.max(result, value));
        this.flying = matches;
    }

    public void resetGlideTransition() {
        this.glideTransitionTicks.set(0);
    }

    public boolean isSprinting() {
        return this.sprinting;
    }

    public boolean canSprint() {
        return this.canSprint;
    }

    public boolean isRiptideReady() {
        return this.riptideReady;
    }

    public boolean crossesWorldBorder(double value, double value2, double value3) {
        return value + value3 > this.borderMaxX || value - value3 < this.borderMinX || value2 + value3 > this.borderMaxZ || value2 - value3 < this.borderMinZ;
    }

    public float getMaximumRecentWalkSpeed() {
        return this.maximumRecentWalkSpeed;
    }

    public boolean hasRecentSpeedChange() {
        return this.speedTransitionTicks.get() > 0;
    }

    public void resetSpeedTransition() {
        this.speedTransitionTicks.set(0);
    }
}
