package ru.hybridplugin.securityprefix;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hybridplugin.securityprefix.command.AuthCommand;
import ru.hybridplugin.securityprefix.command.CustomWhitelistCommand;
import ru.hybridplugin.securityprefix.command.PrefixCommand;
import ru.hybridplugin.securityprefix.listener.AuthProtectionListener;
import ru.hybridplugin.securityprefix.listener.CustomWhitelistListener;
import ru.hybridplugin.securityprefix.listener.PrefixMenuListener;
import ru.hybridplugin.securityprefix.service.AuthService;
import ru.hybridplugin.securityprefix.service.PrefixService;
import ru.hybridplugin.securityprefix.service.WhitelistService;

public class HybridSecurityPrefixPlugin extends JavaPlugin {

    private WhitelistService whitelistService;
    private AuthService authService;
    private PrefixService prefixService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.whitelistService = new WhitelistService(this);
        this.authService = new AuthService(this);
        this.prefixService = new PrefixService(this);

        Bukkit.setWhitelist(false);

        getServer().getPluginManager().registerEvents(new CustomWhitelistListener(whitelistService), this);
        getServer().getPluginManager().registerEvents(new AuthProtectionListener(authService), this);
        getServer().getPluginManager().registerEvents(new PrefixMenuListener(prefixService), this);

        CustomWhitelistCommand whitelistCommand = new CustomWhitelistCommand(whitelistService);
        getCommand("cwhitelist").setExecutor(whitelistCommand);
        getCommand("cwhitelist").setTabCompleter(whitelistCommand);

        AuthCommand authCommand = new AuthCommand(authService);
        getCommand("register").setExecutor(authCommand);
        getCommand("login").setExecutor(authCommand);

        PrefixCommand prefixCommand = new PrefixCommand(prefixService);
        getCommand("prefix").setExecutor(prefixCommand);

        getLogger().info("HybridSecurityPrefix включен. Стандартный whitelist Paper отключен.");
    }
}
