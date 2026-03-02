package com.hazer.hazefishing.command;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.RodTier;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class FishCommand implements CommandExecutor, TabCompleter {
    private final HazerFishingPlugin plugin;

    public FishCommand(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманда доступна только игрокам.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§e/fish rods|mint <tier>|auction|rod info|nft registry");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "rods" -> sender.sendMessage("§7Всего создано удочек: " + plugin.getNftRodManager().allRods().size());
            case "mint" -> mint(player, args);
            case "auction" -> sender.sendMessage("§7Активных аукционов: " + plugin.getAuctionManager().active().size());
            case "rod" -> sender.sendMessage("§7Подсказка: /hf rod inspect <игрок> (в разработке)");
            case "nft" -> {
                if (args.length >= 2 && "registry".equalsIgnoreCase(args[1])) {
                    sender.sendMessage("§d§lNFT Реестр:");
                    plugin.getNftFishManager().lastCaught().ifPresentOrElse(
                            fish -> sender.sendMessage("§7Последняя: #" + fish.registryId() + " | владелец=" + fish.owner()),
                            () -> sender.sendMessage("§7Последняя: нет данных")
                    );
                    sender.sendMessage("§7Топ редких:");
                    plugin.getNftFishManager().topRarest(3).forEach(f ->
                            sender.sendMessage("§8- §f#" + f.registryId() + " §7(" + String.format("%.2f", f.rarityFactor()) + ")"));
                } else {
                    sender.sendMessage("§7Используйте: /fish nft registry");
                }
            }
            default -> sender.sendMessage("§cНеизвестная подкоманда.");
        }
        return true;
    }

    private void mint(Player player, String[] args) {
        RodTier tier = args.length > 1 ? RodTier.parse(args[1]) : RodTier.RARE_ROD;

        if (!checkRequirements(player, tier)) {
            player.sendMessage("§cНедостаточно ресурсов для крафта: " + tier.name());
            player.sendMessage("§7" + plugin.getNftRodManager().requirementsDescription(tier));
            return;
        }

        double cost = 1_000_000 * tier.rarityMultiplier();
        if (plugin.getEconomyManager().enabled() && !plugin.getEconomyManager().withdraw(player, cost)) {
            player.sendMessage("§cНедостаточно денег. Нужно: " + String.format("%.0f", cost));
            return;
        }

        consumeRequirements(player, tier);

        if (!craftSuccess(tier)) {
            player.sendMessage("§4Ковка сорвалась! Ресурсы были потрачены.");
            return;
        }

        var rod = plugin.getNftRodManager().mint(player, tier);
        player.getInventory().addItem(plugin.getNftRodManager().toItem(rod));
        player.sendMessage("§aВы создали NFT-удочку " + rod.serial());
    }

    private boolean checkRequirements(Player player, RodTier tier) {
        return switch (tier) {
            case RARE_ROD -> player.getLevel() >= 20 && has(player, Material.COD, 64);
            case EPIC_ROD -> player.getLevel() >= 35 && has(player, Material.SALMON, 64) && has(player, Material.PRISMARINE_CRYSTALS, 8);
            case LEGENDARY_ROD -> player.getLevel() >= 50 && has(player, Material.NETHERITE_INGOT, 3) && has(player, Material.HEART_OF_THE_SEA, 1);
            case MYTHIC_ROD -> player.getLevel() >= 65 && has(player, Material.NETHERITE_INGOT, 6) && has(player, Material.HEART_OF_THE_SEA, 2);
            case GODLIKE_ROD -> player.getLevel() >= 80 && has(player, Material.NETHERITE_INGOT, 8) && has(player, Material.NETHER_STAR, 1);
            case DIVINE_ROD -> player.getLevel() >= 100 && has(player, Material.NETHERITE_INGOT, 12) && has(player, Material.NETHER_STAR, 2);
            case CELESTIAL_ROD -> player.getLevel() >= 120 && has(player, Material.NETHERITE_INGOT, 16) && has(player, Material.NETHER_STAR, 3);
            case VOID_ROD -> player.getLevel() >= 150 && has(player, Material.NETHERITE_INGOT, 24) && has(player, Material.NETHER_STAR, 5);
            case NFT_ROD -> player.getLevel() >= 200 && has(player, Material.NETHERITE_INGOT, 32) && has(player, Material.NETHER_STAR, 8) && has(player, Material.DRAGON_EGG, 1);
        };
    }

    private void consumeRequirements(Player player, RodTier tier) {
        switch (tier) {
            case RARE_ROD -> {
                take(player, Material.COD, 64); player.setLevel(Math.max(0, player.getLevel() - 20));
            }
            case EPIC_ROD -> {
                take(player, Material.SALMON, 64); take(player, Material.PRISMARINE_CRYSTALS, 8); player.setLevel(Math.max(0, player.getLevel() - 35));
            }
            case LEGENDARY_ROD -> {
                take(player, Material.NETHERITE_INGOT, 3); take(player, Material.HEART_OF_THE_SEA, 1); player.setLevel(Math.max(0, player.getLevel() - 50));
            }
            case MYTHIC_ROD -> {
                take(player, Material.NETHERITE_INGOT, 6); take(player, Material.HEART_OF_THE_SEA, 2); player.setLevel(Math.max(0, player.getLevel() - 65));
            }
            case GODLIKE_ROD -> {
                take(player, Material.NETHERITE_INGOT, 8); take(player, Material.NETHER_STAR, 1); player.setLevel(Math.max(0, player.getLevel() - 80));
            }
            case DIVINE_ROD -> {
                take(player, Material.NETHERITE_INGOT, 12); take(player, Material.NETHER_STAR, 2); player.setLevel(Math.max(0, player.getLevel() - 100));
            }
            case CELESTIAL_ROD -> {
                take(player, Material.NETHERITE_INGOT, 16); take(player, Material.NETHER_STAR, 3); player.setLevel(Math.max(0, player.getLevel() - 120));
            }
            case VOID_ROD -> {
                take(player, Material.NETHERITE_INGOT, 24); take(player, Material.NETHER_STAR, 5); player.setLevel(Math.max(0, player.getLevel() - 150));
            }
            case NFT_ROD -> {
                take(player, Material.NETHERITE_INGOT, 32); take(player, Material.NETHER_STAR, 8); take(player, Material.DRAGON_EGG, 1); player.setLevel(Math.max(0, player.getLevel() - 200));
            }
        }
    }

    private boolean craftSuccess(RodTier tier) {
        double chance = switch (tier) {
            case RARE_ROD -> 0.95;
            case EPIC_ROD -> 0.80;
            case LEGENDARY_ROD -> 0.65;
            case MYTHIC_ROD -> 0.50;
            case GODLIKE_ROD -> 0.35;
            case DIVINE_ROD -> 0.25;
            case CELESTIAL_ROD -> 0.16;
            case VOID_ROD -> 0.08;
            case NFT_ROD -> 0.03;
        };
        return ThreadLocalRandom.current().nextDouble() <= chance;
    }

    private boolean has(Player player, Material material, int amount) {
        return player.getInventory().all(material).values().stream().mapToInt(ItemStack::getAmount).sum() >= amount;
    }

    private void take(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != material) continue;
            int take = Math.min(item.getAmount(), remaining);
            item.setAmount(item.getAmount() - take);
            remaining -= take;
            if (remaining <= 0) break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("rods", "mint", "auction", "rod", "nft");
        if (args.length == 2 && "mint".equalsIgnoreCase(args[0])) {
            return Arrays.stream(RodTier.values()).map(Enum::name).toList();
        }
        if (args.length == 3 && "nft".equalsIgnoreCase(args[0])) {
            return List.of("registry");
        }
        return List.of();
    }
}
