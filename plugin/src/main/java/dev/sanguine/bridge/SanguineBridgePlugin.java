package dev.sanguine.bridge;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class SanguineBridgePlugin extends JavaPlugin {

    private BukkitTask bridgeTask;

    @Override
    public void onEnable() {
        getLogger().info("SanguineBridge enabled. Wiring datapack bridge...");

        dispatch("function sanguine:load");
        dispatch("reload");

        this.bridgeTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            dispatch("scoreboard players enable @a sg.trigger");
            dispatch("function sanguine:bridge/tick");
        }, 20L, 20L);

        Bukkit.getPluginManager().registerEvents(new SanguineJoinListener(this), this);
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
            sender.sendMessage("§eUsage: /" + label + " <status|forcebloodmoon|cleanup|reloadpack|smoketest|enabletriggers>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "status" -> {
                dispatch("data get storage sanguine:bridge world");
                dispatch("data get storage sanguine:bridge last_player");
                sender.sendMessage("§aPrinted bridge status to console output stream.");
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
                dispatch("scoreboard players enable @a sg.trigger");
                sender.sendMessage("§aEnabled sg.trigger for all players.");
                return true;
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand.");
                return true;
            }
        }
    }

    void enableTriggerFor(Player player) {
        dispatch("scoreboard players enable " + player.getName() + " sg.trigger");
    }

    private void dispatch(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
