package com.hazer.march8;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles config.yml and messages.yml loading, defaults and typed getters.
 */
public final class ConfigManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private File messagesFile;
    private YamlConfiguration messagesConfig;

    public ConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reloadAll() {
        plugin.reloadConfig();
        ensureConfigDefaults(plugin.getConfig());
        plugin.saveConfig();
        loadMessages();
    }

    private void ensureConfigDefaults(@NotNull FileConfiguration config) {
        config.addDefault("author", "Hazer_2_0");
        config.addDefault("girls", new ArrayList<>());

        config.addDefault("oraxen.item-id", "love_blade");
        config.addDefault("oraxen.enabled-check", true);

        config.addDefault("celebration.title", "<gradient:#ff75c3:#ffd1ea><bold>С 8 Марта!</bold></gradient>");
        config.addDefault("celebration.subtitle", "<white>От всех парней сервера <gold><bold>DE Squad</bold></gold></white>");
        config.addDefault("celebration.title-fade-in", 10);
        config.addDefault("celebration.title-stay", 70);
        config.addDefault("celebration.title-fade-out", 20);

        List<String> chatLines = List.of(
                "<pink>От всех парней сервера поздравляем вас с Международным женским днём!</pink>",
                "<#ff9ad7>Пусть этот день будет ярким как фейерверки над сервером.</#ff9ad7>",
                "<#ffc5e8>Спасибо что вы делаете сервер лучше.</#ffc5e8>",
                "<gradient:#ff75c3:#ffd1ea><bold>С праздником!</bold></gradient>"
        );
        config.addDefault("celebration.chat-lines", chatLines);

        config.addDefault("effects.base-particles", List.of("HEART", "CHERRY_LEAVES", "FIREWORK", "GLOW"));
        config.addDefault("effects.spring-aura.interval-ticks", 40);
        config.addDefault("effects.spring-aura.duration-seconds", 300);
        config.addDefault("effects.spring-aura.particles", List.of("HEART", "CHERRY_LEAVES", "PINK_DUST"));
        config.addDefault("effects.sword-aura.interval-ticks", 10);
        config.addDefault("effects.sword-aura.particles", List.of("HEART", "PINK_DUST"));

        config.addDefault("fireworks.on-start.count", 3);
        config.addDefault("fireworks.final-delay-seconds", 5);
        config.addDefault("fireworks.final-overhead.count", 1);

        config.addDefault("gifts.rose.material", "POPPY");
        config.addDefault("gifts.rose.amount", 101);
        config.addDefault("gifts.rose.name", "<gradient:#ff6699:#ff99cc><bold>101 Роза</bold></gradient>");
        config.addDefault("gifts.rose.lore", List.of(
                "<white>Пусть каждая роза</white>",
                "<white>подарит улыбку</white>",
                "",
                "<gold>От игроков сервера DE Squad</gold>"
        ));

        config.addDefault("gifts.sword.fallback-material", "DIAMOND_SWORD");
        config.addDefault("gifts.sword.name", "<gradient:#ff99cc:#ff66b2><bold>Клинок Весны</bold></gradient>");
        config.addDefault("gifts.sword.lore", List.of(
                "<white>Подарок на 8 марта</white>",
                "<white>От всего сервера</white>",
                "",
                "<pink>Каждый удар несёт магию весны</pink>"
        ));
        config.addDefault("gifts.sword.heart-hit-chance", 0.30);
        config.addDefault("gifts.sword.firework-hit-chance", 0.10);

        config.options().copyDefaults(true);
    }

    private void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        ensureMessageDefaults(messagesConfig);
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save messages.yml: " + ex.getMessage());
        }
    }

    private void ensureMessageDefaults(@NotNull YamlConfiguration msg) {
        msg.addDefault("prefix", "<gray>[<gradient:#ff75c3:#ffd1ea><bold>March8</bold></gradient><gray>] </gray>");
        msg.addDefault("no-permission", "<red>У вас нет прав: <white>march8.admin</white></red>");
        msg.addDefault("usage", "<yellow>Использование: /march8 <add|remove|list|start|reload></yellow>");
        msg.addDefault("added", "<green>Игрок <white><player></white> добавлен в список девушек.</green>");
        msg.addDefault("already-added", "<yellow>Игрок <white><player></white> уже в списке.</yellow>");
        msg.addDefault("removed", "<green>Игрок <white><player></white> удалён из списка.</green>");
        msg.addDefault("not-found", "<red>Игрок <white><player></white> не найден в списке.</red>");
        msg.addDefault("list-header", "<gradient:#ff75c3:#ffd1ea><bold>Список девушек сервера:</bold></gradient>");
        msg.addDefault("list-empty", "<gray>Список пока пуст.</gray>");
        msg.addDefault("list-item", "<pink>• <white><player></white></pink>");
        msg.addDefault("reload-done", "<green>Конфигурация March8Plugin перезагружена.</green>");
        msg.addDefault("start-done", "<green>Поздравление запущено для всех онлайн девушек.</green>");
        msg.addDefault("receive-required", "<yellow>Игроку <white><player></white> не хватает права march8.receive.</yellow>");
        msg.addDefault("final-broadcast", "<gradient:#ff75c3:#ffd1ea><bold>Все парни сервера поздравляют наших прекрасных девушек с 8 Марта!</bold></gradient>");
        msg.options().copyDefaults(true);
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public YamlConfiguration getMessages() {
        return messagesConfig;
    }

    public Component getMessage(@NotNull String path) {
        String prefix = messagesConfig.getString("prefix", "");
        String body = messagesConfig.getString(path, "<red>Missing message: " + path + "</red>");
        return MINI_MESSAGE.deserialize(prefix + body);
    }

    public Component getMessage(@NotNull String path, @NotNull String token, @NotNull String value) {
        String prefix = messagesConfig.getString("prefix", "");
        String body = messagesConfig.getString(path, "<red>Missing message: " + path + "</red>")
                .replace("<" + token + ">", value);
        return MINI_MESSAGE.deserialize(prefix + body);
    }

    public Component mm(@NotNull String raw) {
        return MINI_MESSAGE.deserialize(raw);
    }

    public List<Component> mmList(@NotNull List<String> lines) {
        List<Component> parsed = new ArrayList<>();
        for (String line : lines) {
            parsed.add(mm(line));
        }
        return parsed;
    }

    public List<String> getGirls() {
        return new ArrayList<>(plugin.getConfig().getStringList("girls"));
    }

    public void setGirls(@NotNull List<String> girls) {
        plugin.getConfig().set("girls", girls);
        plugin.saveConfig();
    }

    public List<String> getStringList(@NotNull String path) {
        List<String> value = plugin.getConfig().getStringList(path);
        return value == null ? Collections.emptyList() : value;
    }

    public String getString(@NotNull String path, @NotNull String fallback) {
        return plugin.getConfig().getString(path, fallback);
    }

    public int getInt(@NotNull String path, int fallback) {
        return plugin.getConfig().getInt(path, fallback);
    }

    public double getDouble(@NotNull String path, double fallback) {
        return plugin.getConfig().getDouble(path, fallback);
    }

    public boolean getBoolean(@NotNull String path, boolean fallback) {
        return plugin.getConfig().getBoolean(path, fallback);
    }
}
