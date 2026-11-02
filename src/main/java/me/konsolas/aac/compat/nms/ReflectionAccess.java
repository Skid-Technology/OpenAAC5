package me.konsolas.aac.compat.nms;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;

public class ReflectionAccess {

    public static Lookup LOOKUP = MethodHandles.lookup();

    public static AccessibleObject makeAccessible(AccessibleObject accessibleObject) {
        accessibleObject.setAccessible(true);
        return accessibleObject;
    }
}
