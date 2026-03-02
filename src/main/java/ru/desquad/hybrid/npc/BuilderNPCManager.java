package ru.desquad.hybrid.npc;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.gui.GUIFactory;
import ru.desquad.hybrid.schematic.ExternalSchematicRepository;
import ru.desquad.hybrid.schematic.SchematicParser;
import ru.desquad.hybrid.storage.DataStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BuilderNPCManager {

    private final DESHybridPlugin plugin;
    private final EconomyManager economy;
    private final DataStorage storage;
    private final ExternalSchematicRepository schematicRepository;
    private final SchematicParser schematicParser = new SchematicParser();

    private final Random random = new Random();
    private final Map<UUID, Long> orderedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, String> selectedSchematic = new ConcurrentHashMap<>();
    private Villager npc;
    private String npcName;

    public BuilderNPCManager(DESHybridPlugin plugin, EconomyManager economy, DataStorage storage, ExternalSchematicRepository schematicRepository) {
        this.plugin = plugin;
        this.economy = economy;
        this.storage = storage;
        this.schematicRepository = schematicRepository;
    }

    public boolean hasCraftedNpcToken(UUID uuid) { return storage.hasCraftedNpcToken(uuid); }
    public void setCraftedNpcToken(UUID uuid, boolean value) { storage.setCraftedNpcToken(uuid, value); }

    public void spawnNpcAt(Location location) {
        if (!plugin.getConfig().getBoolean("npc-builder.enabled", true)) return;
        if (npc != null && !npc.isDead()) npc.remove();
        npcName = randomName();
        npc = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        npc.customName(Component.text("§6" + npcName + " §7[Строитель]"));
        npc.setCustomNameVisible(true);
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setRemoveWhenFarAway(false);
        npc.setPersistent(true);
        npc.setCollidable(false);
        npc.setProfession(Villager.Profession.TOOLSMITH);
    }

    public void spawnOrRespawnNPC() {
        if (!plugin.getConfig().getBoolean("npc-builder.enabled", true)) return;
        World world = Bukkit.getWorld(plugin.getConfig().getString("npc-builder.spawn-world", "world"));
        if (world == null) return;
        spawnNpcAt(new Location(world,
                plugin.getConfig().getDouble("npc-builder.spawn-x"),
                plugin.getConfig().getDouble("npc-builder.spawn-y"),
                plugin.getConfig().getDouble("npc-builder.spawn-z")));
    }

    public void cleanup() { if (npc != null && !npc.isDead()) npc.remove(); }

    public boolean isNearNPC(Player player) {
        if (npc == null || npc.isDead()) return false;
        double radius = plugin.getConfig().getDouble("npc-builder.interaction-radius", 6);
        return npc.getWorld().equals(player.getWorld()) && npc.getLocation().distanceSquared(player.getLocation()) <= radius * radius;
    }

    public void requestOrder(Player player) {
        if (!isNearNPC(player)) { player.sendMessage(colorMsg("messages.npc-order-required")); playConfigSound(player, "npc-builder.sounds.error"); return; }
        orderedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(colorMsg("messages.npc-order-required") + " §7(Напиши фразу в чат)");
    }

    public void acceptOrder(Player player) {
        orderedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(colorMsg("messages.npc-order-accepted"));
        player.openInventory(GUIFactory.createMainNPCGUI(plugin, player, this));
    }

    public List<String> getAvailableSchematics() {
        Set<String> keys = new LinkedHashSet<>();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("npc-builder.schematics");
        if (sec != null) keys.addAll(sec.getKeys(false));
        keys.addAll(schematicRepository.listKeys());
        return new ArrayList<>(keys);
    }

    public void selectSchematic(Player player, String key) { selectedSchematic.put(player.getUniqueId(), key); }
    public String getSelectedSchematic(Player player) { return selectedSchematic.get(player.getUniqueId()); }

    public BuildQuote quote(Player player) {
        String key = getSelectedSchematic(player);
        if (key == null) return null;

        boolean external = schematicRepository.isExternalKey(key);
        ConfigurationSection sec = external ? null : plugin.getConfig().getConfigurationSection("npc-builder.schematics." + key);
        if (!external && sec == null) return null;

        int sx;
        int sy;
        int sz;
        List<Material> palette;
        List<PlannedBlock> plan;
        String displayName;
        String formatType;
        double complexity;
        Map<String, Integer> resources = new LinkedHashMap<>();

        if (external) {
            var p = schematicRepository.resolveByKey(key).orElse(null);
            if (p == null) return null;
            try {
                SchematicParser.ParsedSchematic parsed = schematicParser.parse(p);
                sx = parsed.width();
                sy = parsed.height();
                sz = parsed.length();
                plan = parsed.plan();
                palette = new ArrayList<>();
                for (PlannedBlock pb : plan) if (!palette.contains(pb.material())) palette.add(pb.material());
                displayName = key.substring(4);
                String file = p.getFileName().toString().toLowerCase(Locale.ROOT);
                formatType = file.endsWith(".litematic") ? "litematic" : (file.endsWith(".schem") ? "schem" : "schematic");
                complexity = 1.0;
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка парсинга схемы " + key + ": " + e.getMessage());
                return null;
            }
        } else {
            sx = sec.getInt("size-x", plugin.getConfig().getInt("npc-builder.default-zone.size-x", 16));
            sy = sec.getInt("size-y", plugin.getConfig().getInt("npc-builder.default-zone.size-y", 16));
            sz = sec.getInt("size-z", plugin.getConfig().getInt("npc-builder.default-zone.size-z", 16));
            palette = parsePalette(sec, false);
            plan = parseOrGeneratePlan(sec, sx, sy, sz, palette);
            displayName = sec.getString("display-name", key);
            formatType = sec.getString("type", "schematic");
            complexity = sec.getDouble("complexity", 1.0);
            if (sec.getConfigurationSection("resources") != null) {
                ConfigurationSection rSec = sec.getConfigurationSection("resources");
                for (String mat : rSec.getKeys(false)) resources.put(mat, rSec.getInt(mat));
            }
        }

        int blocks = plan.size();
        double base = plugin.getConfig().getDouble("npc-builder.growth.base-descoin", 25);
        double blockFactor = plugin.getConfig().getDouble("npc-builder.growth.block-cost-factor", 0.1);
        double complexityFactor = plugin.getConfig().getDouble("npc-builder.growth.complexity-factor", 0.8);
        int baseExp = plugin.getConfig().getInt("npc-builder.growth.base-exp", 5);
        double expFactor = plugin.getConfig().getDouble("npc-builder.growth.exp-block-factor", 0.02);
        double descoin = base + blocks * blockFactor + complexity * complexityFactor * 100;
        int exp = (int) Math.ceil(baseExp + blocks * expFactor + complexity * 5);

        Location npcLoc = npc != null ? npc.getLocation() : player.getLocation();

        return new BuildQuote(key, displayName, formatType, descoin, exp, resources,
                npcLoc.getBlockX() + 2, npcLoc.getBlockY(), npcLoc.getBlockZ() + 2, sx, sy, sz, blocks, plan);
    }

    public void startBuild(Player player, BuildQuote quote) {
        if (!economy.withdraw(player.getUniqueId(), quote.descoin())) { player.sendMessage(colorMsg("messages.insufficient-descoin")); playConfigSound(player, "npc-builder.sounds.error"); return; }
        if (player.getLevel() < quote.expLevels()) { economy.add(player.getUniqueId(), quote.descoin()); player.sendMessage(colorMsg("messages.insufficient-exp")); playConfigSound(player, "npc-builder.sounds.error"); return; }

        player.setLevel(Math.max(0, player.getLevel() - quote.expLevels()));
        playConfigSound(player, "npc-builder.sounds.start");

        BossBar bar = BossBar.bossBar(Component.text("§6Строительство: 0%"), 0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        player.showBossBar(bar);

        List<Villager> helpers = spawnHelpers();
        int targetBlocks = Math.max(1, quote.plan().size());
        int placePerStep = Math.max(1, plugin.getConfig().getInt("npc-builder.workers.place-blocks-per-step", 3));

        new BukkitRunnable() {
            int cursor = 0;
            @Override public void run() {
                int stepPlaced = 0;
                while (stepPlaced < placePerStep && cursor < quote.plan().size()) {
                    placePlannedBlock(quote, quote.plan().get(cursor));
                    cursor++; stepPlaced++;
                }
                moveWorkers(helpers, quote);
                float progress = Math.min(1f, cursor / (float) targetBlocks);
                bar.progress(progress);
                bar.name(Component.text("§6Строительство: " + (int) (progress * 100) + "% | " + quote.displayName()));
                if (cursor >= quote.plan().size()) {
                    cancel(); helpers.forEach(Villager::remove); player.hideBossBar(bar);
                    player.sendMessage(colorMsg("messages.build-confirmed") + " §7(" + quote.displayName() + ")");
                    playConfigSound(player, "npc-builder.sounds.complete");
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private List<Villager> spawnHelpers() {
        List<Villager> helpers = new ArrayList<>();
        if (!plugin.getConfig().getBoolean("npc-builder.workers.dual-builders", true) || npc == null || npc.isDead()) return helpers;
        Location secondLoc = npc.getLocation().clone().add(1.5, 0, 0);
        Villager second = (Villager) npc.getWorld().spawnEntity(secondLoc, EntityType.VILLAGER);
        second.customName(Component.text("§6" + ("Равшан".equals(npcName) ? "Джамшут" : "Равшан") + " §7[Помощник]"));
        second.setCustomNameVisible(true); second.setInvulnerable(true); second.setRemoveWhenFarAway(false); second.setPersistent(true); second.setAI(false); second.setCollidable(false);
        helpers.add(second);
        return helpers;
    }

    private void moveWorkers(List<Villager> helpers, BuildQuote quote) {
        int radius = plugin.getConfig().getInt("npc-builder.workers.step-radius", 4);
        for (Villager worker : helpers) {
            if (worker == null || worker.isDead()) continue;
            Location base = new Location(worker.getWorld(), quote.x(), quote.y(), quote.z());
            Location target = base.clone().add(random.nextInt(radius * 2 + 1) - radius, 0, random.nextInt(radius * 2 + 1) - radius);
            target.setY(target.getWorld().getHighestBlockYAt(target) + 1);
            worker.teleport(target);
        }
    }

    private void placePlannedBlock(BuildQuote quote, PlannedBlock planned) {
        World world = npc != null ? npc.getWorld() : Bukkit.getWorld(plugin.getConfig().getString("npc-builder.spawn-world", "world"));
        if (world == null) return;
        Block block = world.getBlockAt(quote.x() + planned.dx(), quote.y() + planned.dy(), quote.z() + planned.dz());
        block.setType(planned.material(), false);
    }

    private List<Material> parsePalette(ConfigurationSection sec, boolean external) {
        List<Material> palette = new ArrayList<>();
        if (!external && sec != null) {
            for (String s : sec.getStringList("palette")) {
                try { palette.add(Material.valueOf(s)); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (palette.isEmpty()) palette.add(Material.STONE);
        return palette;
    }

    private List<PlannedBlock> parseOrGeneratePlan(ConfigurationSection sec, int sx, int sy, int sz, List<Material> palette) {
        List<PlannedBlock> plan = new ArrayList<>();
        List<String> blockLines = sec == null ? List.of() : sec.getStringList("blocks");
        for (String line : blockLines) {
            String[] p = line.split(",");
            if (p.length != 4) continue;
            try { plan.add(new PlannedBlock(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()), Material.valueOf(p[3].trim().toUpperCase()))); }
            catch (Exception ignored) {}
        }
        if (!plan.isEmpty()) return plan;

        int pi = 0;
        for (int y = 0; y < sy; y++) for (int x = 0; x < sx; x++) for (int z = 0; z < sz; z++) {
            boolean floor = y == 0, wall = x == 0 || x == sx - 1 || z == 0 || z == sz - 1, roof = y == sy - 1;
            if (floor || wall || roof) plan.add(new PlannedBlock(x, y, z, palette.get((pi++) % palette.size())));
        }
        return plan;
    }

    public void npcComment(Player player) {
        List<String> comments = plugin.getConfig().getStringList("npc-builder.comments");
        if (comments.isEmpty()) return;
        String c = comments.get(random.nextInt(comments.size())).replace("{name}", npcName != null ? npcName : "Строитель");
        player.sendMessage("§d[NPC] §f" + c);
    }

    public Villager getNpc() { return npc; }
    private String randomName() { List<String> names = plugin.getConfig().getStringList("npc-builder.names"); return names.isEmpty() ? "Равшан" : names.get(random.nextInt(names.size())); }
    private String colorMsg(String path) { return plugin.getConfig().getString(path, "").replace('&', '§'); }
    private void playConfigSound(Player player, String path) { try { player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString(path, "ENTITY_EXPERIENCE_ORB_PICKUP")), 1f, 1f); } catch (IllegalArgumentException ignored) {} }

    public record PlannedBlock(int dx, int dy, int dz, Material material) {}
    public record BuildQuote(String key, String displayName, String formatType, double descoin, int expLevels,
                             Map<String, Integer> resources, int x, int y, int z, int sizeX, int sizeY, int sizeZ, int blockCount,
                             List<PlannedBlock> plan) {}
}
