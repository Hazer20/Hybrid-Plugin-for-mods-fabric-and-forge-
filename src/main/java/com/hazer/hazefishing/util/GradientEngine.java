package com.hazer.hazefishing.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;

public final class GradientEngine {

    public Component gradientText(String input, int tick, TextColor start, TextColor end) {
        List<Component> components = new ArrayList<>();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            double t = (double) i / Math.max(1, chars.length - 1);
            double wave = (Math.sin((tick + i) / 4.5) + 1D) / 2D;
            int r = (int) (start.red() + (end.red() - start.red()) * (t * 0.7 + wave * 0.3));
            int g = (int) (start.green() + (end.green() - start.green()) * (t * 0.7 + wave * 0.3));
            int b = (int) (start.blue() + (end.blue() - start.blue()) * (t * 0.7 + wave * 0.3));
            components.add(Component.text(chars[i], TextColor.color(r, g, b)));
        }
        Component merged = Component.empty();
        for (Component component : components) {
            merged = merged.append(component);
        }
        return merged;
    }

    public TextColor oscillatingColor(int tick, int phase) {
        int r = (int) (Math.sin((tick + phase) * 0.075) * 127 + 128);
        int g = (int) (Math.sin((tick + phase) * 0.075 + 2.0) * 127 + 128);
        int b = (int) (Math.sin((tick + phase) * 0.075 + 4.0) * 127 + 128);
        return TextColor.color(r, g, b);
    }
}
