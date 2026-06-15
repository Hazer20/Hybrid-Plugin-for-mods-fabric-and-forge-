package com.hybrid.rpgcharacter.client;

import com.hybrid.rpgcharacter.data.BodyType;
import com.hybrid.rpgcharacter.data.Gender;
import com.hybrid.rpgcharacter.network.CharacterNetwork;
import com.hybrid.rpgcharacter.network.SubmitCharacterCreationPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** First-login character creation screen for player-facing identity choices. */
public class CharacterCreationScreen extends Screen {
    private EditBox nameBox;
    private Gender selectedGender = Gender.MALE;
    private BodyType selectedBodyType = BodyType.NORMAL;

    public CharacterCreationScreen() {
        super(Component.translatable("screen.rpgcharacter.creation.title"));
    }

    @Override
    protected void init() {
        int panelX = width / 2 - 130;
        int panelY = height / 2 - 95;
        nameBox = new EditBox(font, panelX + 24, panelY + 42, 212, 20, Component.translatable("screen.rpgcharacter.creation.name"));
        nameBox.setMaxLength(32);
        nameBox.setValue(minecraft.getUser().getName());
        addRenderableWidget(nameBox);
        addRenderableWidget(Button.builder(Component.translatable("screen.rpgcharacter.gender.male"), button -> selectedGender = Gender.MALE)
                .pos(panelX + 24, panelY + 78).size(100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.rpgcharacter.gender.female"), button -> selectedGender = Gender.FEMALE)
                .pos(panelX + 136, panelY + 78).size(100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.rpgcharacter.body.normal"), button -> selectedBodyType = BodyType.NORMAL)
                .pos(panelX + 24, panelY + 112).size(100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.rpgcharacter.body.athletic"), button -> selectedBodyType = BodyType.ATHLETIC)
                .pos(panelX + 136, panelY + 112).size(100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.rpgcharacter.creation.confirm"), button -> submit())
                .pos(panelX + 24, panelY + 150).size(212, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelX = width / 2 - 130;
        int panelY = height / 2 - 95;
        graphics.fill(panelX, panelY, panelX + 260, panelY + 190, 0xDD1E1A14);
        graphics.fill(panelX + 4, panelY + 4, panelX + 256, panelY + 186, 0xAA3A2B1C);
        graphics.drawCenteredString(font, title, width / 2, panelY + 14, 0xF5D28A);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter.creation.name"), panelX + 24, panelY + 31, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter.creation.gender", selectedGender.name()), panelX + 24, panelY + 66, 0xE6D3A3);
        graphics.drawString(font, Component.translatable("screen.rpgcharacter.creation.body", selectedBodyType.name()), panelX + 24, panelY + 100, 0xE6D3A3);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void submit() {
        CharacterNetwork.CHANNEL.sendToServer(new SubmitCharacterCreationPacket(nameBox.getValue(), selectedGender, selectedBodyType));
    }
}
