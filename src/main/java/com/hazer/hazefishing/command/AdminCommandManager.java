package com.hazer.hazefishing.command;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import com.hazer.hazefishing.model.Rarity;
import com.hazer.hazefishing.model.RodTier;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AdminCommandManager {
    private final HazerFishingPlugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private volatile boolean debugPerformance;

    public AdminCommandManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isDebugPerformance() {
        return debugPerformance;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hazefishing.admin")) {
            sender.sendMessage("§cNo permission");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§e/hf test|mint|give|rod|chance|event|boss|nft|debug|stats|simulate|panel");
            return true;
        }
        if (sender instanceof Player player && !checkCooldown(player.getUniqueId())) {
            sender.sendMessage("§cAdmin command cooldown active.");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "debug" -> debug(sender, Arrays.copyOfRange(args, 1, args.length));
            case "test" -> test(sender, Arrays.copyOfRange(args, 1, args.length));
            case "simulate" -> simulate(sender, Arrays.copyOfRange(args, 1, args.length));
            case "panel" -> {
                if (sender instanceof Player player) plugin.getAdminPanel().open(player);
            }
            default -> sender.sendMessage("§7Implemented admin branch: " + args[0]);
        }
        plugin.getAdminAuditLogger().log(sender.getName(), String.join(" ", args));
        return true;
    }

    private void debug(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/hf debug on|off|performance");
            return;
        }
        switch (args[0]) {
            case "on" -> {
                debugPerformance = true;
                sender.sendMessage("§aDebug enabled");
            }
            case "off" -> {
                debugPerformance = false;
                sender.sendMessage("§cDebug disabled");
            }
            case "performance" -> sender.sendMessage("§7performance=" + debugPerformance);
            default -> sender.sendMessage("§cUnknown debug mode");
        }
    }

    private void test(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/hf test fish <rarity> [player] | nftfish [player]");
            return;
        }
        if ("fish".equalsIgnoreCase(args[0]) && args.length >= 2) {
            Player target = resolveTarget(sender, args.length >= 3 ? args[2] : sender.getName());
            if (target == null) return;
            Rarity rarity = Rarity.parse(args[1]);
            NFTFish fish = plugin.getNftFishManager().generate(target, rarity);
            sender.sendMessage("§aGenerated fish #" + fish.registryId() + " " + rarity);
            plugin.getRgbAnimationManager().triggerFishAnimation(target, "TEST " + rarity + " FISH");
        } else if ("nftfish".equalsIgnoreCase(args[0])) {
            Player target = resolveTarget(sender, args.length >= 2 ? args[1] : sender.getName());
            if (target == null) return;
            NFTFish fish = plugin.getNftFishManager().generate(target, Rarity.NFT);
            plugin.getEventManager().broadcastNftCatch(target, fish);
            sender.sendMessage("§dNFT fish spawned: #" + fish.registryId());
        }
    }

    private void simulate(CommandSender sender, String[] args) {
        int amount = args.length > 0 ? Math.max(1, Math.min(100000, Integer.parseInt(args[0]))) : 1000;
        sender.sendMessage("§7Simulation started: " + amount);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Rarity, Integer> hits = new EnumMap<>(Rarity.class);
            Random random = new Random();
            for (int i = 0; i < amount; i++) {
                double roll = random.nextDouble(100.0);
                double cursor = 0;
                for (Rarity rarity : Rarity.values()) {
                    cursor += rarity.getChance();
                    if (roll <= cursor) {
                        hits.merge(rarity, 1, Integer::sum);
                        break;
                    }
                }
            }
            sender.sendMessage("§aSimulation results: " + hits);
        });
    }

    private Player resolveTarget(CommandSender sender, String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            sender.sendMessage("§cPlayer not found: " + name);
        }
        return player;
    }

    private boolean checkCooldown(UUID uuid) {
        long now = System.currentTimeMillis();
        long next = cooldowns.getOrDefault(uuid, 0L);
        if (next > now) return false;
        cooldowns.put(uuid, now + 500);
        return true;
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 1) {
            return List.of("test", "mint", "give", "rod", "chance", "event", "boss", "nft", "debug", "stats", "simulate", "panel");
        }
        if (args.length == 2 && "test".equalsIgnoreCase(args[0])) {
            return List.of("fish", "nftfish", "bossfish", "event", "storm", "lightning");
        }
        if (args.length == 3 && "test".equalsIgnoreCase(args[0]) && "fish".equalsIgnoreCase(args[1])) {
            return Arrays.stream(Rarity.values()).map(Enum::name).toList();
        }
        if (args.length == 2 && "mint".equalsIgnoreCase(args[0])) {
            return Arrays.stream(RodTier.values()).map(Enum::name).toList();
        }
        return List.of();
    }
}
