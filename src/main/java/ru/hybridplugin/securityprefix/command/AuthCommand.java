package ru.hybridplugin.securityprefix.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import ru.hybridplugin.securityprefix.service.AuthService;

public class AuthCommand implements CommandExecutor {

    private final AuthService authService;

    public AuthCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cИспользование: /" + label + " <пароль> [повтор]");
            return true;
        }

        if (command.getName().equalsIgnoreCase("register")) {
            if (authService.isRegistered(player.getUniqueId())) {
                player.sendMessage("§eВы уже зарегистрированы. Используйте /login <пароль>.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("§cПовторите пароль: /register <пароль> <пароль>");
                return true;
            }

            if (!args[0].equals(args[1])) {
                player.sendMessage("§cПароли не совпадают.");
                return true;
            }

            if (args[0].length() < 4) {
                player.sendMessage("§cПароль должен быть минимум 4 символа.");
                return true;
            }

            authService.register(player, args[0]);
            unlockPlayer(player);
            playAuthSuccessEffects(player, "§aРегистрация успешна. Вы авторизованы.");
            return true;
        }

        if (!authService.isRegistered(player.getUniqueId())) {
            player.sendMessage("§cВы еще не зарегистрированы. Используйте /register <пароль> <пароль>.");
            return true;
        }

        boolean loggedIn = authService.login(player, args[0]);
        if (loggedIn) {
            unlockPlayer(player);
            playAuthSuccessEffects(player, "§aВход выполнен.");
        } else {
            player.sendMessage("§cНеверный пароль.");
        }
        return true;
    }

    private void unlockPlayer(Player player) {
        player.setInvulnerable(false);
    }

    private void playAuthSuccessEffects(Player player, String message) {
        player.sendMessage(message);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1.0, 0),
                40,
                0.6,
                0.8,
                0.6,
                0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.1f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 8, 0, false, true, true));
    }
}
