package com.hazer2_0.radio;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class RadioCommand implements CommandExecutor {

    private final NamespacedKey radioChannelKey;

    public RadioCommand(JavaPlugin plugin) {
        this.radioChannelKey = new NamespacedKey(plugin, "hazer_radio_channel");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }
        if (!player.hasPermission("hazer.radio.admin")) {
            player.sendMessage("Недостаточно прав.");
            return true;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            player.sendMessage("Использование: /radio give <канал>");
            return true;
        }

        String channel = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (channel.isEmpty()) {
            player.sendMessage("Название канала не может быть пустым.");
            return true;
        }

        ItemStack radio = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta meta = radio.getItemMeta();
        meta.setDisplayName("§aРация §7[" + channel + "]");
        meta.getPersistentDataContainer().set(radioChannelKey, PersistentDataType.STRING, channel);
        radio.setItemMeta(meta);

        player.getInventory().addItem(radio);
        player.sendMessage("§aВы получили рацию канала: §f" + channel);
        return true;
    }
}
