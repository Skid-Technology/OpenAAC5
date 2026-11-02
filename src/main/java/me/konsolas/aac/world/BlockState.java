package me.konsolas.aac.world;

import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.block.Block;

interface BlockState {

    void updateProtocolVersion(ProtocolVersion version, Block block);

    BlockState copy();
}
