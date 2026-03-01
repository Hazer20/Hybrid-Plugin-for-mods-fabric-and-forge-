package com.hazer.bookimage.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Converts image into colored text grid (book-safe rendering).
 */
public final class ImageRenderUtil {

    public static final int MAX_IMAGE_PIXELS = 128;
    private static final int RENDER_WIDTH = 16;
    private static final int RENDER_HEIGHT = 16;

    private ImageRenderUtil() {
    }

    public static BufferedImage scaleTo128(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        double ratio = Math.min((double) MAX_IMAGE_PIXELS / width, (double) MAX_IMAGE_PIXELS / height);
        if (ratio > 1.0d) {
            ratio = 1.0d;
        }

        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));

        BufferedImage out = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = out.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        return out;
    }

    public static Component renderToComponent(BufferedImage image, String sourceUrlBase64) {
        BufferedImage sampled = sampleForBook(image, RENDER_WIDTH, RENDER_HEIGHT);
        TextComponent.Builder builder = Component.text();

        builder.append(Component.text("[Изображение]", TextColor.color(0x55FF55))).append(Component.newline());
        for (int y = 0; y < sampled.getHeight(); y++) {
            for (int x = 0; x < sampled.getWidth(); x++) {
                int argb = sampled.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha < 20) {
                    builder.append(Component.text(" "));
                    continue;
                }
                int rgb = argb & 0xFFFFFF;
                builder.append(Component.text("█", TextColor.color(rgb)));
            }
            if (y < sampled.getHeight() - 1) {
                builder.append(Component.newline());
            }
        }

        builder.append(Component.newline())
                .append(Component.text("meta:", TextColor.color(0x888888)))
                .append(Component.text(sourceUrlBase64, TextColor.color(0x444444)));

        return builder.build();
    }

    private static BufferedImage sampleForBook(BufferedImage image, int width, int height) {
        BufferedImage sampled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = sampled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return sampled;
    }
}
