package me.yourname.freezeplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class FreezePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getScheduler().runTaskTimer(this, new FreezeTask(), 0L, 20L);
        getLogger().info("FreezePlugin включен: улучшенный баланс холода, сердечки и новые эффекты активированы.");
    }
}
