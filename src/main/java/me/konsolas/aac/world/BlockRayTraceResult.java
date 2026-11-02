package me.konsolas.aac.world;

import java.util.Objects;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class BlockRayTraceResult {

    private final Vector hitVector;

    private final BlockVector blockVector;

    private final BlockFace face;

    public BlockRayTraceResult() {
        this.hitVector = null;
        this.blockVector = null;
        this.face = null;
    }

    public BlockRayTraceResult(Vector vector, BlockVector blockVector2, BlockFace blockFace) {
        this.hitVector = Objects.requireNonNull(vector).clone();
        this.blockVector = Objects.requireNonNull(blockVector2);
        this.face = Objects.requireNonNull(blockFace);
    }

    public static BlockRayTraceResult create(RayTraceResult rayTraceResult) {
        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
            Vector hitPosition = rayTraceResult.getHitPosition();
            BlockVector blockVector = rayTraceResult.getHitBlock().getLocation().toVector().toBlockVector();
            BlockFace hitBlockFace = rayTraceResult.getHitBlockFace();
            BlockVector blockVector2 = blockVector;
            Vector vector = hitPosition;
            return new BlockRayTraceResult(vector, blockVector2, hitBlockFace);
        } else {
            return new BlockRayTraceResult();
        }
    }

    public Vector getVector() {
        return this.hitVector.clone();
    }

    public BlockVector getBlockVector() {
        return this.blockVector;
    }

    public BlockFace getFace() {
        return this.face;
    }

    public boolean isBlockRayTraceResult() {
        return this.hitVector != null && this.blockVector != null && this.face != null;
    }

    @Override
    public String toString() {
        return "TraceResult{hitVec=" + this.hitVector + ", blockVector=" + this.blockVector + ", face=" + this.face + '}';
    }
}
