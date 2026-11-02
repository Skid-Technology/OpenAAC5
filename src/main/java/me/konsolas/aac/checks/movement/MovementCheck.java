package me.konsolas.aac.checks.movement;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import me.konsolas.aac.AAC;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.checks.BaseCheck;
import me.konsolas.aac.checks.ViolationBuffer;
import me.konsolas.aac.debug.DebugManager;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.movement.EntityState;
import me.konsolas.aac.movement.EntityView;
import me.konsolas.aac.movement.InputStateIterator;
import me.konsolas.aac.movement.KnockbackState;
import me.konsolas.aac.movement.MovementEnvironment;
import me.konsolas.aac.movement.MovementFrame;
import me.konsolas.aac.movement.MovementState;
import me.konsolas.aac.movement.PlayerInputState;
import me.konsolas.aac.movement.TimedVelocity;
import me.konsolas.aac.protocol.BlockInteraction;
import me.konsolas.aac.protocol.InteractionHand;
import me.konsolas.aac.protocol.PacketReader;
import me.konsolas.aac.protocol.ProtocolVersion;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import me.konsolas.aac.util.math.MinecraftMath;
import me.konsolas.aac.util.time.ExpiringCounter;
import me.konsolas.aac.world.MaterialAlias;
import me.konsolas.aac.world.collision.BlockCollisionCollector;
import me.konsolas.aac.world.collision.CollisionBounds;
import me.konsolas.aac.world.collision.CollisionResolver;
import me.konsolas.aac.world.fluid.FluidType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class MovementCheck extends BaseCheck {

    private final MovementState movementState;

    private MovementEnvironment environment;

    private boolean initialized;

    private Location lastSafeLocation;

    private float fallDistance;

    private Location lastLocation;

    private final AtomicInteger pendingTeleports;

    private volatile boolean vehicleMove;

    private volatile boolean teleporting;

    private final Deque velocityQueue;

    private Location teleportLocation;

    private EntityView entityView;

    private MovementFrame previousFrame;

    private Vector velocity;

    private AxisAlignedBoundingBox boundingBox;

    private int airTicks;

    private boolean onGround;

    private int ticks;

    private boolean wasOnGround;

    private int groundTicks;

    private int lastVelocityTick;

    private int vehicleTicks;

    private long lastMove;

    private final ExpiringCounter velocityTimer;

    private volatile KnockbackState knockbackState;

    private volatile boolean mitigating;

    private volatile long lastVehicleMove;

    private int waterTicks;

    private int lavaTicks;

    private int elytraTicks;

    private final ViolationBuffer timerBuffer;

    private final ViolationBuffer movementBuffer;

    private final float lastFallDistance;

    private final boolean wasFlying;

    private final long lastTeleport;

    private final boolean wasInVehicle;

    private final boolean colliding;

    private final boolean exempt;

    private final double horizontalSpeed;

    private final double verticalSpeed;

    private final double previousHorizontalSpeed;

    private final double previousVerticalSpeed;

    private final double expectedHorizontalSpeed;

    private final double expectedVerticalSpeed;

    private final double accumulatedHorizontalError;

    private final double accumulatedVerticalError;

    public MovementCheck(AAC aacPlugin, Player player) {
        super(aacPlugin, player, ("move"));
        this.movementState = new MovementState();
        this.pendingTeleports = new AtomicInteger();
        this.velocityQueue = new ArrayDeque();
        this.velocityTimer = new ExpiringCounter();
        this.waterTicks = 0;
        this.lavaTicks = 0;
        this.elytraTicks = 0;
        ConfigurationSection configuration = this.getConfiguration();
        ConfigurationSection section = configuration;
        this.timerBuffer = ViolationBuffer.create(section, (String) "vl");
        configuration = this.getConfiguration();
        ConfigurationSection section2 = configuration;
        this.movementBuffer = ViolationBuffer.create(section2, (String) "timer");
        this.lastFallDistance = (float) this.getConfiguration().getDouble("phase_vl");
        this.wasFlying = this.getConfiguration().getBoolean("block_nofall");
        this.lastTeleport = (long) (this.getConfiguration().getDouble("max_vel_time") * 1000.0);
        this.wasInVehicle = this.getConfiguration().getBoolean("check_sneak");
        this.colliding = this.getConfiguration().getBoolean("check_item_use");
        this.exempt = this.getConfiguration().getBoolean("check_flying");
        this.horizontalSpeed = this.getConfiguration().getDouble("flowing_speed");
        this.verticalSpeed = this.getConfiguration().getDouble("bubble_column_speed");
        this.previousHorizontalSpeed = this.getConfiguration().getDouble("bumping_speed");
        this.previousVerticalSpeed = this.getConfiguration().getDouble("elytra_rocket_speed");
        this.expectedHorizontalSpeed = this.getConfiguration().getDouble("riptide_speed");
        this.expectedVerticalSpeed = this.getConfiguration().getDouble("boat_hitbox_tolerance");
        this.accumulatedHorizontalError = this.getConfiguration().getDouble("shulker_hitbox_tolerance");
        this.accumulatedVerticalError = this.getConfiguration().getDouble("elytra_landing_tolerance");
        this.tick();
    }

    public void handlePacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        PacketType type = packet.getType();
        if (type == Server.BLOCK_CHANGE) {
            if (this.initialized && this.environment != null) {
                BlockPosition blockPosition = (BlockPosition) packet.getBlockPositionModifier().read(0);
                MovementEnvironment movementEnvironment = this.environment;
                int x = blockPosition.getX();
                int z = blockPosition.getZ();
                int y = blockPosition.getY();
                int value = x;
                if (movementEnvironment.containsBlock(value, y, z)) {
                    this.initialized = false;
                }
            }
        } else if (type != Server.MAP_CHUNK && type != Server.MULTI_BLOCK_CHANGE) {
            if (PacketReader.checkPacketReader(type) || type == PacketReader.VEHICLE_MOVE) {
                this.handlePacketWithKnockbackState(event);
            } else if (type == Client.ABILITIES) {
                ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
                ProtocolVersion version = ProtocolVersion.V1_16;
                boolean matches = serverVersion.isAtLeast(version) ? (Boolean) event.getPacket().getBooleans().read(0) : (Boolean) event.getPacket().getBooleans().read(1);
                if (matches) {
                    if (this.getPlayer().getAllowFlight()) {
                        this.movementState.deferFlightState(10, true);
                        DebugManager debugManager = this.getPlugin().getDebugManager();
                        Player player = this.getPlayer();
                        Player player2 = player;
                        if (debugManager.isDebugging(player2)) {
                            this.getPlugin().getDebugManager().log(this.getPlayer(), "move: started flying");
                        }
                    } else {
                        DebugManager debugManager2 = this.getPlugin().getDebugManager();
                        Player player3 = this.getPlayer();
                        Player player4 = player3;
                        if (debugManager2.isDebugging(player4)) {
                            this.getPlugin().getDebugManager().log(this.getPlayer(), "move: tried to start flying");
                        }
                    }
                } else {
                    this.movementState.deferFlightState(10, false);
                    DebugManager debugManager3 = this.getPlugin().getDebugManager();
                    Player player5 = this.getPlayer();
                    Player player6 = player5;
                    if (debugManager3.isDebugging(player6)) {
                        this.getPlugin().getDebugManager().log(this.getPlayer(), "move: stopped flying");
                    }
                }
            } else if (type == Client.ENTITY_ACTION) {
                ProtocolVersion serverVersion2 = ProtocolVersion.getServerVersion();
                ProtocolVersion version2 = ProtocolVersion.V1_9;
                if (!serverVersion2.isAtLeast(version2) && this.getPlayer().getVehicle() != null) {
                    return;
                }
                switch(((PlayerAction) packet.getPlayerActions().read(0))) {
                    case START_SNEAKING:
                    case STOP_SNEAKING:
                        this.movementState.deferSneakState(5);
                        break;
                    case START_FALL_FLYING:
                        if (this.movementState.isWearingElytra()) {
                            serverVersion2 = this.environment.getProtocolVersion();
                            ProtocolVersion version3 = ProtocolVersion.V1_9;
                            if (serverVersion2.isAtLeast(version3)) {
                                this.movementState.deferGlideState(2);
                            }
                        }
                }
            } else if (type == Client.USE_ENTITY) {
                this.pendingTeleports.getAndIncrement();
            } else if (type == Server.ENTITY_VELOCITY) {
                int value2 = (Integer) packet.getIntegers().read(0);
                LivingEntity livingEntity = this.movementState != null ? this.movementState.getLivingEntity() : null;
                if (livingEntity != null && value2 == livingEntity.getEntityId()) {
                    TimedVelocity timedVelocity = new TimedVelocity(PacketReader.findVector(packet), System.currentTimeMillis());
                    this.velocityQueue.add(timedVelocity);
                    this.onGround = true;
                    if (this.mitigating) {
                        this.teleporting = false;
                        this.mitigating = false;
                    } else {
                        this.teleporting = false;
                    }
                    DebugManager debugManager4 = this.getPlugin().getDebugManager();
                    Player player7 = this.getPlayer();
                    Player player8 = player7;
                    if (debugManager4.isDebugging(player8)) {
                        debugManager4 = this.getPlugin().getDebugManager();
                        Player player9 = this.getPlayer();
                        StringBuilder stringBuilder = new StringBuilder().append("move: velocity ");
                        Vector vector = timedVelocity.getVector();
                        Vector vector2 = vector;
                        debugManager4.log(player9, stringBuilder.append(findString(vector2)).toString());
                    }
                }
            } else if (type != Client.BLOCK_DIG && type != Client.BLOCK_PLACE && type != PacketReader.USE_ITEM) {
                if (type == Server.POSITION) {
                    float value3 = -0.05F;
                    this.movementBuffer.checkViolationBuffer(value3);
                }
            } else {
                if (type == Client.BLOCK_DIG) {
                    PlayerDigType playerDigType = (PlayerDigType) packet.getPlayerDigTypes().read(0);
                    if (playerDigType == PlayerDigType.STOP_DESTROY_BLOCK) {
                        BlockPosition blockPosition2 = (BlockPosition) packet.getBlockPositionModifier().read(0);
                        MovementEnvironment movementEnvironment2 = this.environment;
                        int x2 = blockPosition2.getX();
                        int y2 = blockPosition2.getY();
                        int z2 = blockPosition2.getZ();
                        int value4 = z2;
                        int value5 = y2;
                        int value6 = x2;
                        movementEnvironment2.invalidateBlock(value6, value5, value4);
                    } else if (playerDigType == PlayerDigType.RELEASE_USE_ITEM && this.movementState.isRiptideReady()) {
                        DebugManager debugManager5 = this.getPlugin().getDebugManager();
                        Player player10 = this.getPlayer();
                        Player player11 = player10;
                        if (debugManager5.isDebugging(player11)) {
                            debugManager5 = this.getPlugin().getDebugManager();
                            Player player12 = this.getPlayer();
                            StringBuilder stringBuilder2 = new StringBuilder().append("move: used riptide at ");
                            Vector vector3 = this.getPlayer().getLocation().toVector();
                            Vector vector4 = vector3;
                            debugManager5.log(player12, stringBuilder2.append(findString(vector4)).toString());
                        }
                        this.lavaTicks = 3;
                    }
                } else {
                    BlockInteraction blockInteraction = PacketReader.findBlockInteraction(packet);
                    if (blockInteraction != null) {
                        MovementEnvironment movementEnvironment3 = this.environment;
                        double x3 = blockInteraction.getPosition().getX();
                        double y3 = blockInteraction.getPosition().getY();
                        double z3 = blockInteraction.getPosition().getZ();
                        double value7 = z3;
                        double value8 = y3;
                        double value9 = x3;
                        Material material = movementEnvironment3.getMaterial(value9, value8, value7);
                        if (!material.isSolid()) {
                            DebugManager debugManager6 = this.getPlugin().getDebugManager();
                            Player player13 = this.getPlayer();
                            Player player14 = player13;
                            if (debugManager6.isDebugging(player14)) {
                                this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "move: can't build on: " + material + ": %d %d %d", new Object[] { blockInteraction.getPosition().getX(), blockInteraction.getPosition().getY(), blockInteraction.getPosition().getZ() });
                            }
                        } else {
                            ProtocolVersion serverVersion3 = ProtocolVersion.getServerVersion();
                            ProtocolVersion version4 = ProtocolVersion.V1_9;
                            Material material2;
                            if (serverVersion3.isAtLeast(version4)) {
                                if (blockInteraction.getHand() == InteractionHand.MAIN_HAND) {
                                    material2 = this.getPlayer().getInventory().getItemInMainHand().getType();
                                } else {
                                    material2 = this.getPlayer().getInventory().getItemInOffHand().getType();
                                }
                            } else {
                                material2 = this.getPlayer().getItemInHand().getType();
                            }
                            if (!material2.isBlock()) {
                                DebugManager debugManager7 = this.getPlugin().getDebugManager();
                                Player player15 = this.getPlayer();
                                Player player16 = player15;
                                if (debugManager7.isDebugging(player16)) {
                                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "move: can't place: " + material2 + ": %d %d %d", new Object[] { blockInteraction.getAdjacentPosition().getX(), blockInteraction.getAdjacentPosition().getY(), blockInteraction.getAdjacentPosition().getZ() });
                                }
                            } else {
                                this.boundingBox = AxisAlignedBoundingBox.forBlock(blockInteraction.getAdjacentPosition().getX(), blockInteraction.getAdjacentPosition().getY(), blockInteraction.getAdjacentPosition().getZ());
                                this.airTicks = 5;
                                DebugManager debugManager8 = this.getPlugin().getDebugManager();
                                Player player17 = this.getPlayer();
                                Player player18 = player17;
                                if (debugManager8.isDebugging(player18)) {
                                    this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "move: client placed: %d %d %d", new Object[] { blockInteraction.getAdjacentPosition().getX(), blockInteraction.getAdjacentPosition().getY(), blockInteraction.getAdjacentPosition().getZ() });
                                }
                            }
                        }
                    }
                }
                this.movementState.deferItemUseState(6);
            }
        } else if (this.initialized && this.environment != null) {
            AxisAlignedBoundingBox box;
            if (type == Server.MAP_CHUNK) {
                box = AxisAlignedBoundingBox.forChunk((Integer) packet.getIntegers().read(0), (Integer) packet.getIntegers().read(1));
            } else if (packet.getChunkCoordIntPairs().size() > 0) {
                ChunkCoordIntPair chunkCoordIntPair = (ChunkCoordIntPair) packet.getChunkCoordIntPairs().readSafely(0);
                box = AxisAlignedBoundingBox.forChunk(chunkCoordIntPair.getChunkX(), chunkCoordIntPair.getChunkZ());
            } else {
                BlockPosition blockPosition3 = (BlockPosition) packet.getSectionPositions().read(0);
                box = AxisAlignedBoundingBox.forChunkSection(blockPosition3.getX(), blockPosition3.getY(), blockPosition3.getZ());
            }
            AxisAlignedBoundingBox boundingBox2 = this.environment.getBoundingBox();
            AxisAlignedBoundingBox box2 = boundingBox2;
            if (box.intersects(box2)) {
                this.initialized = false;
            }
        }
    }

    public void handlePacketWithKnockbackState(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        if (this.knockbackState != null) {
            event.setCancelled(true);
        } else {
            Location location = this.lastLocation;
            Location location2 = PacketReader.findLocation(packet, location);
            if (Double.isFinite(location2.getX()) && Double.isFinite(location2.getY()) && Double.isFinite(location2.getZ()) && Float.isFinite(location2.getPitch()) && Float.isFinite(location2.getYaw())) {
                if (this.groundTicks != 0 && packet.getType() != PacketReader.VEHICLE_MOVE || !this.isMovement()) {
                    this.movementState.pollUpdateFlag();
                    Location location3 = this.teleportLocation.clone();
                    Vector vector = location3.toVector();
                    Vector vector2 = location2.toVector().subtract(this.lastLocation.toVector());
                    Vector vector3 = location2.toVector().subtract(location3.toVector());
                    boolean matches = packet.getType() == Client.POSITION || packet.getType() == Client.POSITION_LOOK || packet.getType() == PacketReader.VEHICLE_MOVE;
                    boolean matches2 = packet.getType() == PacketReader.VEHICLE_MOVE;
                    this.lastLocation = location2;
                    if (!this.exempt && this.getPlayer().getAllowFlight()) {
                        this.onGround = false;
                        this.teleportLocation = this.lastLocation;
                        this.previousFrame = null;
                        this.ticks = 0;
                    } else {
                        if (matches2 && this.groundTicks > 0) {
                            this.groundTicks++;
                        }
                        if (!matches || (this.groundTicks > 5 || this.groundTicks <= 0) && !this.movementState.isUnsupportedEntity()) {
                            if (this.groundTicks > 0) {
                                if (!matches) {
                                    return;
                                }
                                ProtocolVersion protocolVersion = this.environment.getProtocolVersion();
                                ProtocolVersion version = ProtocolVersion.V1_9;
                                if (!protocolVersion.isAtLeast(version)) {
                                    return;
                                }
                            }
                            boolean matches3 = this.groundTicks == 0 && (Boolean) packet.getBooleans().read(0);
                            if (this.movementState.getVehicleType() == EntityType.PLAYER) {
                                DebugManager debugManager = this.getPlugin().getDebugManager();
                                Player player = this.getPlayer();
                                Player player2 = player;
                                if (debugManager.isDebugging(player2)) {
                                    this.getPlugin().getDebugManager().log(this.getPlayer(), "move: riding " + this.movementState.getVehicleType());
                                }
                            } else if (!matches2 || !this.checkMovement(event, vector2)) {
                                MovementEnvironment movementEnvironment = this.environment;
                                MovementState movementState2 = this.movementState;
                                EntityView entityView2 = this.entityView;
                                this.entityView = new EntityState(vector, entityView2, movementState2, movementEnvironment);
                                CollisionBounds collisionBounds = new CollisionBounds(this.environment.getProtocolVersion(), null);
                                CollisionBounds collisionBounds2 = this.boundingBox != null ? new CollisionBounds(this.environment.getProtocolVersion(), this.boundingBox) : null;
                                if (this.boundingBox != null && --this.airTicks <= 0) {
                                    this.boundingBox = null;
                                }
                                if (this.previousFrame == null) {
                                    boolean matches4 = this.wasOnGround;
                                    EntityView entityView3 = this.entityView;
                                    this.previousFrame = collisionBounds.findMovementFrame(entityView3, location3, matches4);
                                }
                                boolean matches5;
                                int value2;
                                matches5 = this.previousFrame.getMovementDelta().isHorizontalCollision() || this.entityView.isInWater() || this.entityView.isInLava() || this.entityView.isClimbing() || this.movementState.getFlyingState() == null || this.movementState.getFlyingState();
                                value2 = this.pendingTeleports.getAndSet(0);
                                decision: if (vector2.getY() >= 0.29900001192092895) {
                                    decision2: {
                                        if (this.entityView.isPreviousInWater()) {
                                            MovementEnvironment movementEnvironment2 = this.environment;
                                            AxisAlignedBoundingBox box = this.entityView.getBoundingBox().offset(this.previousFrame.getMovementDelta().getActualMovement()).offset(0.0, 0.6, 0.0);
                                            Predicate predicate = MaterialAlias.WATER::contains;
                                            Predicate predicate2 = predicate;
                                            AxisAlignedBoundingBox box2 = box;
                                            if (!movementEnvironment2.checkMovementEnvironment(box2, predicate2)) {
                                                break decision2;
                                            }
                                        }
                                        if (!this.entityView.isPreviousInLava()) {
                                            break decision;
                                        }
                                        MovementEnvironment movementEnvironment3 = this.environment;
                                        AxisAlignedBoundingBox box3 = this.entityView.getBoundingBox().offset(this.previousFrame.getMovementDelta().getActualMovement()).offset(0.0, 0.6, 0.0);
                                        Predicate predicate3 = MaterialAlias.LAVA::contains;
                                        Predicate predicate4 = predicate3;
                                        AxisAlignedBoundingBox box4 = box3;
                                        if (movementEnvironment3.checkMovementEnvironment(box4, predicate4)) {
                                            break decision;
                                        }
                                    }
                                    MovementEnvironment movementEnvironment4 = this.environment;
                                    AxisAlignedBoundingBox boundingBox2 = this.entityView.getBoundingBox();
                                    AxisAlignedBoundingBox box5 = boundingBox2;
                                    if (movementEnvironment4.checkMovementEnvironment(box5)) {
                                        this.previousFrame = this.previousFrame.copy();
                                    }
                                }
                                decision3: {
                                    if (this.groundTicks > 0) {
                                        MovementEnvironment movementEnvironment5 = this.environment;
                                        AxisAlignedBoundingBox box6 = this.entityView.getBoundingBox().offset(0.0, 1.0, 0.0);
                                        if (movementEnvironment5.collides(box6)) {
                                            this.elytraTicks = 10;
                                            break decision3;
                                        }
                                    }
                                    this.elytraTicks--;
                                }
                                MovementEnvironment movementEnvironment6 = this.environment;
                                AxisAlignedBoundingBox box7 = this.entityView.getBoundingBox().offset(vector2);
                                boolean matches6 = movementEnvironment6.evaluateCheckMovementEnvironment(box7);
                                PlayerInputState input = this.previousFrame.getInput();
                                float pitch = location2.getPitch();
                                float yaw = location2.getYaw();
                                boolean matches7 = this.movementState.canSprint();
                                Boolean matches8 = this.wasInVehicle ? this.movementState.getStableSneaking() : null;
                                Boolean matches9 = this.colliding ? this.movementState.getStableUsingItem() : null;
                                Boolean stableGliding = this.movementState.getStableGliding();
                                Boolean flyingState = this.movementState.getFlyingState();
                                Boolean matches10 = stableGliding;
                                Boolean matches11 = matches9;
                                Boolean matches12 = matches8;
                                boolean matches13 = matches7;
                                float value4 = yaw;
                                float value5 = pitch;
                                PlayerInputState playerInputState = input;
                                InputStateIterator listWindowIterator = new InputStateIterator(playerInputState, value5, value4, matches5, value2, matches13, matches12, matches11, matches10, flyingState);
                                boolean matches14 = false;
                                MovementFrame frame = null;
                                MovementFrame frame2 = null;
                                MovementFrame frame3 = null;
                                double value6 = Double.POSITIVE_INFINITY;
                                double value7 = Double.POSITIVE_INFINITY;
                                boolean matches15 = false;
                                long now = System.currentTimeMillis();
                                if (this.onGround && !this.velocityQueue.isEmpty() && now - ((TimedVelocity) this.velocityQueue.element()).getTimestamp() > this.lastTeleport) {
                                    this.teleporting = true;
                                }
                                while (listWindowIterator.hasNext()) {
                                    PlayerInputState playerInputState2 = listWindowIterator.getPlayerInputState();
                                    boolean matches16 = !this.teleporting || this.velocityQueue.isEmpty();
                                    MovementFrame frame4;
                                    if (this.groundTicks <= 0 && !matches16) {
                                        frame4 = null;
                                    } else {
                                        MovementFrame frame5 = this.previousFrame;
                                        MovementEnvironment movementEnvironment7 = this.environment;
                                        EntityView entityView4 = this.entityView;
                                        frame4 = collisionBounds.findMovementFrame(vector, entityView4, movementEnvironment7, frame5, playerInputState2, (TimedVelocity) (Object) null);
                                    }
                                    MovementFrame frame6 = frame4;
                                    MovementFrame frame7;
                                    if (!this.velocityQueue.isEmpty()) {
                                        EntityView entityView5 = this.entityView;
                                        MovementEnvironment movementEnvironment8 = this.environment;
                                        MovementFrame frame8 = this.previousFrame;
                                        TimedVelocity timedVelocity = (TimedVelocity) this.velocityQueue.element();
                                        MovementFrame frame9 = frame8;
                                        MovementEnvironment movementEnvironment9 = movementEnvironment8;
                                        EntityView entityView6 = entityView5;
                                        frame7 = collisionBounds.findMovementFrame(vector, entityView6, movementEnvironment9, frame9, playerInputState2, timedVelocity);
                                    } else {
                                        frame7 = null;
                                    }
                                    MovementFrame frame10 = frame7;
                                    MovementFrame frame11;
                                    if (collisionBounds2 != null) {
                                        MovementFrame frame12 = this.previousFrame;
                                        MovementEnvironment movementEnvironment10 = this.environment;
                                        EntityView entityView7 = this.entityView;
                                        frame11 = collisionBounds2.findMovementFrame(vector, entityView7, movementEnvironment10, frame12, playerInputState2, (TimedVelocity) (Object) null);
                                    } else {
                                        frame11 = null;
                                    }
                                    MovementFrame frame13 = frame11;
                                    if (this.groundTicks > 0) {
                                        assert frame6 != null;
                                        if (playerInputState2.isJumping() && vector2.getY() > 0.0 && frame6.getMovementDelta().getExpectedMovement().getY() + 0.001 > vector2.getY() && Math.abs(frame6.getMovementDelta().getExpectedMovement().getX()) + 0.001 > Math.abs(vector2.getX()) && Math.abs(frame6.getMovementDelta().getExpectedMovement().getZ()) + 0.001 > Math.abs(vector2.getZ()) && !matches6) {
                                            frame6 = frame6.withVector(vector2);
                                        }
                                    }
                                    double value8 = frame6 != null ? frame6.getMovementDelta().getExpectedMovement().distanceSquared(vector3) : Double.POSITIVE_INFINITY;
                                    double value9 = frame10 != null ? frame10.getMovementDelta().getExpectedMovement().distanceSquared(vector3) : Double.POSITIVE_INFINITY;
                                    double value10 = frame13 != null ? frame13.getMovementDelta().getExpectedMovement().distanceSquared(vector3) : Double.POSITIVE_INFINITY;
                                    boolean matches17 = frame6 != null && frame6.getMovementDelta().getExpectedMovement().lengthSquared() <= 9.0E-4;
                                    boolean matches18 = frame10 != null && frame10.getMovementDelta().getExpectedMovement().lengthSquared() <= 9.0E-4;
                                    boolean matches19 = frame13 != null && frame13.getMovementDelta().getExpectedMovement().lengthSquared() <= 9.0E-4;
                                    if (matches18) {
                                        this.onGround = false;
                                    }
                                    boolean matches20 = value8 <= 1.0E-6 && (matches3 == frame6.getMovementDelta().isHorizontalCollision() || matches2) || !matches && matches17;
                                    boolean matches21 = value9 <= 1.0E-6 && (matches3 == frame10.getMovementDelta().isHorizontalCollision() || matches2) || !matches && matches18;
                                    boolean matches22 = value10 <= 1.0E-6 && (matches3 == frame13.getMovementDelta().isHorizontalCollision() || matches2) || !matches && matches19;
                                    if (matches20 || matches21 || matches22) {
                                        if (matches21) {
                                            this.previousFrame = frame10;
                                            this.onGround = false;
                                            if (!matches20 || this.teleporting || now - ((TimedVelocity) this.velocityQueue.element()).getTimestamp() > this.lastTeleport) {
                                                this.velocityQueue.pop();
                                            }
                                        } else if (matches20) {
                                            this.previousFrame = frame6;
                                        } else {
                                            this.previousFrame = frame13;
                                        }
                                        assert this.previousFrame != null;
                                        if (this.previousFrame.getMovementDelta().getActualMovement().distanceSquared(vector2) < 1.0E-6) {
                                            this.previousFrame = this.previousFrame.withVector(vector2);
                                        }
                                        if (this.previousFrame.getInput().isElytra()) {
                                            if (this.previousFrame.getMovementDelta().isHorizontalCollision()) {
                                                this.movementState.deferGlideState(10);
                                            }
                                        } else if (this.movementState.getStableGliding() == null) {
                                            this.lastVelocityTick = 2;
                                            this.movementState.resetGlideTransition();
                                        }
                                        EntityView entityView8 = this.entityView;
                                        MovementEnvironment movementEnvironment11 = this.environment;
                                        MovementFrame frame14 = this.previousFrame;
                                        PlayerInputState input2 = this.previousFrame.getInput();
                                        MovementFrame frame15 = this.previousFrame;
                                        EntityView entityView9 = this.entityView;
                                        double y = collisionBounds.getY(entityView9, frame15);
                                        PlayerInputState playerInputState3 = input2;
                                        MovementFrame frame16 = frame14;
                                        MovementEnvironment movementEnvironment12 = movementEnvironment11;
                                        EntityView entityView10 = entityView8;
                                        this.velocity = collisionBounds.findVector(entityView10, movementEnvironment12, frame16, playerInputState3, (TimedVelocity) (Object) null, y);
                                        if (matches) {
                                            this.teleportLocation = this.lastLocation;
                                            this.ticks = -1;
                                        } else {
                                            this.teleportLocation = location3.toVector().add(this.previousFrame.getMovementDelta().getExpectedMovement()).toLocation(this.getPlayer().getWorld(), location2.getYaw(), location2.getPitch());
                                            this.ticks = 0;
                                        }
                                        DebugManager debugManager2 = this.getPlugin().getDebugManager();
                                        Player player3 = this.getPlayer();
                                        Player player4 = player3;
                                        if (debugManager2.isDebugging(player4) && vector2.lengthSquared() != 0.0) {
                                            int value11 = matches21 ? 118 : (matches20 ? 45 : 109);
                                            DebugManager debugManager3 = this.getPlugin().getDebugManager();
                                            Player player5 = this.getPlayer();
                                            StringBuilder stringBuilder = new StringBuilder().append("move: ").append((char) value11).append(" ").append(playerInputState2).append(" / ");
                                            Vector expectedMovement = this.previousFrame.getMovementDelta().getExpectedMovement();
                                            Vector vector4 = expectedMovement;
                                            debugManager3.log(player5, stringBuilder.append(findString(vector4)).toString());
                                        }
                                        this.wasOnGround = this.previousFrame.getMovementDelta().isHorizontalCollision();
                                        if (this.groundTicks == 0) {
                                            MovementFrame frame17 = this.previousFrame;
                                            this.updateMovementFrame(frame17, matches3, packet, (boolean) true);
                                        }
                                        return;
                                    }
                                    matches15 = matches15 || frame6 != null && frame13 != null && AxisAlignedBoundingBox.enclosing(frame6.getMovementDelta().getExpectedMovement(), frame13.getMovementDelta().getExpectedMovement()).expand(0.001, 0.001, 0.001).contains(vector3);
                                    if (value8 < value6) {
                                        frame2 = frame6;
                                        value6 = value8;
                                    }
                                    if (value9 < value7) {
                                        frame3 = frame10;
                                        value7 = value9;
                                    }
                                    if (!matches14) {
                                        if (matches17) {
                                            frame = frame6;
                                            matches14 = true;
                                        } else if (matches18) {
                                            frame = frame10;
                                            matches14 = true;
                                        }
                                    }
                                }
                                if (!this.velocityQueue.isEmpty() && (this.teleporting || now - ((TimedVelocity) this.velocityQueue.element()).getTimestamp() > this.lastTeleport)) {
                                    this.velocityQueue.remove();
                                    DebugManager debugManager4 = this.getPlugin().getDebugManager();
                                    Player player6 = this.getPlayer();
                                    Player player7 = player6;
                                    if (debugManager4.isDebugging(player7)) {
                                        this.getPlugin().getDebugManager().log(this.getPlayer(), "move: cleared modifier");
                                    }
                                }
                                if (matches14 && value7 < 0.0144 && !this.teleporting) {
                                    this.onGround = false;
                                    this.teleportLocation = this.lastLocation;
                                    this.previousFrame = frame3;
                                    EntityView entityView11 = this.entityView;
                                    MovementEnvironment movementEnvironment13 = this.environment;
                                    MovementFrame frame18 = this.previousFrame;
                                    PlayerInputState input3 = this.previousFrame.getInput();
                                    MovementFrame frame19 = this.previousFrame;
                                    EntityView entityView12 = this.entityView;
                                    double y2 = collisionBounds.getY(entityView12, frame19);
                                    PlayerInputState playerInputState4 = input3;
                                    MovementFrame frame20 = frame18;
                                    MovementEnvironment movementEnvironment14 = movementEnvironment13;
                                    EntityView entityView13 = entityView11;
                                    this.velocity = collisionBounds.findVector(entityView13, movementEnvironment14, frame20, playerInputState4, (TimedVelocity) (Object) null, y2);
                                    this.wasOnGround = this.previousFrame.getMovementDelta().isHorizontalCollision();
                                    this.ticks = 0;
                                    this.velocityQueue.poll();
                                    DebugManager debugManager5 = this.getPlugin().getDebugManager();
                                    Player player8 = this.getPlayer();
                                    Player player9 = player8;
                                    if (debugManager5.isDebugging(player9)) {
                                        DebugManager debugManager6 = this.getPlugin().getDebugManager();
                                        Player player10 = this.getPlayer();
                                        StringBuilder stringBuilder2 = new StringBuilder().append("move: f ").append(this.previousFrame.getInput()).append(" / ");
                                        Vector expectedMovement2 = this.previousFrame.getMovementDelta().getExpectedMovement();
                                        Vector vector5 = expectedMovement2;
                                        debugManager6.log(player10, stringBuilder2.append(findString(vector5)).append(" actual:").append(findString(vector2)).toString());
                                    }
                                    if (this.groundTicks == 0) {
                                        MovementFrame frame21 = this.previousFrame;
                                        this.updateMovementFrame(frame21, matches3, packet, (boolean) false);
                                    }
                                } else {
                                    MovementFrame frame22 = Objects.requireNonNull(frame3 != null ? frame3 : frame2);
                                    int value12 = -1;
                                    if (!matches15) {
                                        EntityView entityView14 = this.entityView;
                                        if ((value12 = this.calculateCount(entityView14, vector2, frame22, value6)) < 0) {
                                            EntityView entityView15 = this.entityView;
                                            if (this.checkMovement(entityView15, vector2, frame22)) {
                                                this.onGround = false;
                                                this.teleportLocation = this.lastLocation;
                                                DebugManager debugManager7 = this.getPlugin().getDebugManager();
                                                Player player11 = this.getPlayer();
                                                Player player12 = player11;
                                                if (debugManager7.isDebugging(player12)) {
                                                    DebugManager debugManager8 = this.getPlugin().getDebugManager();
                                                    Player player13 = this.getPlayer();
                                                    StringBuilder stringBuilder3 = new StringBuilder().append("move: P ").append(this.previousFrame.getInput()).append(" / ");
                                                    Vector expectedMovement3 = this.previousFrame.getMovementDelta().getExpectedMovement();
                                                    Vector vector6 = expectedMovement3;
                                                    debugManager8.log(player13, stringBuilder3.append(findString(vector6)).append(" actual:").append(findString(vector2)).toString());
                                                }
                                                this.previousFrame = frame22.withVector(vector2);
                                                EntityView entityView16 = this.entityView;
                                                MovementEnvironment movementEnvironment15 = this.environment;
                                                MovementFrame frame23 = this.previousFrame;
                                                PlayerInputState input4 = this.previousFrame.getInput();
                                                MovementFrame frame24 = this.previousFrame;
                                                EntityView entityView17 = this.entityView;
                                                double y3 = collisionBounds.getY(entityView17, frame24);
                                                PlayerInputState playerInputState5 = input4;
                                                MovementFrame frame25 = frame23;
                                                MovementEnvironment movementEnvironment16 = movementEnvironment15;
                                                EntityView entityView18 = entityView16;
                                                this.velocity = collisionBounds.findVector(entityView18, movementEnvironment16, frame25, playerInputState5, (TimedVelocity) (Object) null, y3);
                                                this.wasOnGround = this.previousFrame.getMovementDelta().isHorizontalCollision();
                                                this.ticks = 0;
                                                return;
                                            }
                                            if (frame2 != null && (this.ticks >= 0 || matches14)) {
                                                MovementFrame frame26;
                                                double value13;
                                                if (!(Math.abs(vector3.getY() - frame2.getMovementDelta().getExpectedMovement().getY()) > 0.001)) {
                                                    value13 = value6;
                                                    frame26 = frame2.withVector(vector2);
                                                } else {
                                                    double value14 = value6;
                                                    MovementFrame frame27 = frame2;
                                                    MovementFrame frame28 = frame2;
                                                    PlayerInputState input5 = frame2.getInput();
                                                    Vector vector7 = vector.clone();
                                                    Vector vector8 = vector3.clone();
                                                    Object result = this.entityView;
                                                    for (int index = 1; index < 3; index++) {
                                                        Vector expectedMovement4 = frame27.getMovementDelta().getExpectedMovement();
                                                        vector7.add(expectedMovement4);
                                                        vector8.subtract(expectedMovement4);
                                                        MovementEnvironment movementEnvironment17 = this.environment;
                                                        MovementState movementState3 = this.movementState;
                                                        result = new EntityState(vector7, (EntityView) result, movementState3, movementEnvironment17);
                                                        MovementEnvironment movementEnvironment18 = this.environment;
                                                        frame27 = collisionBounds.findMovementFrame(vector7, (EntityView) result, movementEnvironment18, frame27, input5, (TimedVelocity) (Object) null);
                                                        if (frame27.getMovementDelta().getExpectedMovement().getY() == 0.0) {
                                                            break;
                                                        }
                                                        double value15 = frame27.getMovementDelta().getExpectedMovement().distanceSquared(vector8);
                                                        if (value15 < value14) {
                                                            value14 = value15;
                                                            frame28 = frame27;
                                                        }
                                                    }
                                                    value13 = value14;
                                                    frame26 = frame28;
                                                }
                                                if (value13 < 0.0144) {
                                                    if (matches6) {
                                                        frame26 = frame26.withValue(0.0);
                                                    }
                                                    this.teleportLocation = this.lastLocation;
                                                    this.previousFrame = frame26;
                                                    EntityView entityView19 = this.entityView;
                                                    MovementEnvironment movementEnvironment19 = this.environment;
                                                    MovementFrame frame29 = this.previousFrame;
                                                    PlayerInputState input6 = this.previousFrame.getInput();
                                                    MovementFrame frame30 = this.previousFrame;
                                                    EntityView entityView20 = this.entityView;
                                                    double y4 = collisionBounds.getY(entityView20, frame30);
                                                    PlayerInputState playerInputState6 = input6;
                                                    MovementFrame frame31 = frame29;
                                                    MovementEnvironment movementEnvironment20 = movementEnvironment19;
                                                    EntityView entityView21 = entityView19;
                                                    this.velocity = collisionBounds.findVector(entityView21, movementEnvironment20, frame31, playerInputState6, (TimedVelocity) (Object) null, y4);
                                                    this.wasOnGround = this.previousFrame.getMovementDelta().isHorizontalCollision();
                                                    this.ticks++;
                                                    if (matches14) {
                                                        this.ticks = 0;
                                                    } else if (this.ticks > 2) {
                                                        this.ticks = -1;
                                                    }
                                                    DebugManager debugManager9 = this.getPlugin().getDebugManager();
                                                    Player player14 = this.getPlayer();
                                                    Player player15 = player14;
                                                    if (debugManager9.isDebugging(player15)) {
                                                        StringBuilder stringBuilder4 = new StringBuilder().append("move: . ").append(this.previousFrame.getInput()).append(" / ");
                                                        Vector expectedMovement5 = frame2.getMovementDelta().getExpectedMovement();
                                                        Vector vector9 = expectedMovement5;
                                                        String text = stringBuilder4.append(findString(vector9)).append(" actual:").append(findString(vector2)).append(" d ").append(Math.sqrt(value13)).toString();
                                                        if (frame != null) {
                                                            StringBuilder stringBuilder5 = new StringBuilder().append(text).append(" ").append(frame.getInput()).append(" ");
                                                            Vector expectedMovement6 = frame.getMovementDelta().getExpectedMovement();
                                                            Vector vector10 = expectedMovement6;
                                                            text = stringBuilder5.append(findString(vector10)).toString();
                                                            if (frame.getVelocity() != null) {
                                                                StringBuilder stringBuilder6 = new StringBuilder().append(text).append(" v ");
                                                                Vector vector11 = frame.getVelocity().getVector();
                                                                Vector vector12 = vector11;
                                                                text = stringBuilder6.append(findString(vector12)).toString();
                                                            }
                                                        }
                                                        this.getPlugin().getDebugManager().log(this.getPlayer(), text);
                                                    }
                                                    if (this.groundTicks == 0) {
                                                        MovementFrame frame32 = this.previousFrame;
                                                        this.updateMovementFrame(frame32, matches3, packet, (boolean) true);
                                                    }
                                                    return;
                                                }
                                            }
                                            AxisAlignedBoundingBox box8 = this.entityView.getBoundingBox().expand(-0.001, -0.001, -0.001);
                                            AxisAlignedBoundingBox box9 = this.entityView.getBoundingBox().offset(vector2).expand(-0.001, -0.001, -0.001);
                                            boolean matches23 = !box8.intersects(box9) || this.environment.collides(box9);
                                            DebugManager debugManager10 = this.getPlugin().getDebugManager();
                                            Player player16 = this.getPlayer();
                                            Player player17 = player16;
                                            if (debugManager10.isDebugging(player17)) {
                                                DebugManager debugManager11 = this.getPlugin().getDebugManager();
                                                Player player18 = this.getPlayer();
                                                StringBuilder stringBuilder7 = new StringBuilder().append("move: ! ").append(frame22.getInput()).append(" / ");
                                                Vector expectedMovement7 = frame22.getMovementDelta().getExpectedMovement();
                                                Vector vector13 = expectedMovement7;
                                                debugManager11.log(player18, stringBuilder7.append(findString(vector13)).append(" actual:").append(findString(vector2)).append(matches23 ? " phase" : "").toString());
                                            }
                                            float value17 = (float) Math.sqrt((float) Math.min(value6, value7)) + (matches23 ? this.lastFallDistance : 0.0F);
                                            if (this.timerBuffer.checkViolationBuffer(value17) && !this.vehicleMove) {
                                                Location location4 = this.teleportLocation.clone().add(frame22.getMovementDelta().getExpectedMovement());
                                                EntityView entityView22 = this.entityView;
                                                MovementEnvironment movementEnvironment21 = this.environment;
                                                PlayerInputState input7 = frame22.getInput();
                                                EntityView entityView23 = this.entityView;
                                                double y5 = collisionBounds.getY(entityView23, frame22);
                                                PlayerInputState playerInputState7 = input7;
                                                MovementEnvironment movementEnvironment22 = movementEnvironment21;
                                                EntityView entityView24 = entityView22;
                                                Vector vector14 = collisionBounds.findVector(entityView24, movementEnvironment22, frame22, playerInputState7, (TimedVelocity) (Object) null, y5);
                                                float value18 = (float) (this.lastSafeLocation.getY() - location4.getY()) + this.fallDistance;
                                                this.knockbackState = new KnockbackState(this.teleportLocation, location4, vector14, value18);
                                                this.wasOnGround = frame22.getMovementDelta().isHorizontalCollision();
                                                event.setCancelled(true);
                                            } else {
                                                this.teleportLocation = this.lastLocation;
                                                this.previousFrame = frame22.withVector(vector2);
                                                this.wasOnGround = frame22.getMovementDelta().isHorizontalCollision();
                                            }
                                            if (this.groundTicks == 0) {
                                                MovementFrame frame33 = this.previousFrame;
                                                this.updateMovementFrame(frame33, matches3, packet, (boolean) true);
                                            }
                                            if (this.timerBuffer.getLevel() >= 1.0F) {
                                                boolean matches24 = frame2 == null && !this.teleporting;
                                                EntityView entityView25 = this.entityView;
                                                this.updateValue(value17, entityView25, vector2, frame22, matches24);
                                            }
                                            return;
                                        }
                                    }
                                    this.onGround = false;
                                    this.teleportLocation = this.lastLocation;
                                    DebugManager debugManager12 = this.getPlugin().getDebugManager();
                                    Player player19 = this.getPlayer();
                                    Player player20 = player19;
                                    if (debugManager12.isDebugging(player20)) {
                                        String text2 = matches15 ? "b" : "s" + value12;
                                        DebugManager debugManager13 = this.getPlugin().getDebugManager();
                                        Player player21 = this.getPlayer();
                                        StringBuilder stringBuilder8 = new StringBuilder().append("move: ").append(text2).append(" ").append(this.previousFrame.getInput()).append(" / ");
                                        Vector expectedMovement8 = this.previousFrame.getMovementDelta().getExpectedMovement();
                                        Vector vector15 = expectedMovement8;
                                        debugManager13.log(player21, stringBuilder8.append(findString(vector15)).append(" actual:").append(findString(vector2)).toString());
                                    }
                                    frame22 = frame22.withVector(vector2);
                                    if (matches6) {
                                        frame22 = frame22.withValue(0.0).copyWithInput();
                                    }
                                    this.previousFrame = frame22;
                                    EntityView entityView26 = this.entityView;
                                    MovementEnvironment movementEnvironment23 = this.environment;
                                    MovementFrame frame34 = this.previousFrame;
                                    PlayerInputState input8 = this.previousFrame.getInput();
                                    MovementFrame frame35 = this.previousFrame;
                                    EntityView entityView27 = this.entityView;
                                    double y6 = collisionBounds.getY(entityView27, frame35);
                                    PlayerInputState playerInputState8 = input8;
                                    MovementFrame frame36 = frame34;
                                    MovementEnvironment movementEnvironment24 = movementEnvironment23;
                                    EntityView entityView28 = entityView26;
                                    this.velocity = collisionBounds.findVector(entityView28, movementEnvironment24, frame36, playerInputState8, (TimedVelocity) (Object) null, y6);
                                    this.wasOnGround = frame22.getMovementDelta().isHorizontalCollision();
                                    this.ticks = 0;
                                    if (this.groundTicks == 0 && !this.entityView.isInScaffolding() && !this.movementState.isNearBoat()) {
                                        MovementFrame frame37 = this.previousFrame;
                                        this.updateMovementFrame(frame37, matches3, packet, (boolean) false);
                                    }
                                }
                            }
                        } else {
                            DebugManager debugManager14 = this.getPlugin().getDebugManager();
                            Player player22 = this.getPlayer();
                            Player player23 = player22;
                            if (debugManager14.isDebugging(player23)) {
                                this.getPlugin().getDebugManager().log(this.getPlayer(), "move: vehicle switch allowed");
                            }
                            this.onGround = false;
                            this.teleportLocation = this.lastLocation;
                            this.previousFrame = null;
                            this.ticks = 0;
                        }
                    }
                }
            } else {
                FeatureType feature = FeatureType.AAC_MOVE_INF;
                this.recordFeature(feature, (float) 0.0F);
                event.setCancelled(true);
            }
        }
    }

    public boolean checkMovement(PacketEvent event, Vector vector) {
        if (this.groundTicks > 1 && this.movementState.getRootVehicleType() != null) {
            Entity vehicle = this.getPlayer().getVehicle();
            if (this.movementState.getRootVehicleType() == EntityType.BOAT && vehicle != null) {
                AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(vehicle.getLocation().toVector(), EntityDimensions.create(vehicle)).expand(1.0, 0.1, 1.0);
                MovementEnvironment movementEnvironment = this.environment;
                Predicate predicate = MaterialAlias.ICE::contains;
                Predicate predicate2 = predicate;
                boolean matches = movementEnvironment.checkMovementEnvironment(box, predicate2);
                boolean matches2 = matches || this.vehicleTicks > 0;
                if (matches) {
                    this.vehicleTicks = 50;
                } else if (this.vehicleTicks > 0) {
                    this.vehicleTicks--;
                }
                double value = matches2 ? 4.0 : 0.4;
                double value2 = vector.getX() * vector.getX() + vector.getZ() * vector.getZ();
                boolean matches3 = false;
                if (value2 > value * value) {
                    DebugManager debugManager = this.getPlugin().getDebugManager();
                    Player player = this.getPlayer();
                    Player player2 = player;
                    if (debugManager.isDebugging(player2)) {
                        this.getPlugin().getDebugManager().log(this.getPlayer(), "move: BOAT: ! " + findString(vector) + " v");
                    }
                    matches3 = true;
                } else if (vector.getY() >= -0.03 && !vehicle.isOnGround()) {
                    FluidType fluidType = FluidType.WATER;
                    if (!this.environment.sampleFluidContact(box, fluidType).isTouching()) {
                        DebugManager debugManager2 = this.getPlugin().getDebugManager();
                        Player player3 = this.getPlayer();
                        Player player4 = player3;
                        if (debugManager2.isDebugging(player4)) {
                            this.getPlugin().getDebugManager().log(this.getPlayer(), "move: BOAT: ! " + findString(vector) + " a");
                        }
                        matches3 = true;
                    }
                }
                if (matches3) {
                    this.knockbackState = new KnockbackState(this.teleportLocation, this.teleportLocation, new Vector(0, 0, 0), 0.0F);
                    event.setCancelled(true);
                    return true;
                }
            }
            DebugManager debugManager3 = this.getPlugin().getDebugManager();
            Player player5 = this.getPlayer();
            Player player6 = player5;
            if (debugManager3.isDebugging(player6)) {
                this.getPlugin().getDebugManager().log(this.getPlayer(), "move: " + this.movementState.getRootVehicleType() + ": - " + findString(vector));
            }
            this.onGround = false;
            this.teleportLocation = this.lastLocation;
            this.previousFrame = null;
            this.ticks = 0;
            return true;
        } else {
            return false;
        }
    }

    public boolean isMovement() {
        long now = System.currentTimeMillis();
        long value = now - this.lastMove;
        this.lastMove = now;
        float value2 = 0.05F - (float) value / 1000.0F;
        boolean matches = this.movementBuffer.checkViolationBuffer(value2);
        if (this.movementBuffer.getLevel() >= 1.0F && value2 > 0.0F) {
            DebugManager debugManager = this.getPlugin().getDebugManager();
            Player player = this.getPlayer();
            Player player2 = player;
            if (debugManager.isDebugging(player2)) {
                this.getPlugin().getDebugManager().updatePlayer(this.getPlayer(), "move: net timer gain of %.3f (+%.3f)", new Object[] { this.movementBuffer.getLevel(), value2 });
            }
            if (this.velocityTimer.tryAcquire()) {
                FeatureType feature = FeatureType.AAC_MOVE_TIMER;
                float level = this.movementBuffer.getLevel();
                FeatureType feature2 = feature;
                this.recordFeature(feature2, level);
            }
        }
        if (matches) {
            float value3 = (float) (this.lastSafeLocation.getY() - this.teleportLocation.getY()) + this.fallDistance;
            this.knockbackState = new KnockbackState(this.teleportLocation, this.teleportLocation, this.velocity == null ? new Vector(0, 0, 0) : this.velocity, value3);
            float value4 = -0.01F;
            this.movementBuffer.checkViolationBuffer(value4);
            return true;
        } else {
            return false;
        }
    }

    private void updateValue(float value, EntityView entityView, Vector vector, MovementFrame frame, boolean matches) {
        if ((matches || frame.getVelocity() != null && this.onGround) && System.currentTimeMillis() - this.lastVehicleMove > this.lastTeleport) {
            FeatureType feature = FeatureType.AAC_MOVE_VEL;
            this.recordFeature(feature, value);
        } else {
            if (entityView.getEntityType() != EntityType.PLAYER) {
                FeatureType feature2 = FeatureType.AAC_MOVE_VEHICLE;
                this.recordFeature(feature2, value);
            }
            if (frame.getInput().isElytra()) {
                FeatureType feature3 = FeatureType.AAC_MOVE_ELYTRA;
                this.recordFeature(feature3, value);
            } else if (entityView.isInWater()) {
                FeatureType feature4 = FeatureType.AAC_MOVE_WATER;
                this.recordFeature(feature4, value);
            } else if (entityView.isInLava()) {
                FeatureType feature5 = FeatureType.AAC_MOVE_LAVA;
                this.recordFeature(feature5, value);
            } else if (!frame.getInput().isSneaking() && !frame.getInput().isUsingItem()) {
                FeatureType feature6 = FeatureType.AAC_MOVE_GENERIC;
                this.recordFeature(feature6, value);
            } else {
                FeatureType feature7 = FeatureType.AAC_MOVE_NOSLOW;
                this.recordFeature(feature7, value);
            }
        }
    }

    private void updateMovementFrame(MovementFrame frame, boolean matches, PacketContainer packet, boolean matches2) {
        if (this.groundTicks == 0 && frame.getMovementDelta().isHorizontalCollision() != matches) {
            if (matches2 && this.fallDistance > 3.0) {
                float value = matches ? 1.0F : 0.0F;
                FeatureType feature = FeatureType.AAC_MOVE_NOFALL;
                this.recordFeature(feature, value);
            }
            if (this.wasFlying) {
                DebugManager debugManager = this.getPlugin().getDebugManager();
                Player player = this.getPlayer();
                Player player2 = player;
                if (debugManager.isDebugging(player2)) {
                    debugManager = this.getPlugin().getDebugManager();
                    Player player3 = this.getPlayer();
                    StringBuilder stringBuilder = new StringBuilder().append("move: rewrote ground to ").append(!matches).append(" at ");
                    Vector vector = this.getPlayer().getLocation().toVector();
                    Vector vector2 = vector;
                    debugManager.log(player3, stringBuilder.append(findString(vector2)).append(" fd: ").append(this.fallDistance).toString());
                }
                packet.getBooleans().write(0, !matches);
            }
        }
    }

    public void tick() {
        AAC plugin = this.getPlugin();
        Player player = this.getPlayer();
        Player player2 = player;
        ProtocolVersion version = plugin.getProtocolVersion(player2);
        KnockbackState knockbackState2 = this.knockbackState;
        if (this.knockbackState != null) {
            if (!this.vehicleMove) {
                Location origin = knockbackState2.getOrigin();
                origin.setYaw(origin.getYaw() % 360.0F);
                float pitch = origin.getPitch();
                float value2 = -90.0F;
                float value3 = pitch;
                origin.setPitch(MinecraftMath.clamp(value3, value2, (float) 90.0F));
                Vector vector = origin.toVector();
                Player player3 = this.getPlayer();
                Player player4 = player3;
                AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(vector, EntityDimensions.create(player4));
                Player player5 = this.getPlayer();
                AxisAlignedBoundingBox box2 = box.expand(0.001, 0.001, 0.001);
                Player player6 = player5;
                List values = BlockCollisionCollector.findList(player6, box2, version);
                AxisAlignedBoundingBox box3 = box.resolvePenetration(values);
                if (box3.getBottomCenter().distanceSquared(origin.toVector()) < 0.01) {
                    origin = box3.getBottomCenter().toLocation(Objects.requireNonNull(origin.getWorld()), origin.getYaw(), origin.getPitch());
                }
                Vector vector2 = knockbackState2.getDestination().toVector().subtract(knockbackState2.getOrigin().toVector());
                Player player7 = this.getPlayer();
                CollisionResolver.findList(player7, origin, vector2);
                Location location = origin.clone().add(vector2);
                DebugManager debugManager = this.getPlugin().getDebugManager();
                player = this.getPlayer();
                Player player8 = player;
                if (debugManager.isDebugging(player8)) {
                    DebugManager debugManager2 = this.getPlugin().getDebugManager();
                    Player player9 = this.getPlayer();
                    StringBuilder stringBuilder = new StringBuilder().append("move: setback from ");
                    Vector vector3 = knockbackState2.getOrigin().toVector();
                    Vector vector4 = vector3;
                    StringBuilder stringBuilder2 = stringBuilder.append(findString(vector4)).append(" to ");
                    Vector vector5 = location.toVector();
                    Vector vector6 = vector5;
                    StringBuilder stringBuilder3 = stringBuilder2.append(findString(vector6)).append(" fall ").append(knockbackState2.getHorizontalMagnitude()).append(" vel ");
                    Vector velocity = knockbackState2.getVelocity();
                    Vector vector7 = velocity;
                    debugManager2.log(player9, stringBuilder3.append(findString(vector7)).toString());
                }
                this.lastVehicleMove = System.currentTimeMillis();
                this.mitigating = true;
                this.getPlayer().teleport(location, TeleportCause.UNKNOWN);
                this.getPlayer().setFallDistance(knockbackState2.getHorizontalMagnitude());
                this.getPlayer().setVelocity(knockbackState2.getVelocity());
            } else {
                DebugManager debugManager3 = this.getPlugin().getDebugManager();
                player = this.getPlayer();
                Player player10 = player;
                if (debugManager3.isDebugging(player10)) {
                    this.getPlugin().getDebugManager().log(this.getPlayer(), "move: setback aborted by external teleport");
                }
            }
            this.knockbackState = null;
        }
        this.lastSafeLocation = this.getPlayer().getLocation();
        this.fallDistance = this.getPlayer().getFallDistance();
        if (this.lastLocation == null || this.teleportLocation == null) {
            this.lastLocation = this.teleportLocation = this.lastSafeLocation;
        }
        if (this.getPlayer().getVehicle() != null) {
            if (version == ProtocolVersion.V1_8) {
                this.lastLocation = this.teleportLocation = this.lastSafeLocation;
            }
            this.groundTicks = Math.max(this.groundTicks, 1);
        } else {
            this.groundTicks = 0;
        }
        Entity player11 = this.getPlayer();
        while (player11.getVehicle() != null) {
            player11 = player11.getVehicle();
        }
        AxisAlignedBoundingBox box4 = new AxisAlignedBoundingBox(this.vehicleMove ? this.lastSafeLocation.toVector() : this.lastLocation.toVector(), EntityDimensions.create((Entity) player11)).expand(3.0, 2.0, 3.0);
        if (this.previousFrame != null) {
            Vector expectedMovement = this.previousFrame.getMovementDelta().getExpectedMovement();
            Vector vector8 = expectedMovement;
            AxisAlignedBoundingBox box5 = box4.expandTowards(vector8);
            Vector actualMovement = this.previousFrame.getMovementDelta().getActualMovement();
            Vector vector9 = actualMovement;
            box4 = box5.expandTowards(vector9);
        }
        if (!this.velocityQueue.isEmpty()) {
            Vector vector10 = ((TimedVelocity) this.velocityQueue.element()).getVector();
            Vector vector11 = vector10;
            box4 = box4.expandTowards(vector11);
        }
        World world = this.getPlayer().getWorld();
        int value4 = NumberConversions.floor(box4.getMinX()) >> 4;
        int value5 = NumberConversions.floor(box4.getMinZ()) >> 4;
        int value6 = NumberConversions.ceil(box4.getMaxX()) >> 4;
        int value7 = NumberConversions.ceil(box4.getMaxZ()) >> 4;
        boolean matches = world.isChunkLoaded(value4, value5) && world.isChunkLoaded(value6, value7) && world.isChunkLoaded(value4, value7) && world.isChunkLoaded(value6, value5);
        if (matches) {
            if (!this.initialized || !this.environment.getBoundingBox().alignToBlocks().contains(box4)) {
                MovementEnvironment movementEnvironment = this.initialized ? this.environment : null;
                Player player12 = this.getPlayer();
                this.environment = new MovementEnvironment(player12, box4, version, movementEnvironment);
                this.initialized = true;
            }
            MovementState movementState2 = this.movementState;
            player = this.getPlayer();
            Player player13 = player;
            movementState2.updateAttributes(player13);
            MovementState movementState3 = this.movementState;
            Player player14 = this.getPlayer();
            movementState3.updateNearbyEntities(player14);
            MovementState movementState4 = this.movementState;
            Player player15 = this.getPlayer();
            movementState4.updateInputFlags(player15);
            this.movementState.markUpdated();
        } else {
            if (this.environment == null) {
                this.environment = new MovementEnvironment(version);
                this.initialized = false;
            }
            DebugManager debugManager4 = this.getPlugin().getDebugManager();
            player = this.getPlayer();
            Player player16 = player;
            if (debugManager4.isDebugging(player16)) {
                DebugManager debugManager5 = this.getPlugin().getDebugManager();
                Player player17 = this.getPlayer();
                StringBuilder stringBuilder4 = new StringBuilder().append("move: ");
                Vector vector12 = this.teleportLocation.toVector();
                Vector vector13 = vector12;
                debugManager5.log(player17, stringBuilder4.append(findString(vector13)).append(" not loaded").toString());
            }
        }
    }

    public void updateLocation(Location location) {
        this.teleporting = true;
        this.vehicleMove = true;
        this.knockbackState = null;
        this.velocityQueue.clear();
    }

    public void updateLocationWithFallDistance(Location location) {
        this.fallDistance = 0.0F;
        this.previousFrame = null;
        this.lastLocation = this.teleportLocation = location;
        this.vehicleMove = false;
        this.knockbackState = null;
    }

    private double getZ(Vector vector, MovementFrame frame) {
        double value = vector.getX() - frame.getMovementDelta().getExpectedMovement().getX();
        double value2 = vector.getZ() - frame.getMovementDelta().getExpectedMovement().getZ();
        return value * value + value2 * value2;
    }

    private int calculateCount(EntityView entityView, Vector vector, MovementFrame frame, double value) {
        ProtocolVersion protocolVersion = entityView.getProtocolVersion();
        ProtocolVersion version = ProtocolVersion.V1_13;
        if (protocolVersion.isAtLeast(version) && Math.abs(vector.getY() - frame.getMovementDelta().getExpectedMovement().getY()) < 0.001 && Math.abs(vector.getZ() - frame.getMovementDelta().getExpectedMovement().getZ()) < 0.001 && Math.abs(vector.getX()) < 0.2 && this.previousFrame.getMovementDelta().isVerticalCollision()) {
            return 0;
        } else if (!entityView.isInFlowingWater() && !entityView.isInFlowingLava() && !entityView.isPreviousFlowingFluid() || !(value < this.horizontalSpeed * this.horizontalSpeed) && (!(this.getZ(vector, frame) < this.horizontalSpeed * this.horizontalSpeed) || !(vector.getY() > 0.299) || !(vector.getY() < 0.341))) {
            if (entityView.isInBubbleColumn() && value < this.verticalSpeed * this.verticalSpeed) {
                return 2;
            } else if (entityView.isClimbing() && vector.getY() <= 0.121 && vector.getY() >= -0.151 && Math.abs(vector.getX()) <= 0.151 && Math.abs(vector.getZ()) <= 0.151) {
                return 3;
            } else {
                protocolVersion = entityView.getProtocolVersion();
                ProtocolVersion version2 = ProtocolVersion.V1_9;
                if (protocolVersion.isAtLeast(version2) && this.movementState.getEntityPushMagnitude() > 0.0 && Math.sqrt(value) <= this.movementState.getEntityPushMagnitude() + this.previousHorizontalSpeed) {
                    return 4;
                } else {
                    protocolVersion = ProtocolVersion.getServerVersion();
                    ProtocolVersion version3 = ProtocolVersion.V1_11;
                    if (protocolVersion.isAtLeast(version3)) {
                        if (this.movementState.getStableGliding() != null && !this.movementState.getStableGliding()) {
                            if (this.waterTicks-- > 0 && value < this.previousVerticalSpeed * this.previousVerticalSpeed) {
                                return 5;
                            }
                        } else {
                            if (this.movementState.isNearFirework()) {
                                this.waterTicks = 10;
                            }
                            if (this.waterTicks-- > 0 && value < this.previousVerticalSpeed * this.previousVerticalSpeed) {
                                return 5;
                            }
                        }
                    }
                    if (this.lavaTicks-- > 0 && value < this.expectedHorizontalSpeed * this.expectedHorizontalSpeed) {
                        return 6;
                    } else if (this.movementState.isNearBoat() && value < this.expectedVerticalSpeed * this.expectedVerticalSpeed) {
                        return 7;
                    } else if ((this.movementState.isNearShulker() || entityView.isAboveShulkerBox()) && value < this.accumulatedHorizontalError * this.accumulatedHorizontalError) {
                        return 8;
                    } else if (this.lastVelocityTick-- > 0 && value < this.accumulatedVerticalError * this.accumulatedVerticalError) {
                        return 9;
                    } else if (this.movementState.hasRecentSpeedChange() && value < this.movementState.getMaximumRecentWalkSpeed() * this.movementState.getMaximumRecentWalkSpeed()) {
                        return 10;
                    } else {
                        protocolVersion = ProtocolVersion.getServerVersion();
                        ProtocolVersion version4 = ProtocolVersion.V1_16;
                        if (protocolVersion.isAtLeast(version4) && entityView.getEntityType() == EntityType.STRIDER && entityView.isInLava() && value < 0.25) {
                            return 11;
                        } else if (this.groundTicks > 0 && this.elytraTicks > 0 && value < 0.04000000000000001) {
                            return 12;
                        } else if (entityView.isInScaffolding() && value < entityView.getJumpStrength()) {
                            return 13;
                        } else {
                            Vector vector2 = entityView.getBoundingBox().getBottomCenter().add(frame.getMovementDelta().getExpectedMovement());
                            MovementState movementState2 = this.movementState;
                            double x = vector2.getX();
                            double z = vector2.getZ();
                            double value2 = z;
                            double value3 = x;
                            return movementState2.crossesWorldBorder(value3, value2, (double) 1.0) && Math.abs(frame.getMovementDelta().getExpectedMovement().getY() - vector.getY()) < 0.001 && vector.lengthSquared() < frame.getMovementDelta().getExpectedMovement().lengthSquared() ? 14 : -1;
                        }
                    }
                }
            }
        } else {
            return 1;
        }
    }

    private boolean checkMovement(EntityView entityView, Vector vector, MovementFrame frame) {
        AxisAlignedBoundingBox box = entityView.getBoundingBox().expand(0.5, 0.5, 0.5);
        for (Entry entry : (Iterable<Entry>) (Iterable<?>) (this.getPlugin().getPacketDispatcher().getPistonPushes().asMap().entrySet())) {
            Object key = entry.getKey();
            AxisAlignedBoundingBox box2 = (AxisAlignedBoundingBox) key;
            if (box.intersects(box2) && vector.lengthSquared() <= 1.0) {
                return true;
            }
        }
        for (Entry entry2 : (Iterable<Entry>) (Iterable<?>) (this.getPlugin().getPacketDispatcher().getSlimePushes().asMap().entrySet())) {
            Object key2 = entry2.getKey();
            AxisAlignedBoundingBox box3 = (AxisAlignedBoundingBox) key2;
            if (box.intersects(box3) && vector.lengthSquared() <= 3.0) {
                return true;
            }
        }
        return false;
    }

    public static String findString(Vector vector) {
        return vector == null ? "null" : String.format(Locale.UK, "%.4f,%.4f,%.4f", vector.getX(), vector.getY(), vector.getZ());
    }
}
