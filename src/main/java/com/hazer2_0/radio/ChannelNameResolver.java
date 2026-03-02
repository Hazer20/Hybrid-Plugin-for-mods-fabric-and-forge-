package com.hazer2_0.radio;

import org.bukkit.block.BlockState;

import java.lang.reflect.Method;

public final class ChannelNameResolver {

    private ChannelNameResolver() {
    }

    public static String resolve(BlockState state) {
        if (state == null) {
            return null;
        }

        // Support API variants where block custom naming is exposed as customName() or getCustomName().
        String viaCustomName = invokeNameMethod(state, "customName");
        if (viaCustomName != null && !viaCustomName.isBlank()) {
            return viaCustomName;
        }

        String viaGetCustomName = invokeNameMethod(state, "getCustomName");
        if (viaGetCustomName != null && !viaGetCustomName.isBlank()) {
            return viaGetCustomName;
        }

        return null;
    }

    private static String invokeNameMethod(BlockState state, String methodName) {
        try {
            Method method = state.getClass().getMethod(methodName);
            Object result = method.invoke(state);
            return result == null ? null : String.valueOf(result);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
