package me.konsolas.aac.checks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import me.konsolas.aac.AAC;
import me.konsolas.aac.checks.combat.AimbotCheck;
import me.konsolas.aac.checks.combat.AutoClickerCheck;
import me.konsolas.aac.checks.combat.HitboxCheck;
import me.konsolas.aac.checks.interaction.InteractCheck;
import me.konsolas.aac.checks.movement.MovementCheck;
import me.konsolas.aac.checks.player.ActionDelayCheck;
import me.konsolas.aac.checks.player.ActionStateTracker;
import me.konsolas.aac.checks.player.MiscellaneousCheck;
import me.konsolas.aac.protocol.EntityVelocityPacket;
import me.konsolas.aac.protocol.PacketReader;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerChecks extends BaseCheck {

    private long lastTick;

    private final ActionDelayCheck delayCheck;

    private final MovementCheck movementCheck;

    private final AimbotCheck aimbotCheck;

    private final AutoClickerCheck autoClickerCheck;

    private final HitboxCheck hitboxCheck;

    private final InteractCheck interactCheck;

    private final MiscellaneousCheck miscellaneousCheck;

    private final ActionStateTracker actionStateCheck;

    public PlayerChecks(AAC aacPlugin, Player player) {
        super(aacPlugin, player, (String) (null));
        this.lastTick = 0L;
        this.delayCheck = this.getConfiguration().getBoolean("delays.enabled") ? new ActionDelayCheck(aacPlugin, player) : null;
        this.movementCheck = this.getConfiguration().getBoolean("move.enabled") ? new MovementCheck(aacPlugin, player) : null;
        this.aimbotCheck = this.getConfiguration().getBoolean("aimbot.enabled") ? new AimbotCheck(aacPlugin, player) : null;
        this.autoClickerCheck = this.getConfiguration().getBoolean("autoclicker.enabled") ? new AutoClickerCheck(aacPlugin, player) : null;
        this.hitboxCheck = this.getConfiguration().getBoolean("hitbox.enabled") ? new HitboxCheck(aacPlugin, player) : null;
        this.interactCheck = this.getConfiguration().getBoolean("interact.enabled") ? new InteractCheck(aacPlugin, player) : null;
        this.miscellaneousCheck = this.getConfiguration().getBoolean("misc.enabled") ? new MiscellaneousCheck(aacPlugin, player) : null;
        this.actionStateCheck = new ActionStateTracker(aacPlugin, player);
    }

    public void updateLocation(Location location) {
        if (this.movementCheck != null) {
            this.movementCheck.updateLocation(location);
        }
    }

    public void updateLocationWithMovementCheck(Location location) {
        if (this.movementCheck != null) {
            this.movementCheck.updateLocationWithFallDistance(location);
        }
        if (this.aimbotCheck != null) {
            this.aimbotCheck.updateLocation(location);
        }
    }

    public void onEvent(Event event) {
        if (event instanceof PlayerMoveEvent) {
            this.actionStateCheck.recordMovement();
        }
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent blockPlaceEvent = (BlockPlaceEvent) event;
            if (this.delayCheck != null && this.delayCheck.isActionDelay()) {
                blockPlaceEvent.setCancelled(true);
            }
        } else if (event instanceof BlockDamageEvent) {
            BlockDamageEvent blockDamageEvent = (BlockDamageEvent) event;
            if (this.delayCheck != null) {
                ActionDelayCheck actionDelayCheck = this.delayCheck;
                boolean instaBreak = blockDamageEvent.getInstaBreak();
                boolean matches = instaBreak;
                if (actionDelayCheck.checkActionDelay(matches)) {
                    blockDamageEvent.setCancelled(true);
                }
            }
        } else if (event instanceof BlockBreakEvent) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            if (this.delayCheck != null) {
                ActionDelayCheck actionDelayCheck2 = this.delayCheck;
                Block block = blockBreakEvent.getBlock();
                if (actionDelayCheck2.checkActionDelay(block)) {
                    blockBreakEvent.setCancelled(true);
                }
            }
            if (this.interactCheck != null) {
                InteractCheck interactCheck2 = this.interactCheck;
                Block block2 = blockBreakEvent.getBlock();
                if (interactCheck2.checkInteract(block2)) {
                    blockBreakEvent.setCancelled(true);
                }
            }
        } else if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
            if (this.delayCheck != null) {
                ActionDelayCheck actionDelayCheck3 = this.delayCheck;
                Action action = playerInteractEvent.getAction();
                Action action2 = action;
                actionDelayCheck3.onAction(action2);
            }
            if (this.interactCheck != null && this.interactCheck.onPlayerInteractEvent(playerInteractEvent)) {
                playerInteractEvent.setCancelled(true);
            }
            this.actionStateCheck.recordInteraction();
        } else if (event instanceof PlayerItemConsumeEvent) {
            PlayerItemConsumeEvent playerItemConsumeEvent = (PlayerItemConsumeEvent) event;
            if (this.delayCheck != null) {
                ActionDelayCheck actionDelayCheck4 = this.delayCheck;
                Material type = playerItemConsumeEvent.getItem().getType();
                Material material = type;
                if (actionDelayCheck4.checkActionDelay(material)) {
                    playerItemConsumeEvent.setCancelled(true);
                }
            }
        } else if (event instanceof EntityShootBowEvent) {
            EntityShootBowEvent entityShootBowEvent = (EntityShootBowEvent) event;
            if (this.delayCheck != null && entityShootBowEvent.getBow() != null && entityShootBowEvent.getBow().getType() == Material.BOW) {
                ActionDelayCheck actionDelayCheck5 = this.delayCheck;
                float force = entityShootBowEvent.getForce();
                if (actionDelayCheck5.checkActionDelay(force)) {
                    entityShootBowEvent.setCancelled(true);
                }
            }
        } else if (event instanceof EntityRegainHealthEvent) {
            EntityRegainHealthEvent entityRegainHealthEvent = (EntityRegainHealthEvent) event;
            if (this.delayCheck != null) {
                ActionDelayCheck actionDelayCheck6 = this.delayCheck;
                RegainReason regainReason = entityRegainHealthEvent.getRegainReason();
                if (actionDelayCheck6.onEntityRegainHealthEventRegainReason(regainReason)) {
                    entityRegainHealthEvent.setCancelled(true);
                }
            }
        }
    }

    public void handlePacket(PacketEvent event) {
        if (this.movementCheck != null) {
            this.movementCheck.handlePacket(event);
        }
        if (this.hitboxCheck != null) {
            this.hitboxCheck.handlePacket(event);
        }
        if (event.getPacketType() == Client.ENTITY_ACTION) {
            if (event.getPacket().getPlayerActions().read(0) == PlayerAction.STOP_SNEAKING && this.delayCheck != null) {
                this.delayCheck.tickWithCheckFastSneak();
            }
        } else if (event.getPacketType() == Client.BLOCK_DIG) {
            if (event.getPacket().getPlayerDigTypes().read(0) == PlayerDigType.RELEASE_USE_ITEM && this.delayCheck != null) {
                this.delayCheck.tick();
            }
            if (this.autoClickerCheck != null) {
                AutoClickerCheck autoClickerCheck2 = this.autoClickerCheck;
                BlockPosition blockPosition = (BlockPosition) event.getPacket().getBlockPositionModifier().read(0);
                PlayerDigType playerDigType = (PlayerDigType) event.getPacket().getPlayerDigTypes().read(0);
                BlockPosition blockPosition2 = blockPosition;
                autoClickerCheck2.updateBlockPosition(blockPosition2, playerDigType);
            }
        } else {
            PacketType packetType = event.getPacketType();
            if (PacketReader.checkPacketReader(packetType)) {
                if (this.autoClickerCheck != null) {
                    this.autoClickerCheck.tickWithTickDelayCheck();
                }
                if (this.interactCheck != null) {
                    InteractCheck interactCheck2 = this.interactCheck;
                    PacketContainer packet = event.getPacket();
                    PacketContainer packet2 = packet;
                    interactCheck2.handlePacket(packet2);
                }
            } else if (event.getPacketType() == Client.BLOCK_PLACE || event.getPacketType() == PacketReader.USE_ITEM) {
                PacketContainer packet3 = event.getPacket();
                EntityVelocityPacket entityVelocityPacket = EntityVelocityPacket.create(packet3);
                if (entityVelocityPacket != null && this.autoClickerCheck != null) {
                    this.autoClickerCheck.tickWithCheckTiming();
                }
            } else if (event.getPacketType() == Client.ABILITIES && this.miscellaneousCheck != null) {
                long value = this.lastTick;
                this.miscellaneousCheck.updateTime(value);
            }
        }
        if (event.getPacketType() == Client.LOOK || event.getPacketType() == Client.POSITION_LOOK) {
            float value2 = (Float) event.getPacket().getFloat().read(0);
            float value3 = (Float) event.getPacket().getFloat().read(1);
            if (this.aimbotCheck != null) {
                this.aimbotCheck.analyzeRotation(value3, value2);
            }
            if (this.miscellaneousCheck != null) {
                long value4 = this.lastTick;
                this.miscellaneousCheck.updateTime(value4, value3, value2);
            }
        }
        PacketContainer packet4 = event.getPacket();
        if (PacketReader.checkPacketReader(packet4)) {
            if (this.aimbotCheck != null) {
                this.aimbotCheck.tick();
            }
            if (this.autoClickerCheck != null) {
                this.autoClickerCheck.updateCount();
            }
            this.actionStateCheck.recordAttack();
        }
        if (event.getPacketType() == Client.ARM_ANIMATION && this.autoClickerCheck != null) {
            this.autoClickerCheck.tick();
        }
    }

    public void updateTime(long value) {
        this.lastTick++;
        if (this.movementCheck != null) {
            this.movementCheck.tick();
        }
        if (this.hitboxCheck != null) {
            this.hitboxCheck.tick();
        }
        this.actionStateCheck.updateTime(value);
    }
}
