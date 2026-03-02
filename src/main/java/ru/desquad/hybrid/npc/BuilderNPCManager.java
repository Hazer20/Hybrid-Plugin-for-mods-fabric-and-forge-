package ru.desquad.hybrid.npc;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.gui.GUIFactory;
import ru.desquad.hybrid.storage.DataStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BuilderNPCManager {

    private final DESHybridPlugin plugin;
    private final EconomyManager economy;
    private final DataStorage storage;

    private final Random random = new Random();
    private final Map<UUID, Long> orderedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, String> selectedSchematic = new ConcurrentHashMap<>();
    private Villager npc;
    private String npcName;

    public BuilderNPCManager(DESHybridPlugin plugin, EconomyManager economy, DataStorage storage) {
        this.plugin = plugin;
        this.economy = economy;
        this.storage = storage;
    }

    public boolean hasCraftedNpcToken(UUID uuid) {
        return storage.hasCraftedNpcToken(uuid);
    }

    public void setCraftedNpcToken(UUID uuid, boolean value) {
        storage.setCraftedNpcToken(uuid, value);
    }

    public void spawnNpcAt(Location location) {
        if (!plugin.getConfig().getBoolean("npc-builder.enabled", true)) return;
        if (npc != null && !npc.isDead()) npc.remove();
        npcName = randomName();
        npc = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        npc.customName(Component.text("§6" + npcName + " §7[Строитель]"));
        npc.setCustomNameVisible(true);
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setProfession(Villager.Profession.TOOLSMITH);
    }

    public void spawnOrRespawnNPC() {
        if (!plugin.getConfig().getBoolean("npc-builder.enabled", true)) return;
        World world = Bukkit.getWorld(plugin.getConfig().getString("npc-builder.spawn-world", "world"));
        if (world == null) {
            plugin.getLogger().warning("Мир для NPC не найден.");
            return;
        }
        spawnNpcAt(new Location(world,
                plugin.getConfig().getDouble("npc-builder.spawn-x"),
                plugin.getConfig().getDouble("npc-builder.spawn-y"),
                plugin.getConfig().getDouble("npc-builder.spawn-z")));
    }

    public void cleanup() {
        if (npc != null && !npc.isDead()) npc.remove();
    }

    public boolean isNearNPC(Player player) {
        if (npc == null || npc.isDead()) return false;
        double radius = plugin.getConfig().getDouble("npc-builder.interaction-radius", 6);
        if (!npc.getWorld().equals(player.getWorld())) return false;
        return npc.getLocation().distanceSquared(player.getLocation()) <= radius * radius;
    }

    public void requestOrder(Player player) {
        if (!isNearNPC(player)) {
            player.sendMessage(colorMsg("messages.npc-order-required"));
            playConfigSound(player, "npc-builder.sounds.error");
            return;
        }
        orderedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(colorMsg("messages.npc-order-required") + " §7(Напиши фразу в чат)");
    }

    public void acceptOrder(Player player) {
        orderedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(colorMsg("messages.npc-order-accepted"));
        player.openInventory(GUIFactory.createMainNPCGUI(plugin, player, this));
    }

    public List<String> getAvailableSchematics() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("npc-builder.schematics");
        if (sec == null) return List.of();
        return new ArrayList<>(sec.getKeys(false));
    }

    public void selectSchematic(Player player, String key) {
        selectedSchematic.put(player.getUniqueId(), key);
    }

    public String getSelectedSchematic(Player player) {
        return selectedSchematic.get(player.getUniqueId());
    }

    public BuildQuote quote(Player player) {
        String key = getSelectedSchematic(player);
        if (key == null) return null;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("npc-builder.schematics." + key);
        if (sec == null) return null;

        int blocks = sec.getInt("block-count", 100);
        double complexity = sec.getDouble("complexity", 1.0);
        double base = plugin.getConfig().getDouble("npc-builder.growth.base-descoin", 25);
        double blockFactor = plugin.getConfig().getDouble("npc-builder.growth.block-cost-factor", 0.1);
        double complexityFactor = plugin.getConfig().getDouble("npc-builder.growth.complexity-factor", 0.8);
        int baseExp = plugin.getConfig().getInt("npc-builder.growth.base-exp", 5);
        double expFactor = plugin.getConfig().getDouble("npc-builder.growth.exp-block-factor", 0.02);

        double descoin = base + blocks * blockFactor + complexity * complexityFactor * 100;
        int exp = (int) Math.ceil(baseExp + blocks * expFactor + complexity * 5);

        Location npcLoc = npc != null ? npc.getLocation() : player.getLocation();
        int sx = sec.getInt("size-x", plugin.getConfig().getInt("npc-builder.default-zone.size-x", 16));
        int sy = sec.getInt("size-y", plugin.getConfig().getInt("npc-builder.default-zone.size-y", 16));
        int sz = sec.getInt("size-z", plugin.getConfig().getInt("npc-builder.default-zone.size-z", 16));

        Map<String, Integer> resources = new LinkedHashMap<>();
        ConfigurationSection rSec = sec.getConfigurationSection("resources");
        if (rSec != null) {
            for (String mat : rSec.getKeys(false)) resources.put(mat, rSec.getInt(mat));
        }

        List<Material> palette = new ArrayList<>();
        for (String s : sec.getStringList("palette")) {
            try { palette.add(Material.valueOf(s)); } catch (IllegalArgumentException ignored) {}
        }
        if (palette.isEmpty()) palette.add(Material.STONE);

        return new BuildQuote(key, sec.getString("display-name", key), sec.getString("type", "schematic"), descoin, exp,
                resources, palette, npcLoc.getBlockX() + 2, npcLoc.getBlockY(), npcLoc.getBlockZ() + 2, sx, sy, sz, blocks);
    }

    public void startBuild(Player player, BuildQuote quote) {
        if (!economy.withdraw(player.getUniqueId(), quote.descoin())) {
            player.sendMessage(colorMsg("messages.insufficient-descoin"));
            playConfigSound(player, "npc-builder.sounds.error");
            return;
        }
        if (player.getLevel() < quote.expLevels()) {
            economy.add(player.getUniqueId(), quote.descoin());
            player.sendMessage(colorMsg("messages.insufficient-exp"));
            playConfigSound(player, "npc-builder.sounds.error");
            return;
        }

        player.setLevel(Math.max(0, player.getLevel() - quote.expLevels()));
        playConfigSound(player, "npc-builder.sounds.start");

        BossBar bar = BossBar.bossBar(Component.text("§6Строительство: 0%"), 0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        player.showBossBar(bar);

        List<Villager> workers = spawnWorkers(quote);
        int targetBlocks = quote.blockCount();
        int placePerStep = Math.max(1, plugin.getConfig().getInt("npc-builder.workers.place-blocks-per-step", 3));

        new BukkitRunnable() {
            int placed = 0;

            @Override
            public void run() {
                int stepPlaced = 0;
                while (stepPlaced < placePerStep && placed < targetBlocks) {
                    placeRandomBlock(quote);
                    stepPlaced++;
                    placed++;
                }

                moveWorkers(workers, quote);
                float progress = Math.min(1f, placed / (float) targetBlocks);
                bar.progress(progress);
                bar.name(Component.text("§6Строительство: " + (int) (progress * 100) + "% | " + quote.displayName()));

                if (placed % Math.max(20, targetBlocks / 8) == 0) npcComment(player);

                if (progress >= 1f) {
                    cancel();
                    workers.forEach(Entity::remove);
                    player.hideBossBar(bar);
                    player.sendMessage(colorMsg("messages.build-confirmed") + " §7(" + quote.displayName() + ")");
                    playConfigSound(player, "npc-builder.sounds.complete");
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private List<Villager> spawnWorkers(BuildQuote quote) {
        List<Villager> workers = new ArrayList<>();
        if (npc != null && !npc.isDead()) workers.add(npc);
        boolean dual = plugin.getConfig().getBoolean("npc-builder.workers.dual-builders", true);
        if (dual && npc != null && !npc.isDead()) {
            Location secondLoc = npc.getLocation().clone().add(1.5, 0, 0);
            Villager second = (Villager) npc.getWorld().spawnEntity(secondLoc, EntityType.VILLAGER);
            second.customName(Component.text("§6" + ("Равшан".equals(npcName) ? "Джамшут" : "Равшан") + " §7[Помощник]"));
            second.setCustomNameVisible(true);
            second.setInvulnerable(true);
            second.setAI(false);
            workers.add(second);
        }
        return workers;
    }

    private void moveWorkers(List<Villager> workers, BuildQuote quote) {
        int radius = plugin.getConfig().getInt("npc-builder.workers.step-radius", 4);
        for (Villager worker : workers) {
            if (worker == null || worker.isDead()) continue;
            Location base = new Location(worker.getWorld(), quote.x(), quote.y(), quote.z());
            double dx = random.nextInt(radius * 2 + 1) - radius;
            double dz = random.nextInt(radius * 2 + 1) - radius;
            Location target = base.clone().add(dx, 0, dz);
            target.setY(target.getWorld().getHighestBlockYAt(target) + 1);
            worker.teleport(target);
            worker.getWorld().spawnParticle(Particle.CLOUD, target, 4, 0.2, 0.2, 0.2, 0.01);
        }
    }

    private void placeRandomBlock(BuildQuote quote) {
        World world = npc != null ? npc.getWorld() : Bukkit.getWorld(plugin.getConfig().getString("npc-builder.spawn-world", "world"));
        if (world == null) return;

        int x = quote.x() + random.nextInt(Math.max(1, quote.sizeX()));
        int y = quote.y() + random.nextInt(Math.max(1, quote.sizeY()));
        int z = quote.z() + random.nextInt(Math.max(1, quote.sizeZ()));
        Material material = quote.palette().get(random.nextInt(quote.palette().size()));
        Block block = world.getBlockAt(x, y, z);

        if (block.getType().isAir() || block.isPassable()) {
            block.setType(material, false);
            world.playSound(block.getLocation(), Sound.BLOCK_STONE_PLACE, 0.5f, 1.2f);
        }
    }

    public void npcComment(Player player) {
        List<String> comments = plugin.getConfig().getStringList("npc-builder.comments");
        if (comments.isEmpty()) return;
        String c = comments.get(random.nextInt(comments.size())).replace("{name}", npcName != null ? npcName : "Строитель");
        player.sendMessage("§d[NPC] §f" + c);
    }

    public Villager getNpc() {
        return npc;
    }

    private String randomName() {
        List<String> names = plugin.getConfig().getStringList("npc-builder.names");
        if (names.isEmpty()) return "Равшан";
        return names.get(random.nextInt(names.size()));
    }

    private String colorMsg(String path) {
        return plugin.getConfig().getString(path, "").replace('&', '§');
    }

    private void playConfigSound(Player player, String path) {
        try {
            Sound sound = Sound.valueOf(plugin.getConfig().getString(path, "ENTITY_EXPERIENCE_ORB_PICKUP"));
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public record BuildQuote(String key, String displayName, String formatType, double descoin, int expLevels,
                             Map<String, Integer> resources, List<Material> palette,
                             int x, int y, int z, int sizeX, int sizeY, int sizeZ, int blockCount) {}
}
