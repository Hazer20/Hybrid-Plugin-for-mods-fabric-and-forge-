package ru.hybridplugin.securityprefix;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.whitelistService = new WhitelistService(this);
        this.authService = new AuthService(this);
        this.economy = hookEconomy();
        this.prefixService = new PrefixService(this, economy);

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

    private Economy hookEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault не найден. Платные префиксы будут недоступны.");
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("Провайдер экономики не найден. Платные префиксы будут недоступны.");
            return null;
        }

        return rsp.getProvider();
    }
}
