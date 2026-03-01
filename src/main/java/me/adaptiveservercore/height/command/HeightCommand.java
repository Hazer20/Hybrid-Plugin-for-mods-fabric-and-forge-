package me.adaptiveservercore.height.command;

import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.height.HeightManager;
import me.adaptiveservercore.height.gui.HeightGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HeightCommand implements CommandExecutor, TabCompleter {

    private final HeightManager heightManager;
    private final HeightGui heightGui;

    public HeightCommand(AdaptiveServerCore plugin, HeightManager heightManager, HeightGui heightGui) {
        this.heightManager = heightManager;
        this.heightGui = heightGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда доступна только игроку.");
            return true;
        }

        if (!player.hasPermission("height.use")) {
            player.sendMessage("§cУ вас нет прав на использование этой команды.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eИспользование: /height <значение|reset|gui|info>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reset" -> {
                if (heightManager.resetHeight(player)) {
                    player.sendMessage("§aРост сброшен до стандартного значения §f1.8§a.");
                }
            }
            case "gui" -> heightGui.open(player);
            case "info" -> sendInfo(player);
            default -> {
                Double parsed = heightManager.resolveNamedHeight(sub);
                if (parsed == null) {
                    try {
                        parsed = Double.parseDouble(sub.replace(',', '.'));
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНеверный формат роста. Пример: §f/height 1.25");
                        return true;
                    }
                }

                if (heightManager.setHeight(player, parsed)) {
                    player.sendMessage("§aНовый рост установлен: §f" + String.format(Locale.US, "%.2f", parsed));
                }
            }
        }

        return true;
    }

    private void sendInfo(Player player) {
        double height = heightManager.getHeight(player);
        double scale = height / HeightManager.DEFAULT_HEIGHT;
        double hitboxHeight = 1.8D * scale;
        double hitboxWidth = 0.6D * scale;
        double eyeHeight = 1.62D * scale;

        player.sendMessage("§6§m------------------------------");
        player.sendMessage("§eТекущий рост: §f" + String.format(Locale.US, "%.2f", height));
        player.sendMessage("§eРазмер хитбокса: §f" + String.format(Locale.US, "%.2f", hitboxWidth) + " x "
                + String.format(Locale.US, "%.2f", hitboxHeight));
        player.sendMessage("§eВысота глаз: §f" + String.format(Locale.US, "%.2f", eyeHeight));
        player.sendMessage("§eДальность атаки: §f" + String.format(Locale.US, "%.2f", heightManager.getAttackReach(player)));
        player.sendMessage("§6§m------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = List.of("reset", "gui", "info", "0.5", "1.0", "1.8", "2.5");
            List<String> result = new ArrayList<>();
            for (String option : base) {
                if (option.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    result.add(option);
                }
            }
            return result;
        }
        return List.of();
    }
}
