package ru.desquad.hybrid.command;

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
}
