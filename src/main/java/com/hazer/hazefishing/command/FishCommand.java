package com.hazer.hazefishing.command;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.RodTier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class FishCommand implements CommandExecutor, TabCompleter {
    private final HazerFishingPlugin plugin;

    public FishCommand(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§e/fish rods|mint|auction|rod|nft");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "rods" -> sender.sendMessage("§7Total minted rods: " + plugin.getNftRodManager().allRods().size());
            case "mint" -> {
                RodTier tier = args.length > 1 ? RodTier.parse(args[1]) : RodTier.NFT_ROD;
                double cost = 1_000_000 * tier.rarityMultiplier();
                if (!plugin.getEconomyManager().withdraw(player, cost)) {
                    sender.sendMessage("§cNeed " + cost + " to mint");
                    return true;
                }
                var rod = plugin.getNftRodManager().mint(player, tier);
                player.getInventory().addItem(plugin.getNftRodManager().toItem(rod));
                sender.sendMessage("§aMinted rod " + rod.serial());
            }
            case "auction" -> sender.sendMessage("§7Active auctions: " + plugin.getAuctionManager().active().size());
            case "rod" -> sender.sendMessage("§7Hold rod and use /hf rod inspect");
            case "nft" -> sender.sendMessage("§7Use /fish nft registry");
            default -> sender.sendMessage("§cUnknown subcommand");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("rods", "mint", "auction", "rod", "nft");
        if (args.length == 2 && "mint".equalsIgnoreCase(args[0])) {
            return Arrays.stream(RodTier.values()).map(Enum::name).toList();
        }
        return List.of();
    }
}
