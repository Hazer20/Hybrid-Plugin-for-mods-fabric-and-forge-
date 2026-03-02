package ru.desquad.hybrid.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.util.Message;

public class DESCoinCommand implements CommandExecutor {

    private final DESHybridPlugin plugin;
    private final EconomyManager economy;

    public DESCoinCommand(DESHybridPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Message.send(sender, "&eИспользование: /descoin <give|balance|pay>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "balance" -> handleBalance(sender, args);
            case "pay" -> handlePay(sender, args);
            default -> Message.send(sender, "&cНеизвестная подкоманда.");
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("descoin.admin")) {
            Message.sendConfig(sender, "descoin.messages.no-permission");
            return;
        }
        if (args.length < 3) {
            Message.send(sender, "&eИспользование: /descoin give <игрок> <кол-во>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Message.sendConfig(sender, "descoin.messages.player-not-found");
            return;
        }
        Double amount = parseAmount(args[2], sender);
        if (amount == null) return;

        economy.add(target.getUniqueId(), amount);
        Message.send(sender, plugin.getConfig().getString("descoin.messages.gave", "")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{target}", target.getName()));
        Message.send(target, plugin.getConfig().getString("descoin.messages.received", "")
                .replace("{amount}", String.format("%.2f", amount)));
    }

    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (!(sender instanceof Player player)) {
                Message.sendConfig(sender, "descoin.messages.only-players");
                return;
            }
            double balance = economy.getBalance(player.getUniqueId());
            Message.send(sender, plugin.getConfig().getString("descoin.messages.balance-self", "")
                    .replace("{amount}", String.format("%.2f", balance)));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Message.sendConfig(sender, "descoin.messages.player-not-found");
            return;
        }
        double balance = economy.getBalance(target.getUniqueId());
        Message.send(sender, plugin.getConfig().getString("descoin.messages.balance-other", "")
                .replace("{amount}", String.format("%.2f", balance))
                .replace("{target}", target.getName()));
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Message.sendConfig(sender, "descoin.messages.only-players");
            return;
        }
        if (args.length < 3) {
            Message.send(sender, "&eИспользование: /descoin pay <игрок> <кол-во>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Message.sendConfig(sender, "descoin.messages.player-not-found");
            return;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            Message.send(sender, "&cНельзя перевести самому себе.");
            return;
        }
        Double amount = parseAmount(args[2], sender);
        if (amount == null) return;

        boolean success = economy.transfer(player.getUniqueId(), target.getUniqueId(), amount);
        if (!success) {
            Message.sendConfig(sender, "descoin.messages.pay-not-enough");
            return;
        }
        Message.send(player, plugin.getConfig().getString("descoin.messages.pay-success-sender", "")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{target}", target.getName()));
        Message.send(target, plugin.getConfig().getString("descoin.messages.pay-success-target", "")
                .replace("{amount}", String.format("%.2f", amount))
                .replace("{sender}", player.getName()));
    }

    private Double parseAmount(String input, CommandSender sender) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                Message.sendConfig(sender, "descoin.messages.invalid-number");
                return null;
            }
            return amount;
        } catch (NumberFormatException ex) {
            Message.sendConfig(sender, "descoin.messages.invalid-number");
            return null;
        }
    }
}
