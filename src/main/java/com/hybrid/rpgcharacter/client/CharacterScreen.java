package com.hybrid.rpgcharacter.client;

import com.hybrid.rpgcharacter.config.CharacterConfig;
import com.hybrid.rpgcharacter.data.CharacterData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Polished Forge GUI for inspecting synced character data. */
public class CharacterScreen extends Screen {
    private final CharacterData data;

    public CharacterScreen(CharacterData data) {
        super(Component.translatable("screen.rpgcharacter.character"));
        this.data = data;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = 286;
        int panelHeight = 194;
        int panelX = width / 2 - panelWidth / 2;
        int panelY = height / 2 - panelHeight / 2;
        drawPanel(graphics, panelX, panelY, panelWidth, panelHeight);
        graphics.drawCenteredString(font, title, width / 2, panelY + 14, 0xF5D28A);
        if (data == null) {
            graphics.drawCenteredString(font, Component.translatable("screen.rpgcharacter.no_data"), width / 2, panelY + 84, 0xFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        int leftX = panelX + 22;
        int rightX = panelX + 148;
        int y = panelY + 38;
        drawStat(graphics, leftX, y, "name", data.getCharacterName());
        drawStat(graphics, rightX, y, "age", String.valueOf(data.getAge()));
        drawStat(graphics, leftX, y + 28, "gender", Component.translatable("screen.rpgcharacter.gender." + data.getGender().name().toLowerCase()).getString());
        drawStat(graphics, rightX, y + 28, "height", data.getHeightCm() + " cm");
        drawStat(graphics, leftX, y + 56, "weight", String.format("%.1f kg", data.getWeightKg()));
        drawStat(graphics, rightX, y + 56, "body_type", Component.translatable("screen.rpgcharacter.body." + data.getBodyType().name().toLowerCase()).getString());
        drawWideStat(graphics, panelX + 22, y + 88, "traits", data.getTraits().toString());
        drawProgress(graphics, panelX + 22, panelY + 160, panelWidth - 44);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xE61B1712);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, 0xCC33271A);
        graphics.fill(x + 8, y + 28, x + width - 8, y + 29, 0xFF7A5A34);
    }

    private void drawStat(GuiGraphics graphics, int x, int y, String key, String value) {
        graphics.fill(x - 4, y - 3, x + 112, y + 21, 0x66302016);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter." + key), x, y, 0xBFA37A);
        graphics.drawString(font, value, x, y + 10, 0xFFFFFF);
    }

    private void drawWideStat(GuiGraphics graphics, int x, int y, String key, String value) {
        graphics.fill(x - 4, y - 3, x + 242, y + 21, 0x66302016);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter." + key), x, y, 0xBFA37A);
        graphics.drawString(font, value, x, y + 10, 0xFFFFFF);
    }

    private void drawProgress(GuiGraphics graphics, int x, int y, int width) {
        long threshold = Math.max(1L, CharacterConfig.AGE_PROGRESS_THRESHOLD.get());
        int filled = (int) Math.min(width - 2L, data.getAgeProgress() * (width - 2L) / threshold);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter.age_progress"), x, y - 12, 0xFFFFFF);
        graphics.fill(x, y, x + width, y + 10, 0xFF20140D);
        graphics.fill(x + 1, y + 1, x + 1 + filled, y + 9, 0xFF80B95A);
    }
}
