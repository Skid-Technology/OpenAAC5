package me.konsolas.aac.checks.player;

import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import me.konsolas.aac.checks.ViolationBuffer;
import me.konsolas.aac.compat.NativeAccess;
import me.konsolas.aac.compat.NativeAccessProvider;
import me.konsolas.aac.debug.DebugManager;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.world.MaterialAlias;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;

public class ActionDelayCheck extends BaseCheck {

    private static final ItemStack lastItem = MaterialAlias.BONE_MEAL.getItemStack();

    private final ViolationBuffer delayBuffer;

    private final boolean checkFastUse;

    private final boolean checkFastBow;

    private final boolean checkFastPlace;

    private final boolean checkFastBreak;

    private final boolean checkRegeneration;

    private final boolean checkFastSneak;

    private final boolean checkRelease;

    private final boolean usingItem;

    private long lastPlace;

    private long lastBreak;

    private long lastUse;

    private long lastBow;

    private long lastHeal;

    private long lastSneak;

    private long lastRelease;

    public ActionDelayCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("delays"));
        ConfigurationSection configuration = this.getConfiguration();
        ConfigurationSection section = configuration;
        this.delayBuffer = ViolationBuffer.create(section, (String) "vl");
        this.checkFastUse = this.getConfiguration().getBoolean("fast_break");
        this.checkFastBow = this.getConfiguration().getBoolean("fast_place");
        this.checkFastPlace = this.getConfiguration().getBoolean("fast_use");
        this.checkFastBreak = this.getConfiguration().getBoolean("fast_bow");
        this.checkRegeneration = this.getConfiguration().getBoolean("regen");
        this.checkFastSneak = this.getConfiguration().getBoolean("fast_sneak");
        this.checkRelease = this.getConfiguration().getBoolean("fast_release");
        this.usingItem = this.getConfiguration().getBoolean("break_delay");
    }

    public boolean checkActionDelay(boolean matches) {
        if (!matches) {
            this.lastPlace = System.currentTimeMillis();
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().log(this.getPlayer(), "delays: damaged block");
            }
        } else {
            this.lastPlace = 0L;
            DebugManager debugManager2 = this.getPlugin().getDebugManager();
            Player player3 = this.getPlayer();
            Player player4 = player3;
            if (debugManager2.isDebugging(player4)) {
                this.getPlugin().getDebugManager().log(this.getPlayer(), "delays: damaged block (instabreak)");
            }
        }
        if (this.usingItem) {
            long value = System.currentTimeMillis() - this.lastBreak;
            if (value < 500L) {
                float value2 = matches ? 0.05F : 0.1F;
                float value3 = value2 - (float) value / 1000.0F;
                if (this.delayBuffer.getLevel() > 0.5F) {
                    float value4 = value3 / value2;
                    FeatureType feature = FeatureType.AAC_DELAYS_BREAKDELAY;
                    this.recordFeature(feature, value4);
                }
                DebugManager debugManager3 = this.getPlugin().getDebugManager();
                Player player5 = this.getPlayer();
                Player player6 = player5;
                if (debugManager3.isDebugging(player6)) {
                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: breakdelay: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value3 });
                }
                return this.delayBuffer.checkViolationBuffer(value3);
            }
        }
        return false;
    }

    public boolean checkActionDelay(Block block) {
        if (this.checkFastUse && this.lastPlace != 0L) {
            this.lastBreak = System.currentTimeMillis();
            long value = this.lastBreak - this.lastPlace;
            NativeAccess instance = NativeAccessProvider.getInstance();
            Player player = this.getPlayer();
            Player player2 = player;
            double blockDamage = instance.getBlockDamage(player2, block);
            if (blockDamage >= 1.0) {
                return false;
            } else {
                float value2 = (float) (1.0 / blockDamage * 0.05F);
                float value3 = value2 - (float) value / 1000.0F;
                if (this.delayBuffer.getLevel() > 0.5F) {
                    float value4 = value3 / value2;
                    FeatureType feature = FeatureType.AAC_DELAYS_BREAK;
                    this.recordFeature(feature, value4);
                }
                DebugManager debugManager = this.getPlugin().getDebugManager();
                player = this.getPlayer();
                Player player3 = player;
                if (debugManager.isDebugging(player3)) {
                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: fastbreak: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value3 });
                }
                return this.delayBuffer.checkViolationBuffer(value3);
            }
        } else {
            return false;
        }
    }

    public boolean isActionDelay() {
        if (!this.checkFastBow) {
            return false;
        } else {
            for (ItemStack itemStack : NativeAccessProvider.getInstance().getHandItems(this.getPlayer())) {
                if (itemStack != null && (itemStack.getType() == Material.FLINT_AND_STEEL || itemStack.isSimilar(lastItem))) {
                    return false;
                }
            }
            long now = System.currentTimeMillis();
            long value = this.lastUse;
            long value2 = now - value;
            this.lastUse = now;
            if (value == 0L) {
                return false;
            } else {
                float value3 = 0.05F - (float) value2 / 1000.0F;
                if (this.delayBuffer.getLevel() > 0.5F) {
                    float value4 = value3 / 0.05F;
                    FeatureType feature = FeatureType.AAC_DELAYS_PLACE;
                    this.recordFeature(feature, value4);
                }
                DebugManager debugManager = this.getPlugin().getDebugManager();
                Player player = this.getPlayer();
                Player player2 = player;
                if (debugManager.isDebugging(player2)) {
                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: fastplace: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value3 });
                }
                return this.delayBuffer.checkViolationBuffer(value3);
            }
        }
    }

    public void onAction(Action action) {
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            this.lastBow = System.currentTimeMillis();
        }
    }

    public boolean checkActionDelay(Material material) {
        if (!this.checkFastPlace) {
            return false;
        } else {
            long value = System.currentTimeMillis() - this.lastBow;
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_13;
            float value2 = serverVersion.isAtLeast(version) && material == Material.DRIED_KELP ? 0.75F : 1.5F;
            float value3 = value2 - (float) value / 1000.0F;
            if (this.delayBuffer.getLevel() > 0.5F) {
                float value4 = value3 / value2;
                FeatureType feature = FeatureType.AAC_DELAYS_CONSUME;
                this.recordFeature(feature, value4);
            }
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: fastuse: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value3 });
            }
            return this.delayBuffer.checkViolationBuffer(value3);
        }
    }

    public boolean checkActionDelay(float value) {
        if (this.checkFastBreak) {
            long value2 = System.currentTimeMillis() - this.lastBow;
            float value3 = calculateValue(value);
            float value4 = value3 - (float) value2 / 1000.0F;
            if (this.delayBuffer.getLevel() > 0.5F) {
                float value5 = value4 / value3;
                FeatureType feature = FeatureType.AAC_DELAYS_BOW;
                this.recordFeature(feature, value5);
            }
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: fastbow: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value4 });
            }
            return this.delayBuffer.checkViolationBuffer(value4);
        } else {
            return false;
        }
    }

    private static float calculateValue(float value) {
        return 0.1F + 0.95F * value;
    }

    public boolean onEntityRegainHealthEventRegainReason(RegainReason regainReason) {
        if (this.checkRegeneration && regainReason == RegainReason.SATIATED) {
            long now = System.currentTimeMillis();
            long value = this.lastHeal;
            long value2 = now - value;
            this.lastHeal = now;
            if (value == 0L) {
                return false;
            } else {
                ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version = ProtocolVersion.V1_9;
                float value3 = serverVersion.isAtLeast(version) && this.getPlayer().getFoodLevel() == 20 ? 0.5F : 4.0F;
                float value4 = value3 - (float) value2 / 1000.0F;
                if (this.delayBuffer.getLevel() > 0.5F) {
                    float value5 = value4 / value3;
                    FeatureType feature = FeatureType.AAC_DELAYS_REGEN;
                    this.recordFeature(feature, value5);
                }
                DebugManager debugManager = this.getPlugin().getDebugManager();
                Player player = this.getPlayer();
                Player player2 = player;
                if (debugManager.isDebugging(player2)) {
                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: regen: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value4 });
                }
                return this.delayBuffer.checkViolationBuffer(value4);
            }
        } else {
            return false;
        }
    }

    public void tickWithCheckFastSneak() {
        if (this.checkFastSneak) {
            long now = System.currentTimeMillis();
            long value = this.lastSneak;
            long value2 = now - value;
            this.lastSneak = now;
            if (value == 0L) {
                return;
            }
            float value3 = 0.1F;
            float value4 = value3 - (float) value2 / 1000.0F;
            if (this.delayBuffer.getLevel() > 0.5F) {
                float value5 = value4 / value3;
                FeatureType feature = FeatureType.AAC_DELAYS_UNSNEAK;
                this.recordFeature(feature, value5);
            }
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: sneak: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value4 });
            }
            this.delayBuffer.checkViolationBuffer(value4);
        }
    }

    public void tick() {
        if (this.checkRelease) {
            long now = System.currentTimeMillis();
            long value = this.lastRelease;
            long value2 = now - value;
            this.lastRelease = now;
            if (value == 0L) {
                return;
            }
            float value3 = 0.1F;
            float value4 = value3 - (float) value2 / 1000.0F;
            if (this.delayBuffer.getLevel() > 0.5F) {
                float value5 = value4 / value3;
                FeatureType feature = FeatureType.AAC_DELAYS_RELEASE;
                this.recordFeature(feature, value5);
            }
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "delays: block: %.3f%+.3f", new Object[] { this.delayBuffer.getLevel(), value4 });
            }
            this.delayBuffer.checkViolationBuffer(value4);
        }
    }
}
