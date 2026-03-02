package com.hazer.bookimage;

import com.hazer.bookimage.command.BookImageCommand;
import com.hazer.bookimage.listener.BookListener;
import com.hazer.bookimage.service.BookImageService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class.
 */
public class BookImagePlugin extends JavaPlugin {

    private BookImageService bookImageService;

    @Override
    public void onEnable() {
        // No default config is required now; calling saveDefaultConfig without bundled config.yml causes startup failure.
        this.bookImageService = new BookImageService(this);

        BookImageCommand commandExecutor = new BookImageCommand(bookImageService);
        PluginCommand pluginCommand = getCommand("bookimage");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(commandExecutor);
        } else {
            getLogger().severe("Command /bookimage is missing in plugin.yml");
        }

        getServer().getPluginManager().registerEvents(new BookListener(bookImageService), this);
        getLogger().info("BookImage enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BookImage disabled.");
    }
}
