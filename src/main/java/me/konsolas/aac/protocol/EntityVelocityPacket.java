package me.konsolas.aac.protocol;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import org.bukkit.util.Vector;

public class EntityVelocityPacket {

    private final Vector velocity;

    private EntityVelocityPacket(Vector velocity) {
        this.velocity = velocity;
    }

    public static EntityVelocityPacket create(PacketContainer packet) {
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version = ProtocolVersion.V1_14;
        if (serverVersion.isAtLeast(version)) {
            if (packet.getType() == PacketReader.USE_ITEM) {
                MovingObjectPositionBlock movingObjectPositionBlock = (MovingObjectPositionBlock) packet.getMovingBlockPositions().read(0);
                return new EntityVelocityPacket(movingObjectPositionBlock.getPosVector());
            }
        } else {
            serverVersion = ProtocolVersion.getServerVersion();
            ProtocolVersion version2 = ProtocolVersion.V1_9;
            if (serverVersion.isAtLeast(version2) && packet.getType() == PacketReader.USE_ITEM || ProtocolVersion.getServerVersion() == ProtocolVersion.V1_8 && packet.getType() == Client.BLOCK_PLACE) {
                BlockPosition blockPosition = (BlockPosition) packet.getBlockPositionModifier().read(0);
                double value = blockPosition.getX() + (Float) packet.getFloat().read(0);
                double value2 = blockPosition.getY() + (Float) packet.getFloat().read(1);
                double value3 = blockPosition.getZ() + (Float) packet.getFloat().read(2);
                return new EntityVelocityPacket(new Vector(value, value2, value3));
            }
        }
        return null;
    }

    public Vector getVelocity() {
        return this.velocity;
    }
}
