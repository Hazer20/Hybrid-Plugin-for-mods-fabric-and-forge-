package ru.desquad.hybrid.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.gui.GUIFactory;
import ru.desquad.hybrid.npc.BuilderNPCManager;

public class NPCCraftCommand implements CommandExecutor {

    private final DESHybridPlugin plugin;
    private final BuilderNPCManager npcManager;

    public NPCCraftCommand(DESHybridPlugin plugin, BuilderNPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            return handleAdmin(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (!plugin.getConfig().getBoolean("npc-craft.enabled", true)) {
            player.sendMessage("§cСистема крафта NPC отключена администратором.");
            return true;
        }
        if (npcManager.hasCraftedNpcToken(player.getUniqueId()) && plugin.getConfig().getBoolean("npc-craft.one-time-per-player", true)) {
            player.sendMessage("§cТы уже создавал предмет призыва NPC. Повторно нельзя.");
            return true;
        }
        player.openInventory(GUIFactory.createNpcCraftGUI(player));
        return true;
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("desnpccraft.admin") && !sender.isOp()) {
            sender.sendMessage("§cНет прав. Нужен OP или desnpccraft.admin");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§eИспользование: /desnpccraft admin <reset|give> <игрок>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден онлайн.");
            return true;
        }

        if (args[1].equalsIgnoreCase("reset")) {
            npcManager.setCraftedNpcToken(target.getUniqueId(), false);
            sender.sendMessage("§aЛимит крафта NPC сброшен для " + target.getName());
            target.sendMessage("§eТвой лимит крафта NPC был сброшен администратором.");
            return true;
        }

        if (args[1].equalsIgnoreCase("give")) {
            target.getInventory().addItem(GUIFactory.createNpcTokenItem());
            sender.sendMessage("§aВыдан спавнер NPC игроку " + target.getName());
            target.sendMessage("§aАдминистратор выдал тебе спавнер NPC.");
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда. Используй reset или give.");
        return true;
    }
}
