package me.yourname.freezeplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FreezeTask implements Runnable {

    private static final int CHECK_RADIUS = 6;
    private static final int CHECK_RADIUS_SQUARED = CHECK_RADIUS * CHECK_RADIUS;

    private static final double MAX_FREEZE = 100.0;
    private static final int HEART_BAR_COUNT = 10;

    private static final double BASE_COLD_GAIN = 0.95;
    private static final double SNOW_STORM_BONUS = 0.30;
    private static final double WET_CLOTHES_COLD_GAIN = 0.08;
    private static final double NATURAL_WARMUP = 0.50;
    private static final double SHELTER_WARMUP = 0.80;

    private static final int EXTREME_DAMAGE_INTERVAL = 4;

    private final Map<UUID, Double> freezeLevels = new HashMap<>();
    private final Map<UUID, Integer> damageTicks = new HashMap<>();
    private final Map<UUID, Integer> lastStage = new HashMap<>();

    @Override
    public void run() {
        Set<UUID> onlineIds = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            onlineIds.add(id);

            if (player.isDead()) {
                freezeLevels.put(id, 0.0);
                damageTicks.put(id, 0);
                lastStage.put(id, 0);
                continue;
            }

            double previousFreeze = freezeLevels.getOrDefault(id, 0.0);
            double delta = 0.0;

            boolean shelter = hasShelter(player);
            boolean winterBiome = isWinterBiome(player.getLocation().getBlock().getBiome());
            boolean snowingNow = winterBiome && isSnowingAtPlayer(player);
            boolean standingOnSnow = isStandingOnSnow(player);

            boolean climateCold = winterBiome && (standingOnSnow || snowingNow);

            if (shelter) {
                climateCold = false;
            }

            if (climateCold) {
                delta += BASE_COLD_GAIN;

                if (snowingNow) {
                    delta += SNOW_STORM_BONUS;
                }

                delta -= getArmorWarmth(player);
            }

            boolean wetClothesCold = winterBiome && !shelter && (player.isInWater() || player.isInRain());
            if (wetClothesCold) {
                delta += WET_CLOTHES_COLD_GAIN;
            }

            delta += getHeatFromNearbyBlocks(player);
            delta += getHeatFromHeldItems(player);

            if (!climateCold && delta >= 0) {
                delta -= NATURAL_WARMUP;
                if (shelter) {
                    delta -= SHELTER_WARMUP;
                }
            }

            if (player.isSprinting()) {
                delta -= 0.10;
            }

            double freeze = clamp(previousFreeze + delta, 0.0, MAX_FREEZE);
            freezeLevels.put(id, freeze);

            applyEffects(player, previousFreeze, freeze, wetClothesCold);
        }

        cleanupOfflinePlayers(onlineIds);
    }

    private void cleanupOfflinePlayers(Set<UUID> onlineIds) {
        freezeLevels.keySet().removeIf(id -> !onlineIds.contains(id));
        damageTicks.keySet().removeIf(id -> !onlineIds.contains(id));
        lastStage.keySet().removeIf(id -> !onlineIds.contains(id));
    }

    private boolean isStandingOnSnow(Player player) {
        Location loc = player.getLocation();
        Block under = loc.clone().subtract(0, 1, 0).getBlock();
        Material type = under.getType();
        return type == Material.SNOW || type == Material.SNOW_BLOCK || type == Material.POWDER_SNOW;
    }

    private boolean isSnowingAtPlayer(Player player) {
        World world = player.getWorld();
        if (!world.hasStorm()) {
            return false;
        }
        return player.getLocation().getBlock().getTemperature() <= 0.15;
    }

    private boolean isWinterBiome(Biome biome) {
        String name = biome.name();
        return name.contains("SNOW")
                || name.contains("FROZEN")
                || name.contains("ICE")
                || name.contains("COLD")
                || name.contains("GROVE")
                || name.contains("PEAKS")
                || name.contains("TAIGA");
    }

    private boolean hasShelter(Player player) {
        return player.getLocation().getBlock().getLightFromSky() < 14;
    }

    private double getArmorWarmth(Player player) {
        int leatherPieces = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType().name().contains("LEATHER")) {
                leatherPieces++;
            }
        }

        double warmth = leatherPieces * 0.35;
        if (leatherPieces == 4) {
            warmth += 0.30;
        }
        return warmth;
    }

    private double getHeatFromNearbyBlocks(Player player) {
        double heat = 0.0;
        Location loc = player.getLocation();

        for (int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                    int distSquared = x * x + y * y + z * z;
                    if (distSquared == 0 || distSquared > CHECK_RADIUS_SQUARED) {
                        continue;
                    }

                    Block block = loc.clone().add(x, y, z).getBlock();
                    Material type = block.getType();
                    double strength = getBlockHeatStrength(block, type);

                    if (strength > 0) {
                        double distanceFactor = 1.0 - (Math.sqrt(distSquared) / CHECK_RADIUS);
                        heat -= strength * distanceFactor;
                    }
                }
            }
        }

        return heat;
    }

    private double getBlockHeatStrength(Block block, Material type) {
        if (type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE) {
            Campfire campfire = (Campfire) block.getBlockData();
            if (campfire.isLit()) {
                return type == Material.CAMPFIRE ? 2.4 : 1.7;
            }
        }

        if (type == Material.FIRE || type == Material.SOUL_FIRE) {
            return 2.8;
        }

        if (type == Material.LAVA || type == Material.MAGMA_BLOCK) {
            return type == Material.LAVA ? 3.2 : 1.4;
        }

        if (type == Material.FURNACE || type == Material.BLAST_FURNACE || type == Material.SMOKER) {
            if (block.getBlockData() instanceof Lightable lightable && lightable.isLit()) {
                return 2.0;
            }
        }

        if (type == Material.TORCH || type == Material.WALL_TORCH) {
            return 0.9;
        }

        if (type == Material.LANTERN || type == Material.REDSTONE_LAMP) {
            return 1.2;
        }

        return 0.0;
    }

    private double getHeatFromHeldItems(Player player) {
        return getItemHeat(player.getInventory().getItemInMainHand())
                + getItemHeat(player.getInventory().getItemInOffHand());
    }

    private double getItemHeat(ItemStack item) {
        if (item == null) {
            return 0.0;
        }

        Material type = item.getType();
        if (type == Material.TORCH || type == Material.SOUL_TORCH) {
            return -1.2;
        }
        if (type == Material.LANTERN || type == Material.SOUL_LANTERN) {
            return -1.8;
        }
        if (type == Material.BLAZE_ROD) {
            return -0.9;
        }

        return 0.0;
    }

    private void applyEffects(Player player, double previousFreeze, double freeze, boolean wetClothesCold) {
        int percent = (int) Math.round(freeze);
        String hearts = buildFreezeHearts(percent);
        int stage = getStage(percent);

        playStageTransitionFeedback(player, stage);
        showAmbientParticles(player, stage);

        String trend = getTrend(previousFreeze, freeze);
        String cause = wetClothesCold ? " §9(мерзну из-за мокрой одежды)" : "";

        switch (stage) {
            case 3 -> applyExtremeStage(player, hearts, percent, trend, cause);
            case 2 -> {
                player.sendActionBar("§3🥶 Очень холодно §7(" + percent + "%) " + trend + cause + " §f" + hearts);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1, false, false));
                damageTicks.put(player.getUniqueId(), 0);
            }
            case 1 -> {
                player.sendActionBar("§bХолодно §7(" + percent + "%) " + trend + cause + " §f" + hearts);
                damageTicks.put(player.getUniqueId(), 0);
            }
            default -> {
                damageTicks.put(player.getUniqueId(), 0);
                if (percent > 0) {
                    player.sendActionBar("§fТемпература нормализуется §7(" + percent + "%) " + trend + " §f" + hearts);
                } else {
                    player.sendActionBar("");
                }
            }
        }
    }

    private void showAmbientParticles(Player player, int stage) {
        if (stage >= 2) {
            player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1.0, 0), 2, 0.2, 0.25, 0.2, 0.01);
        }
    }

    private void applyExtremeStage(Player player, String hearts, int percent, String trend, String cause) {
        player.sendActionBar("§b❄ Сильное обморожение! §7(" + percent + "%) " + trend + cause + " §f" + hearts);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false));

        int tick = damageTicks.getOrDefault(player.getUniqueId(), 0) + 1;
        if (tick >= EXTREME_DAMAGE_INTERVAL) {
            player.damage(1.0);
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.2, 0), 3, 0.2, 0.2, 0.2, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.35f, 1.9f);
            tick = 0;
        }
        damageTicks.put(player.getUniqueId(), tick);
    }

    private int getStage(int freezePercent) {
        if (freezePercent >= 80) {
            return 3;
        }
        if (freezePercent >= 55) {
            return 2;
        }
        if (freezePercent >= 30) {
            return 1;
        }
        return 0;
    }

    private void playStageTransitionFeedback(Player player, int stage) {
        UUID id = player.getUniqueId();
        int previousStage = lastStage.getOrDefault(id, 0);
        if (stage > previousStage) {
            if (stage == 1) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 0.5f, 1.7f);
            } else if (stage == 2) {
                player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_HIT, 0.6f, 1.2f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_STRAY_AMBIENT, 0.7f, 0.9f);
            }
        }
        lastStage.put(id, stage);
    }

    private String getTrend(double previousFreeze, double currentFreeze) {
        double diff = currentFreeze - previousFreeze;
        if (diff > 0.15) {
            return "§b↑";
        }
        if (diff < -0.15) {
            return "§a↓";
        }
        return "§7→";
    }

    private String buildFreezeHearts(int freezePercent) {
        int frozenHearts = (int) Math.ceil((freezePercent / 100.0) * HEART_BAR_COUNT);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < HEART_BAR_COUNT; i++) {
            if (i < frozenHearts) {
                bar.append("§b❤");
            } else {
                bar.append("§7❤");
            }
        }

        return bar.toString();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
