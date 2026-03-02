package com.hazer.hazefishing.command;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import com.hazer.hazefishing.model.NFTRod;
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
    private final Map<Rarity, Double> chanceOverrides = new ConcurrentHashMap<>();
    private volatile boolean debugPerformance;

    public AdminCommandManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isDebugPerformance() {
        return debugPerformance;
    }

    public boolean toggleDebug() {
        debugPerformance = !debugPerformance;
        return debugPerformance;
    }

    public void runQuickSimulation(CommandSender sender, int amount) {
        simulate(sender, new String[]{String.valueOf(amount)});
    }

    public double getChance(Rarity rarity) {
        return chanceOverrides.getOrDefault(rarity, rarity.getChance());
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hazefishing.admin")) {
            sender.sendMessage("§cУ вас нет прав.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§e/hf test|mint|give|rod|chance|event|boss|nft|debug|stats|simulate|panel");
            return true;
        }
        if (sender instanceof Player player && !checkCooldown(player.getUniqueId())) {
            sender.sendMessage("§cПодождите перед следующей админ-командой.");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "debug" -> debug(sender, Arrays.copyOfRange(args, 1, args.length));
            case "test" -> test(sender, Arrays.copyOfRange(args, 1, args.length));
            case "simulate" -> simulate(sender, Arrays.copyOfRange(args, 1, args.length));
            case "mint" -> mint(sender, Arrays.copyOfRange(args, 1, args.length));
            case "give" -> give(sender, Arrays.copyOfRange(args, 1, args.length));
            case "chance" -> chance(sender, Arrays.copyOfRange(args, 1, args.length));
            case "stats" -> stats(sender, Arrays.copyOfRange(args, 1, args.length));
            case "nft" -> nft(sender, Arrays.copyOfRange(args, 1, args.length));
            case "event" -> event(sender, Arrays.copyOfRange(args, 1, args.length));
            case "panel" -> {
                if (sender instanceof Player player) plugin.getAdminPanel().open(player);
                else sender.sendMessage("§cПанель доступна только игроку.");
            }
            default -> sender.sendMessage("§cНеизвестная подкоманда.");
        }
        plugin.getAdminAuditLogger().log(sender.getName(), String.join(" ", args));
        return true;
    }

    private void mint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок.");
            return;
        }
        if (args.length < 2 || !"testrod".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§e/hf mint testrod <редкость_удочки>");
            return;
        }
        RodTier tier = RodTier.parse(args[1]);
        NFTRod rod = plugin.getNftRodManager().mint(player, tier);
        player.getInventory().addItem(plugin.getNftRodManager().toItem(rod));
        player.sendMessage("§aСоздана удочка: " + rod.serial());
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 3 || !"rod".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§e/hf give rod <редкость_удочки> <игрок>");
            return;
        }
        RodTier tier = RodTier.parse(args[1]);
        Player target = resolveTarget(sender, args[2]);
        if (target == null) return;
        NFTRod rod = plugin.getNftRodManager().mint(target, tier);
        target.getInventory().addItem(plugin.getNftRodManager().toItem(rod));
        sender.sendMessage("§aУдочка выдана игроку " + target.getName());
    }

    private void debug(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/hf debug on|off|performance");
            return;
        }
        switch (args[0]) {
            case "on" -> {
                debugPerformance = true;
                sender.sendMessage("§aDebug включён");
            }
            case "off" -> {
                debugPerformance = false;
                sender.sendMessage("§cDebug выключен");
            }
            case "performance" -> sender.sendMessage("§7debug.performance=" + debugPerformance);
            default -> sender.sendMessage("§cНеизвестный режим debug.");
        }
    }

    private void chance(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/hf chance set <rarity> <value> | boost <rarity> <x> | reset | info");
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /hf chance set <rarity> <value>");
                    return;
                }
                Rarity r = Rarity.parse(args[1]);
                double v = Double.parseDouble(args[2]);
                chanceOverrides.put(r, v);
                sender.sendMessage("§aШанс для " + r + " установлен на " + v + "%");
            }
            case "boost" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /hf chance boost <rarity> <x>");
                    return;
                }
                Rarity r = Rarity.parse(args[1]);
                double m = Double.parseDouble(args[2]);
                chanceOverrides.put(r, getChance(r) * m);
                sender.sendMessage("§aШанс для " + r + " умножен на " + m);
            }
            case "reset" -> {
                chanceOverrides.clear();
                sender.sendMessage("§aВсе live-шансы сброшены.");
            }
            case "info" -> {
                sender.sendMessage("§6Текущие шансы:");
                for (Rarity rarity : Rarity.values()) {
                    sender.sendMessage("§7- " + rarity + ": " + getChance(rarity) + "%");
                }
            }
            default -> sender.sendMessage("§cНеизвестная подкоманда chance.");
        }
    }

    private void stats(CommandSender sender, String[] args) {
        String type = args.length == 0 ? "global" : args[0].toLowerCase(Locale.ROOT);
        switch (type) {
            case "global" -> sender.sendMessage("§a[Статистика] Удочек: " + plugin.getNftRodManager().allRods().size() + ", NFT рыб: " + plugin.getNftFishManager().topRarest(Integer.MAX_VALUE).size());
            case "nft" -> sender.sendMessage("§d[Статистика NFT] Последняя рыба: " + plugin.getNftFishManager().lastCaught().map(f -> "#" + f.registryId()).orElse("нет"));
            case "performance" -> sender.sendMessage("§b[Статистика] Debug.performance=" + debugPerformance);
            default -> sender.sendMessage("§e/hf stats global|nft|performance");
        }
    }

    private void nft(CommandSender sender, String[] args) {
        if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§dТоп NFT рыб:");
            for (NFTFish fish : plugin.getNftFishManager().topRarest(5)) {
                sender.sendMessage("§7#" + fish.registryId() + " | " + fish.rarity() + " | " + String.format(Locale.US, "%.2f", fish.rarityFactor()));
            }
            return;
        }
        if ("inspect".equalsIgnoreCase(args[0]) && args.length >= 2) {
            long id = Long.parseLong(args[1]);
            plugin.getNftFishManager().byId(id).ifPresentOrElse(
                    fish -> sender.sendMessage("§dNFT #" + fish.registryId() + " вес=" + String.format(Locale.US, "%.2f", fish.weight()) + "г владелец=" + fish.owner()),
                    () -> sender.sendMessage("§cNFT с таким ID не найден.")
            );
            return;
        }
        sender.sendMessage("§e/hf nft list | inspect <id>");
    }

    private void event(CommandSender sender, String[] args) {
        if (args.length == 0 || "list".equalsIgnoreCase(args[0])) {
            sender.sendMessage("§7События: nft_legend, storm, lightning");
            return;
        }
        if ("force".equalsIgnoreCase(args[0]) && args.length >= 2 && sender instanceof Player player) {
            if ("lightning".equalsIgnoreCase(args[1])) {
                player.getWorld().strikeLightningEffect(player.getLocation());
                sender.sendMessage("§eМолния вызвана.");
                return;
            }
        }
        sender.sendMessage("§e/hf event list | force lightning");
    }

    private void test(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/hf test fish <редкость> [игрок] | nftfish [игрок]");
            return;
        }
        if ("fish".equalsIgnoreCase(args[0]) && args.length >= 2) {
            Player target = resolveTarget(sender, args.length >= 3 ? args[2] : sender.getName());
            if (target == null) return;
            Rarity rarity = Rarity.parse(args[1]);
            NFTFish fish = plugin.getNftFishManager().generate(target, rarity);
            target.getInventory().addItem(plugin.getNftFishManager().toItem(fish));
            sender.sendMessage("§aВыдана рыба #" + fish.registryId() + " редкость " + rarity);
            plugin.getRgbAnimationManager().triggerFishAnimation(target, "ТЕСТ " + rarity + " РЫБА");
        } else if ("nftfish".equalsIgnoreCase(args[0])) {
            Player target = resolveTarget(sender, args.length >= 2 ? args[1] : sender.getName());
            if (target == null) return;
            NFTFish fish = plugin.getNftFishManager().generate(target, Rarity.NFT);
            target.getInventory().addItem(plugin.getNftFishManager().toItem(fish));
            plugin.getEventManager().broadcastNftCatch(target, fish);
            sender.sendMessage("§dNFT-рыба выдана: #" + fish.registryId());
        }
    }

    private void simulate(CommandSender sender, String[] args) {
        int amount = args.length > 0 ? Math.max(1, Math.min(100000, Integer.parseInt(args[0]))) : 1000;
        sender.sendMessage("§7Симуляция начата: " + amount);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Rarity, Integer> hits = new EnumMap<>(Rarity.class);
            Random random = new Random();
            for (int i = 0; i < amount; i++) {
                double roll = random.nextDouble(100.0);
                double cursor = 0;
                for (Rarity rarity : Rarity.values()) {
                    cursor += getChance(rarity);
                    if (roll <= cursor) {
                        hits.merge(rarity, 1, Integer::sum);
                        break;
                    }
                }
            }
            sender.sendMessage("§aИтоги симуляции: " + hits);
        });
    }

    private Player resolveTarget(CommandSender sender, String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            sender.sendMessage("§cИгрок не найден: " + name);
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
        if (args.length == 2 && "mint".equalsIgnoreCase(args[0])) return List.of("testrod");
        if (args.length == 3 && "mint".equalsIgnoreCase(args[0]) && "testrod".equalsIgnoreCase(args[1])) return Arrays.stream(RodTier.values()).map(Enum::name).toList();
        if (args.length == 2 && "give".equalsIgnoreCase(args[0])) return List.of("rod");
        if (args.length == 3 && "give".equalsIgnoreCase(args[0]) && "rod".equalsIgnoreCase(args[1])) return Arrays.stream(RodTier.values()).map(Enum::name).toList();
        if (args.length == 2 && "chance".equalsIgnoreCase(args[0])) return List.of("set", "boost", "reset", "info");
        if (args.length == 3 && "chance".equalsIgnoreCase(args[0])) return Arrays.stream(Rarity.values()).map(Enum::name).toList();
        if (args.length == 2 && "stats".equalsIgnoreCase(args[0])) return List.of("global", "nft", "performance");
        if (args.length == 2 && "nft".equalsIgnoreCase(args[0])) return List.of("list", "inspect");
        return List.of();
    }
}
