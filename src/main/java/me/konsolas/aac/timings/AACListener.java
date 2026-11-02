package me.konsolas.aac.timings;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.ArrayList;
import java.util.List;
import me.konsolas.aac.compat.nms.MinecraftReflection;
import me.konsolas.aac.protocol.PacketDispatcher;
import me.konsolas.aac.protocol.PacketReader;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class AACListener extends PacketAdapter implements Listener {

    private final Class placeholderApiClass = MinecraftReflection.getCraftPlayerClass();

    private final PacketDispatcher dispatcher;

    public AACListener(PacketDispatcher dispatcher) {
        super(dispatcher.getPlugin(), ListenerPriority.LOW, getList(), new ListenerOptions[] { ListenerOptions.ASYNC });
        this.dispatcher = dispatcher;
    }

    private static List getList() {
        ArrayList values = new ArrayList();
        values.add(PacketReader.GROUND);
        values.add(Client.POSITION);
        values.add(Client.LOOK);
        values.add(Client.POSITION_LOOK);
        values.add(Server.POSITION);
        values.add(Server.ENTITY_VELOCITY);
        values.add(Server.BLOCK_CHANGE);
        values.add(Server.MULTI_BLOCK_CHANGE);
        values.add(Server.MAP_CHUNK);
        values.add(Client.ENTITY_ACTION);
        values.add(Client.BLOCK_DIG);
        values.add(Client.BLOCK_PLACE);
        values.add(Client.USE_ENTITY);
        values.add(Client.ARM_ANIMATION);
        values.add(Client.ABILITIES);
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_9;
        if (serverVersion.isAtLeast(version)) {
            values.add(Client.VEHICLE_MOVE);
            values.add(Client.USE_ITEM);
            values.add(Client.TELEPORT_ACCEPT);
        }
        return values;
    }

    public void onPacketReceiving(PacketEvent event) {
        if (this.placeholderApiClass.isInstance(event.getPlayer())) {
            this.dispatcher.handlePacket(event);
        }
    }

    public void onPacketSending(PacketEvent event) {
        if (this.placeholderApiClass.isInstance(event.getPlayer())) {
            this.dispatcher.handlePacket(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent blockPlaceEvent) {
        if (blockPlaceEvent.getClass() == BlockPlaceEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Player player = blockPlaceEvent.getPlayer();
            Player player2 = player;
            packetDispatcher.handleEvent(player2, blockPlaceEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamageEvent(BlockDamageEvent blockDamageEvent) {
        if (blockDamageEvent.getClass() == BlockDamageEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Player player = blockDamageEvent.getPlayer();
            Player player2 = player;
            packetDispatcher.handleEvent(player2, blockDamageEvent);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent blockBreakEvent) {
        if (blockBreakEvent.getClass() == BlockBreakEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Player player = blockBreakEvent.getPlayer();
            Player player2 = player;
            packetDispatcher.handleEvent(player2, blockBreakEvent);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getClass() == PlayerInteractEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Player player = playerInteractEvent.getPlayer();
            Player player2 = player;
            packetDispatcher.handleEvent(player2, playerInteractEvent);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent playerItemConsumeEvent) {
        if (playerItemConsumeEvent.getClass() == PlayerItemConsumeEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Player player = playerItemConsumeEvent.getPlayer();
            Player player2 = player;
            packetDispatcher.handleEvent(player2, playerItemConsumeEvent);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityShootBowEvent(EntityShootBowEvent entityShootBowEvent) {
        if (entityShootBowEvent.getEntity() instanceof Player && entityShootBowEvent.getClass() == EntityShootBowEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            LivingEntity entity = entityShootBowEvent.getEntity();
            Player player = (Player) entity;
            packetDispatcher.handleEvent(player, entityShootBowEvent);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMoveEvent(PlayerMoveEvent playerMoveEvent) {
        if (playerMoveEvent.getClass() == PlayerMoveEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Player player = playerMoveEvent.getPlayer();
            Player player2 = player;
            packetDispatcher.handleEvent(player2, playerMoveEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportEvent(PlayerTeleportEvent playerTeleportEvent) {
        PacketDispatcher packetDispatcher = this.dispatcher;
        Player player = playerTeleportEvent.getPlayer();
        Player player2 = player;
        packetDispatcher.handleEvent(player2, playerTeleportEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawnEvent(PlayerRespawnEvent playerRespawnEvent) {
        PacketDispatcher packetDispatcher = this.dispatcher;
        Player player = playerRespawnEvent.getPlayer();
        Player player2 = player;
        packetDispatcher.handleEvent(player2, playerRespawnEvent);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent entityRegainHealthEvent) {
        if (entityRegainHealthEvent.getEntity() instanceof Player && entityRegainHealthEvent.getClass() == EntityRegainHealthEvent.class) {
            PacketDispatcher packetDispatcher = this.dispatcher;
            Entity entity = entityRegainHealthEvent.getEntity();
            Player player = (Player) entity;
            packetDispatcher.handleEvent(player, entityRegainHealthEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent blockPistonExtendEvent) {
        BlockFace direction = blockPistonExtendEvent.getDirection();
        List blocks = blockPistonExtendEvent.getBlocks();
        Block block = blockPistonExtendEvent.getBlock();
        Block block2 = block;
        List values = blocks;
        this.updateBlockFace(direction, values, block2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent blockPistonRetractEvent) {
        BlockFace oppositeFace = blockPistonRetractEvent.getDirection().getOppositeFace();
        List blocks = blockPistonRetractEvent.getBlocks();
        Block block = blockPistonRetractEvent.getBlock();
        Block block2 = block;
        List values = blocks;
        this.updateBlockFace(oppositeFace, values, block2);
    }

    private void updateBlockFace(BlockFace blockFace, List<Block> values, Block block) {
        Vector vector = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
        for (Block block2 : values) {
            AxisAlignedBoundingBox box = AxisAlignedBoundingBox.forBlock(block2.getX() + blockFace.getModX(), block2.getY() + blockFace.getModY(), block2.getZ() + blockFace.getModZ());
            if (block2.getType() == Material.SLIME_BLOCK) {
                this.dispatcher.getSlimePushes().put(box, vector);
            } else {
                this.dispatcher.getPistonPushes().put(box, vector);
            }
        }
        this.dispatcher.getPistonPushes().put(AxisAlignedBoundingBox.forBlock(block.getX() + blockFace.getModX(), block.getY() + blockFace.getModY(), block.getZ() + blockFace.getModZ()), vector);
    }
}
