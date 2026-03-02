package ru.desquad.hybrid.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import ru.desquad.hybrid.DESHybridPlugin;

public final class Message {

    private static DESHybridPlugin plugin;
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private Message() {}

    public static void init(DESHybridPlugin pl) {
        plugin = pl;
    }

    public static String raw(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public static String color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(SERIALIZER.deserialize(text));
    }

    public static Component component(String text) {
        return SERIALIZER.deserialize(text);
    }

    public static void send(CommandSender sender, String text) {
        sender.sendMessage(component(text));
    }

    public static void sendConfig(CommandSender sender, String path) {
        String prefix = plugin.getConfig().getString("descoin.messages.prefix", "");
        send(sender, prefix + plugin.getConfig().getString(path, ""));
    }
}
