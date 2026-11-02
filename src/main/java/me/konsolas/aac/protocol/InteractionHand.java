package me.konsolas.aac.protocol;

import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum InteractionHand {

    MAIN_HAND, OFF_HAND;

    private static final List HANDS = Collections.unmodifiableList(Arrays.asList(values()));

    static InteractionHand create(Hand hand) {
        return (InteractionHand) HANDS.get(hand.ordinal());
    }
}
