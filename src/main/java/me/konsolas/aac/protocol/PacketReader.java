package me.konsolas.aac.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.Direction;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import java.util.Objects;
import me.konsolas.aac.util.math.MinecraftMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PacketReader {

    public static final PacketType VEHICLE_MOVE;

    public static final PacketType USE_ITEM;

    public static final PacketType TELEPORT_ACCEPT;

    public static final PacketType GROUND;

    public static boolean checkPacketReader(PacketContainer packet) {
        if (packet.getType() != Client.USE_ENTITY) {
            return false;
        } else {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_17;
            if (serverVersion.isAtLeast(version)) {
                WrappedEnumEntityUseAction wrappedEnumEntityUseAction = (WrappedEnumEntityUseAction) packet.getEnumEntityUseActions().read(0);
                return wrappedEnumEntityUseAction.getAction() == EntityUseAction.ATTACK;
            } else {
                return packet.getEntityUseActions().read(0) == EntityUseAction.ATTACK;
            }
        }
    }

    public static boolean checkPacketReader(PacketType packetType) {
        return packetType == Client.POSITION || packetType == Client.POSITION_LOOK || packetType == Client.LOOK || packetType == GROUND;
    }

    public static Location findLocation(PacketContainer packet, Location location) {
        Vector vector;
        if (packet.getType() != Client.POSITION && packet.getType() != Client.POSITION_LOOK && packet.getType() != VEHICLE_MOVE) {
            vector = location.toVector();
        } else {
            vector = new Vector((Double) packet.getDoubles().read(0), (Double) packet.getDoubles().read(1), (Double) packet.getDoubles().read(2));
        }
        float value;
        float value2;
        if (packet.getType() != Client.LOOK && packet.getType() != Client.POSITION_LOOK && packet.getType() != VEHICLE_MOVE) {
            value2 = location.getYaw();
            value = location.getPitch();
        } else {
            value2 = (Float) packet.getFloat().read(0) % 360.0F;
            float value3 = (Float) packet.getFloat().read(1);
            float value4 = value3;
            value = MinecraftMath.clamp(value4, -90.0F, 90.0F);
        }
        return vector.toLocation(Objects.requireNonNull(location.getWorld()), value2, value);
    }

    public static Vector findVector(PacketContainer packet) {
        int value = (Integer) packet.getIntegers().read(1);
        int value2 = (Integer) packet.getIntegers().read(2);
        int value3 = (Integer) packet.getIntegers().read(3);
        return new Vector(value / 8000.0, value2 / 8000.0, value3 / 8000.0);
    }

    public static BlockInteraction findBlockInteraction(PacketContainer packet) {
        if (packet.getBlockPositionModifier().size() > 0) {
            ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version = ProtocolVersion.V1_9;
            if (serverVersion.isAtLeast(version)) {
                Object result = packet.getBlockPositionModifier().read(0);
                result = (BlockPosition) result;
                BlockDirection blockDirection = BlockDirection.create((Direction) packet.getDirections().read(0));
                InteractionHand interactionHand = InteractionHand.create((Hand) packet.getHands().read(0));
                BlockDirection blockDirection2 = blockDirection;
                BlockPosition blockPosition = (BlockPosition) result;
                return new BlockInteraction(blockPosition, blockDirection2, interactionHand);
            }
            if ((Integer) packet.getIntegers().read(0) < 6) {
                Object result2 = packet.getBlockPositionModifier().read(0);
                result2 = (BlockPosition) result2;
                BlockDirection blockDirection3 = BlockDirection.create((Integer) packet.getIntegers().read(0));
                InteractionHand interactionHand2 = InteractionHand.MAIN_HAND;
                BlockDirection blockDirection4 = blockDirection3;
                BlockPosition blockPosition2 = (BlockPosition) result2;
                return new BlockInteraction(blockPosition2, blockDirection4, interactionHand2);
            }
        } else {
            ProtocolVersion serverVersion2 = ProtocolVersion.getServerVersion();
            ProtocolVersion version2 = ProtocolVersion.V1_14;
            if (serverVersion2.isAtLeast(version2) && packet.getType() == USE_ITEM) {
                MovingObjectPositionBlock movingObjectPositionBlock = (MovingObjectPositionBlock) packet.getMovingBlockPositions().read(0);
                BlockPosition blockPosition3 = movingObjectPositionBlock.getBlockPosition();
                BlockDirection blockDirection5 = BlockDirection.create(movingObjectPositionBlock.getDirection());
                InteractionHand interactionHand3 = InteractionHand.create((Hand) packet.getHands().read(0));
                BlockDirection blockDirection6 = blockDirection5;
                BlockPosition blockPosition4 = blockPosition3;
                return new BlockInteraction(blockPosition4, blockDirection6, interactionHand3);
            }
        }
        return null;
    }

    private static BlockPosition findBlockPositionWithVALUESS(BlockPosition blockPosition, BlockDirection blockDirection) {
        switch(blockDirection) {
            case DOWN:
                return new BlockPosition(blockPosition.getX(), blockPosition.getY() - 1, blockPosition.getZ());
            case BLOCK_DIRECTION:
                return new BlockPosition(blockPosition.getX(), blockPosition.getY() + 1, blockPosition.getZ());
            case NORTH:
                return new BlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ() - 1);
            case SOUTH:
                return new BlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ() + 1);
            case WEST:
                return new BlockPosition(blockPosition.getX() - 1, blockPosition.getY(), blockPosition.getZ());
            case EAST:
                return new BlockPosition(blockPosition.getX() + 1, blockPosition.getY(), blockPosition.getZ());
            default:
                throw new AssertionError();
        }
    }

    static BlockPosition findBlockPosition(BlockPosition blockPosition, BlockDirection blockDirection) {
        return findBlockPositionWithVALUESS(blockPosition, blockDirection);
    }

    static {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_9;
        VEHICLE_MOVE = serverVersion.isAtLeast(version) ? Client.VEHICLE_MOVE : null;
        serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version2 = ProtocolVersion.V1_9;
        USE_ITEM = serverVersion.isAtLeast(version2) ? Client.USE_ITEM : null;
        serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version3 = ProtocolVersion.V1_9;
        TELEPORT_ACCEPT = serverVersion.isAtLeast(version3) ? Client.TELEPORT_ACCEPT : null;
        serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version4 = ProtocolVersion.V1_16;
        GROUND = serverVersion.isAtLeast(version4) ? Client.GROUND : Client.FLYING;
    }
}
