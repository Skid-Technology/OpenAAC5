package me.konsolas.aac.protocol;

import com.comphenix.protocol.wrappers.BlockPosition;

public class BlockInteraction {

    private final BlockPosition position;

    private final BlockPosition adjacentPosition;

    private final InteractionHand hand;

    public BlockInteraction(BlockPosition position, BlockDirection blockDirection, InteractionHand hand) {
        this.position = position;
        this.adjacentPosition = PacketReader.findBlockPosition(position, blockDirection);
        this.hand = hand;
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public BlockPosition getAdjacentPosition() {
        return this.adjacentPosition;
    }

    public InteractionHand getHand() {
        return this.hand;
    }
}
