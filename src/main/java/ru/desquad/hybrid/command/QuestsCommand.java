package ru.desquad.hybrid.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.desquad.hybrid.gui.GUIFactory;
import ru.desquad.hybrid.quest.QuestManager;

public class QuestsCommand implements CommandExecutor {

    private final QuestManager questManager;

    public QuestsCommand(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        questManager.ensureQuests(player);
        player.openInventory(GUIFactory.createQuestGUI(player, questManager));
        return true;
    }
}
