package com.desquad.server.commands;

import com.desquad.api.DESquadAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class NpcCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
            var npc = DESquadAPI.getNPCManager().create(args[1]);
            sender.sendMessage("§aNPC created: " + npc.name());
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("setjob")) {
            var maybe = DESquadAPI.getNPCManager().findByName(args[1]);
            if (maybe.isPresent() && args.length >= 3) {
                maybe.get().setProfession(args[2]);
                sender.sendMessage("§aJob updated");
            } else {
                sender.sendMessage("§cNPC not found or missing job");
            }
            return true;
        }
        sender.sendMessage("§cUsage: /npc create <name> | /npc setjob <name> <job>");
        return true;
    }
}
