package me.konsolas.aac.world;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

class LegacyBlockState implements BlockState {

    static Set TRANSPARENT_MATERIALS = EnumSet.noneOf(Material.class);

    static BlockFace[] NEIGHBOR_FACES = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };

    ProtocolVersion protocolVersion;

    Material material;

    byte data;

    Material[] neighborMaterials = new Material[6];

    byte[] neighborData = new byte[6];

    LegacyBlockState(ProtocolVersion version, Block block) {
        this.updateProtocolVersion(version, block);
    }

    LegacyBlockState(LegacyBlockState legacyBlockState) {
        this.protocolVersion = legacyBlockState.protocolVersion;
        this.material = legacyBlockState.material;
        this.data = legacyBlockState.data;
        this.neighborMaterials = (Material[]) legacyBlockState.neighborMaterials.clone();
        this.neighborData = (byte[]) legacyBlockState.neighborData.clone();
    }

    @Override
    public void updateProtocolVersion(ProtocolVersion version, Block block) {
        this.protocolVersion = version;
        this.material = block.getType();
        this.data = block.getData();
        if (TRANSPARENT_MATERIALS.contains(this.material)) {
            for (int index = 0; index < NEIGHBOR_FACES.length; index++) {
                Block relative = block.getRelative(NEIGHBOR_FACES[index]);
                this.neighborMaterials[index] = relative.getType();
                this.neighborData[index] = relative.getData();
            }
        } else {
            Arrays.fill(this.neighborMaterials, null);
            Arrays.fill(this.neighborData, (byte) 0);
        }
    }

    @Override
    public BlockState copy() {
        return new LegacyBlockState(this);
    }

    @Override
    public boolean equals(Object result) {
        if (this == result) {
            return true;
        } else if (result != null && this.getClass() == result.getClass()) {
            LegacyBlockState legacyBlockState = (LegacyBlockState) result;
            if (this.data != legacyBlockState.data) {
                return false;
            } else if (this.protocolVersion != legacyBlockState.protocolVersion) {
                return false;
            } else if (this.material != legacyBlockState.material) {
                return false;
            } else {
                return !Arrays.equals((Object[]) this.neighborMaterials, (Object[]) legacyBlockState.neighborMaterials) ? false : Arrays.equals(this.neighborData, legacyBlockState.neighborData);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int value = this.protocolVersion.hashCode();
        value = 31 * value + this.material.hashCode();
        value = 31 * value + this.data;
        value = 31 * value + Arrays.hashCode((Object[]) this.neighborMaterials);
        return 31 * value + Arrays.hashCode(this.neighborData);
    }

    @Override
    public String toString() {
        return String.format("%s/%s[data=0x%02x]", this.protocolVersion.name(), "legacy:" + this.material.name().toLowerCase(Locale.ROOT), this.data) + (TRANSPARENT_MATERIALS.contains(this.material) ? String.format("#{%s.%02x,%s.%02x,%s.%02x,%s.%02x,%s.%02x,%s.%02x}", this.neighborMaterials[0].name().toLowerCase(Locale.ROOT), this.neighborData[0], this.neighborMaterials[1].name().toLowerCase(Locale.ROOT), this.neighborData[1], this.neighborMaterials[2].name().toLowerCase(Locale.ROOT), this.neighborData[2], this.neighborMaterials[3].name().toLowerCase(Locale.ROOT), this.neighborData[3], this.neighborMaterials[4].name().toLowerCase(Locale.ROOT), this.neighborData[4], this.neighborMaterials[5].name().toLowerCase(Locale.ROOT), this.neighborData[5]) : "");
    }

    static {
        for (Material material : Material.values()) {
            if (material.isBlock() && (material.name().contains("STAIRS") || material.name().contains("FENCE") || material.name().contains("CHEST") || material.name().contains("WALL") || material.name().contains("DOOR") || material.name().contains("PANE"))) {
                TRANSPARENT_MATERIALS.add(material);
            }
        }
    }
}
