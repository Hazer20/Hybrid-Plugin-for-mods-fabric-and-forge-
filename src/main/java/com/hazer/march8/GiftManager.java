package com.hazer.march8;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Responsible for gifts, congratulation flow and sword special behavior.
 */
public final class GiftManager implements Listener {

    private final March8Plugin plugin;
    private final ConfigManager configManager;
    private final OraxenHook oraxenHook;

    private final NamespacedKey springBladeKey;

    private double heartChance;
    private double fireworkChance;

    public GiftManager(@NotNull March8Plugin plugin,
                       @NotNull ConfigManager configManager,
                       @NotNull OraxenHook oraxenHook) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.oraxenHook = oraxenHook;
        this.springBladeKey = new NamespacedKey(plugin, "spring_blade");
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        this.heartChance = clamp(configManager.getDouble("gifts.sword.heart-hit-chance", 0.30), 0.0, 1.0);
        this.fireworkChance = clamp(configManager.getDouble("gifts.sword.firework-hit-chance", 0.10), 0.0, 1.0);
    }

    public void congratulate(@NotNull Player player,
                             @NotNull ParticleManager particleManager,
                             @NotNull FireworkManager fireworkManager) {
        sendTitle(player);
        sendChatMessage(player);
        particleManager.startCelebrationParticles(player);
        fireworkManager.spawnStartFireworks(player);

        giveRoses(player);
        giveSpringBlade(player);
        particleManager.grantSpringAura(player);
    }

    public void runFinalGlobalMoment(@NotNull List<Player> recipients,
                                     @NotNull FireworkManager fireworkManager) {
        int delaySeconds = Math.max(0, configManager.getInt("fireworks.final-delay-seconds", 5));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Component finalBroadcast = configManager.getMessage("final-broadcast");
            Bukkit.broadcast(finalBroadcast);
            for (Player recipient : recipients) {
                if (recipient != null && recipient.isOnline()) {
                    fireworkManager.spawnFinalFireworks(recipient);
                }
            }
        }, delaySeconds * 20L);
    }

    private void sendTitle(@NotNull Player player) {
        String title = configManager.getString("celebration.title", "<pink><bold>С 8 Марта!</bold></pink>");
        String subtitle = configManager.getString("celebration.subtitle", "<white>От всех парней сервера DE Squad</white>");
        int fadeIn = configManager.getInt("celebration.title-fade-in", 10);
        int stay = configManager.getInt("celebration.title-stay", 70);
        int fadeOut = configManager.getInt("celebration.title-fade-out", 20);

        player.showTitle(net.kyori.adventure.title.Title.title(
                configManager.mm(title),
                configManager.mm(subtitle),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(fadeIn * 50L),
                        java.time.Duration.ofMillis(stay * 50L),
                        java.time.Duration.ofMillis(fadeOut * 50L)
                )
        ));
    }

    private void sendChatMessage(@NotNull Player player) {
        List<String> lines = configManager.getStringList("celebration.chat-lines");
        for (String line : lines) {
            player.sendMessage(configManager.mm(line));
        }
    }

    private void giveRoses(@NotNull Player player) {
        String materialName = configManager.getString("gifts.rose.material", "POPPY");
        int amount = Math.max(1, configManager.getInt("gifts.rose.amount", 101));
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.POPPY;
        }

        ItemStack roses = new ItemStack(material, amount);
        ItemMeta meta = roses.getItemMeta();
        if (meta != null) {
            meta.displayName(configManager.mm(configManager.getString("gifts.rose.name", "<pink><bold>101 Роза</bold></pink>")));
            meta.lore(configManager.mmList(configManager.getStringList("gifts.rose.lore")));
            roses.setItemMeta(meta);
        }
        giveItem(player, roses);
    }

    private void giveSpringBlade(@NotNull Player player) {
        ItemStack sword = oraxenHook.createSpringBlade();
        tagSpringBlade(sword);
        giveItem(player, sword);
    }

    private void tagSpringBlade(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(springBladeKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
    }

    private boolean isSpringBlade(@NotNull ItemStack stack) {
        if (stack.getType().isAir() || !stack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }

        Byte marker = meta.getPersistentDataContainer().get(springBladeKey, PersistentDataType.BYTE);
        if (marker != null && marker == 1) {
            return true;
        }

        // Compatibility with previously generated items.
        if (meta.displayName() != null) {
            String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName());
            String lowered = plain.toLowerCase(Locale.ROOT);
            return lowered.contains("клинок весны") || lowered.contains("spring blade");
        }

        return false;
    }

    private void giveItem(@NotNull Player player, @NotNull ItemStack item) {
        PlayerInventory inv = player.getInventory();
        var leftovers = inv.addItem(item);
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
        }
    }

    @EventHandler
    public void onSpringBladeDamage(EntityDamageByEntityEvent event) {
        Player attacker = extractPlayerDamager(event.getDamager());
        if (attacker == null) {
            return;
        }

        ItemStack hand = attacker.getInventory().getItemInMainHand();
        if (!isSpringBlade(hand)) {
            return;
        }

        Entity target = event.getEntity();
        Location targetLoc = target.getLocation().clone().add(0, target.getHeight() * 0.6, 0);

        if (roll(heartChance)) {
            spawnHearts(targetLoc, target instanceof LivingEntity living ? Math.max(10, (int) living.getHealth()) : 12);
        }

        if (roll(fireworkChance)) {
            plugin.getFireworkManager().spawnImpactFirework(targetLoc);
        }

        // Extra subtle spring sparkles on every hit.
        spawnSpringSparkles(targetLoc);
    }

    private Player extractPlayerDamager(@NotNull Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    private void spawnHearts(@NotNull Location around, int count) {
        around.getWorld().spawnParticle(Particle.HEART, around, count, 0.6, 0.8, 0.6, 0.02);
    }

    private void spawnSpringSparkles(@NotNull Location around) {
        around.getWorld().spawnParticle(Particle.CHERRY_LEAVES, around, 8, 0.4, 0.5, 0.4, 0.01);
        around.getWorld().spawnParticle(Particle.DUST, around, 8, 0.4, 0.6, 0.4, 0.01,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 120, 205), 1.3f));
    }

    private boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() <= chance;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public List<Player> congratulateOnlineGirls(@NotNull GirlManager girlManager,
                                                @NotNull ParticleManager particleManager,
                                                @NotNull FireworkManager fireworkManager) {
        List<Player> recipients = new ArrayList<>();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!girlManager.isGirl(online.getName())) {
                continue;
            }
            if (!online.hasPermission("march8.receive")) {
                Bukkit.getConsoleSender().sendMessage(configManager.getMessage("receive-required", "player", online.getName()));
                continue;
            }
            congratulate(online, particleManager, fireworkManager);
            recipients.add(online);
        }

        runFinalGlobalMoment(recipients, fireworkManager);
        return recipients;
    }
}
