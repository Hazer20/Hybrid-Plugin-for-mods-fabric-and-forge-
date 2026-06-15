package com.hybrid.rpgcharacter.client;

import com.hybrid.rpgcharacter.config.CharacterConfig;
import com.hybrid.rpgcharacter.data.CharacterData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Basic Forge GUI for inspecting synced character data. */
public class CharacterScreen extends Screen {
    private final CharacterData data;

    public CharacterScreen(CharacterData data) {
        super(Component.translatable("screen.rpgcharacter.character"));
        this.data = data;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int x = width / 2 - 100;
        int y = 32;
        graphics.drawCenteredString(font, title, width / 2, y, 0xF5D28A);
        if (data == null) {
            graphics.drawString(font, Component.translatable("screen.rpgcharacter.no_data"), x, y + 24, 0xFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        drawLine(graphics, "name", data.getCharacterName(), x, y += 24);
        drawLine(graphics, "age", String.valueOf(data.getAge()), x, y += 12);
        drawLine(graphics, "gender", data.getGender().name(), x, y += 12);
        drawLine(graphics, "height", data.getHeightCm() + " cm", x, y += 12);
        drawLine(graphics, "weight", String.format("%.1f kg", data.getWeightKg()), x, y += 12);
        drawLine(graphics, "body_type", data.getBodyType().name(), x, y += 12);
        drawLine(graphics, "traits", data.getTraits().toString(), x, y += 12);
        drawProgress(graphics, x, y + 18);
        drawPlaceholderIcons(graphics, x, y + 34);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawLine(GuiGraphics graphics, String key, String value, int x, int y) {
        graphics.drawString(font, Component.translatable("screen.rpgcharacter." + key).append(": " + value), x, y, 0xFFFFFF);
    }

    private void drawProgress(GuiGraphics graphics, int x, int y) {
        long threshold = Math.max(1L, CharacterConfig.AGE_PROGRESS_THRESHOLD.get());
        int filled = (int) Math.min(100L, data.getAgeProgress() * 100L / threshold);
        graphics.fill(x, y, x + 102, y + 8, 0xFF2A1D14);
        graphics.fill(x + 1, y + 1, x + 1 + filled, y + 7, 0xFF7BC46A);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter.age_progress"), x, y - 10, 0xFFFFFF);
    }

    private void drawPlaceholderIcons(GuiGraphics graphics, int x, int y) {
        for (int i = 0; i < 4; i++) {
            graphics.fill(x + i * 18, y, x + i * 18 + 14, y + 14, 0xFF6B5940);
        }
    }
}
