package me.konsolas.aac.analysis.feature;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum FeatureType {

    AAC_NORM_MOVE(2),
    AAC_NORM_ATTACK(1),
    AAC_NORM_INTERACT(3),
    AAC_DELAYS_BREAK(100),
    AAC_DELAYS_PLACE(101),
    AAC_DELAYS_CONSUME(102),
    AAC_DELAYS_BOW(103),
    AAC_DELAYS_REGEN(104),
    AAC_DELAYS_UNSNEAK(105),
    AAC_DELAYS_RELEASE(106),
    AAC_DELAYS_BREAKDELAY(108),
    AAC_MOVE_NOFALL(300),
    AAC_MOVE_VEL(301),
    AAC_MOVE_VEHICLE(302),
    AAC_MOVE_ELYTRA(303),
    AAC_MOVE_WATER(304),
    AAC_MOVE_LAVA(305),
    AAC_MOVE_NOSLOW(306),
    AAC_MOVE_GENERIC(307),
    AAC_MOVE_TIMER(308),
    AAC_MOVE_INF(309),
    AAC_AIMBOT_PITCH_SPREAD(401),
    AAC_AIMBOT_SENS_MISMATCH(402),
    AAC_AIMBOT_SENS_X_OOR(403),
    AAC_AIMBOT_SENS_Y_OOR(404),
    AAC_AIMBOT_BAD_DELTA_X(405),
    AAC_AIMBOT_BAD_DELTA_Y(406),
    AAC_AIMBOT_HEAD_SNAP(407),
    AAC_AIMBOT_ZERO_POINT(408),
    AAC_CLICK_CPS(500),
    AAC_CLICK_VAR(501),
    AAC_CLICK_TICK_DELAY(502),
    AAC_CLICK_NOSWING(503),
    AAC_CLICK_TIMING(504),
    AAC_HITBOX_COUNT(601),
    AAC_HITBOX_MISS(602),
    AAC_HITBOX_WALLS(603),
    AAC_HITBOX_REACH(604),
    AAC_INTERACT_GEN(700),
    AAC_INTERACT_BREAK(701),
    AAC_INTERACT_PLACE(702),
    AAC_MISC_ABILITIES(801),
    AAC_MISC_PITCH(802),
    AAC_MISC_ROTATION(803);

    private final int id;

    private static final Int2ObjectMap BY_ID = new Int2ObjectOpenHashMap();

    private FeatureType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static FeatureType fromId(int value) {
        return (FeatureType) BY_ID.get(value);
    }

    static {
        for (FeatureType feature : values()) {
            if (BY_ID.put(feature.getId(), feature) != null) {
                throw new IllegalStateException("duplicate mapping for id " + feature);
            }
        }
    }
}
