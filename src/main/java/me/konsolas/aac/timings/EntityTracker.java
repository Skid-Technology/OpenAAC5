package me.konsolas.aac.timings;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.konsolas.aac.AAC;
import me.konsolas.aac.movement.EntityDimensions;
import me.konsolas.aac.util.collection.FixedSizeList;
import me.konsolas.aac.util.math.AxisAlignedBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EntityTracker implements Runnable {

    private final LoadingCache<LivingEntity, Boolean> trackedEntities = CacheBuilder.newBuilder().expireAfterWrite(5000L, TimeUnit.MILLISECONDS).build(CacheLoader.from(entity -> Boolean.TRUE));

    private final IntOpenHashSet activeEntityIds = new IntOpenHashSet();

    private final Int2ObjectMap positionHistory = new Int2ObjectOpenHashMap();

    private final int historyLength;

    public EntityTracker(AAC aacPlugin) {
        aacPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(aacPlugin, this, 1L, 1L);
        this.historyLength = aacPlugin.getConfig().getInt("checks.hitbox.lag_compensation_ticks");
    }

    @Override
    public void run() {
        this.activeEntityIds.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.updateLivingEntityWithPositionHistory(player);
        }
        for (LivingEntity livingEntity : (Iterable<LivingEntity>) (Iterable<?>) (this.trackedEntities.asMap().keySet())) {
            this.updateLivingEntityWithPositionHistory(livingEntity);
        }
        this.positionHistory.keySet().retainAll(this.activeEntityIds);
    }

    private void updateLivingEntityWithPositionHistory(LivingEntity livingEntity) {
        FixedSizeList fixedSizeList = (FixedSizeList) this.positionHistory.computeIfAbsent(livingEntity.getEntityId(), result -> new FixedSizeList(this.historyLength));
        fixedSizeList.add(livingEntity.getLocation().toVector());
        this.activeEntityIds.add(livingEntity.getEntityId());
    }

    public List findList(LivingEntity livingEntity) {
        FixedSizeList fixedSizeList = (FixedSizeList) this.positionHistory.get(livingEntity.getEntityId());
        if (fixedSizeList != null) {
            EntityDimensions entityDimensions = EntityDimensions.create(livingEntity);
            ArrayList values = new ArrayList();
            for (Vector vector : (Iterable<Vector>) (Iterable<?>) (fixedSizeList)) {
                if (vector != null) {
                    AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(vector, entityDimensions);
                    values.add(box);
                }
            }
            return values;
        } else {
            return null;
        }
    }

    public void updateLivingEntity(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player)) {
            this.trackedEntities.put(livingEntity, Boolean.TRUE);
            this.trackedEntities.refresh(livingEntity);
        }
    }
}
