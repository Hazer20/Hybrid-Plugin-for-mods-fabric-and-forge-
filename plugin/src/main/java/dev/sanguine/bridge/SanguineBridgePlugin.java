package dev.sanguine.bridge;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public final class SanguineBridgePlugin extends JavaPlugin {

    private BukkitTask bridgeTask;

    @Override
    public void onEnable() {
        getLogger().info("SanguineBridge enabled. Initializing datapack bridge...");

        // Always reload first so function tags are registered, then initialize objectives.
        dispatch("reload");
        ensureCoreObjectives();
        dispatch("function sanguine:load");

        this.bridgeTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            ensureCoreObjectives();
            dispatch("scoreboard players enable @a sg.trigger");
            dispatch("function sanguine:bridge/tick");
        }, 20L, 20L);

        Bukkit.getPluginManager().registerEvents(new SanguineJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SanguineMenuListener(this), this);
    }

    @Override
    public void onDisable() {
        if (bridgeTask != null) {
            bridgeTask.cancel();
        }
        getLogger().info("SanguineBridge disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sanguine.bridge.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /" + label + " <status|forcebloodmoon|cleanup|reloadpack|smoketest|enabletriggers|menu|ritualaltar|ritualhelp>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "status" -> {
                dispatch("scoreboard objectives list");
                dispatch("data get storage sanguine:bridge");
                sender.sendMessage("§aBridge/objective status printed to server console.");
                return true;
            }
            case "forcebloodmoon" -> {
                dispatch("data merge storage sanguine:bridge {flags:{force_bloodmoon:1}}");
                sender.sendMessage("§cBlood moon flag sent to datapack bridge.");
                return true;
            }
            case "cleanup" -> {
                dispatch("data merge storage sanguine:bridge {flags:{global_cleanup:1}}");
                sender.sendMessage("§aCleanup flag sent to datapack bridge.");
                return true;
            }
            case "reloadpack" -> {
                dispatch("reload");
                ensureCoreObjectives();
                dispatch("function sanguine:load");
                sender.sendMessage("§aDatapacks reloaded and Sanguine reinitialized.");
                return true;
            }
            case "smoketest" -> {
                int dispatched = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("sanguine.bridge.admin")) {
                        continue;
                    }
                    dispatch("execute as " + player.getName() + " run function sanguine:admin/tests/grant_admin");
                    dispatch("execute as " + player.getName() + " run function sanguine:admin/tests/test_all_features");
                    dispatched++;
                }
                sender.sendMessage("§aSanguine smoke test dispatched for admins online: " + dispatched);
                return true;
            }
            case "enabletriggers" -> {
                ensureCoreObjectives();
                dispatch("scoreboard players enable @a sg.trigger");
                sender.sendMessage("§aEnabled sg.trigger for all players.");
                return true;
            }
            case "menu" -> {
                if (sender instanceof Player player) {
                    openControlMenu(player);
                } else {
                    sender.sendMessage("§eMenu can be opened only by a player.");
                }
                return true;
            }
            case "ritualaltar" -> {
                if (sender instanceof Player player) {
                    dispatch("execute as " + player.getName() + " at " + player.getName() + " run function sanguine:blocks/create_ritual_altar");
                    sender.sendMessage("§5Ritual altar created at your location.");
                } else {
                    sender.sendMessage("§eUse this command in-game as player.");
                }
                return true;
            }
            case "ritualhelp" -> {
                sender.sendMessage("§dSanguine Ritual quick list:");
                sender.sendMessage("§7/function sanguine:ritual/embrace");
                sender.sendMessage("§7/function sanguine:ritual/cure");
                sender.sendMessage("§7/function sanguine:ritual/feed");
                sender.sendMessage("§7/function sanguine:ritual/create_ward");
                sender.sendMessage("§7/function sanguine:ritual/remove_ward");
                sender.sendMessage("§7/function sanguine:ritual/infect_target");
                sender.sendMessage("§7/function sanguine:ritual/channel_blood");
                sender.sendMessage("§7/trigger sg.trigger set 1..8");
                return true;
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand.");
                return true;
            }
        }
    }

    void enableTriggerFor(Player player) {
        ensureCoreObjectives();
        dispatch("scoreboard players enable " + player.getName() + " sg.trigger");
    }

    void handleMenuClick(Player player, Material material) {
        switch (material) {
            case REDSTONE_TORCH -> dispatch("execute as " + player.getName() + " run function sanguine:admin/tests/force_bloodmoon");
            case LODESTONE -> dispatch("execute as " + player.getName() + " at " + player.getName() + " run function sanguine:blocks/create_ritual_altar");
            case IRON_SWORD -> dispatch("execute as " + player.getName() + " run function sanguine:items/give_hunter_kit");
            case NETHERITE_SWORD -> dispatch("execute as " + player.getName() + " run function sanguine:items/give_vampire_kit");
            case TOTEM_OF_UNDYING -> dispatch("execute as " + player.getName() + " run function sanguine:admin/tests/test_all_features");
            default -> {
                return;
            }
        }
        player.sendMessage("§aSanguine action executed from menu.");
        player.closeInventory();
    }

    private void openControlMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Sanguine Bridge Control");
        inv.setItem(10, menuItem(Material.REDSTONE_TORCH, "§cForce Blood Moon"));
        inv.setItem(12, menuItem(Material.LODESTONE, "§5Create Ritual Altar"));
        inv.setItem(14, menuItem(Material.IRON_SWORD, "§6Give Hunter Kit"));
        inv.setItem(16, menuItem(Material.NETHERITE_SWORD, "§4Give Vampire Kit"));
        inv.setItem(22, menuItem(Material.TOTEM_OF_UNDYING, "§aRun Full Smoke Test"));
        player.openInventory(inv);
    }

    private ItemStack menuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to execute Sanguine action");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void ensureCoreObjectives() {
        dispatch("scoreboard objectives add sg.trigger trigger");
        dispatch("scoreboard objectives add sg.role dummy");
        dispatch("scoreboard objectives add sg.blood dummy");
        dispatch("scoreboard objectives add sg.thirst dummy");
    }

    private void dispatch(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
