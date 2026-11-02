package me.konsolas.aac.world;

import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

class ModernBlockState implements BlockState {

    ProtocolVersion protocolVersion;

    BlockData blockData;

    int cacheKey;

    ModernBlockState(ProtocolVersion version, Block block) {
        this.updateProtocolVersion(version, block);
    }

    ModernBlockState(ModernBlockState modernBlockState) {
        this.protocolVersion = modernBlockState.protocolVersion;
        this.blockData = modernBlockState.blockData.clone();
        this.cacheKey = modernBlockState.cacheKey;
    }

    @Override
    public void updateProtocolVersion(ProtocolVersion version, Block block) {
        this.protocolVersion = version;
        this.blockData = block.getBlockData();
        ProtocolVersion serverVersion = ProtocolVersion.getServerVersion();
        ProtocolVersion version2 = ProtocolVersion.V1_14;
        int value;
        if (serverVersion.isAtLeast(version2) && this.blockData.getMaterial() == Material.BAMBOO) {
            int z = block.getZ();
            int x = block.getX();
            value = calculateCount(x, z);
        } else {
            value = 0;
        }
        this.cacheKey = value;
    }

    private static int calculateCount(int value, int value2) {
        long value3 = value * 3129871L ^ value2 * 116129781L ^ 0L;
        value3 = value3 * value3 * 42317861L + value3 * 11L;
        return (int) (value3 >> 16 & 4095L);
    }

    @Override
    public BlockState copy() {
        return new ModernBlockState(this);
    }

    @Override
    public boolean equals(Object result) {
        if (this == result) {
            return true;
        } else if (result != null && this.getClass() == result.getClass()) {
            ModernBlockState modernBlockState = (ModernBlockState) result;
            if (this.cacheKey != modernBlockState.cacheKey) {
                return false;
            } else {
                return this.protocolVersion != modernBlockState.protocolVersion ? false : this.blockData.equals(modernBlockState.blockData);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int value = this.protocolVersion.hashCode();
        value = 31 * value + this.blockData.hashCode();
        return 31 * value + this.cacheKey;
    }

    @Override
    public String toString() {
        return this.protocolVersion + "/" + this.blockData.getAsString() + (this.cacheKey != 0 ? "#" + this.cacheKey : "");
    }
}
