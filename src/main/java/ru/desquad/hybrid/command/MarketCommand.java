package ru.desquad.hybrid.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.desquad.hybrid.gui.GUIFactory;
import ru.desquad.hybrid.market.MarketManager;

public class MarketCommand implements CommandExecutor {

    private final MarketManager marketManager;

    public MarketCommand(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        player.openInventory(GUIFactory.createMarketGUI(player, marketManager, null, null, null, null));
        return true;
    }
}
