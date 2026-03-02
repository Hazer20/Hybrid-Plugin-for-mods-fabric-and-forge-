package com.hazer2_0;

import com.hazer2_0.audio.AudioPlaybackManager;
import com.hazer2_0.database.SQLiteManager;
import com.hazer2_0.disc.DiscCommand;
import com.hazer2_0.disc.DiscManager;
import com.hazer2_0.disc.JukeboxListener;
import com.hazer2_0.radio.RadioManager;
import com.hazer2_0.radio.VoiceListener;
import com.hazer2_0.utils.AsyncExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HazerPlugin extends JavaPlugin {

    private AsyncExecutor asyncExecutor;
    private SQLiteManager sqLiteManager;
    private DiscManager discManager;
    private AudioPlaybackManager audioPlaybackManager;
    private RadioManager radioManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.asyncExecutor = new AsyncExecutor(this);
        this.sqLiteManager = new SQLiteManager(this);
        this.sqLiteManager.initialize();
        this.discManager = new DiscManager(this, sqLiteManager, asyncExecutor);
        this.audioPlaybackManager = new AudioPlaybackManager(this);
        this.radioManager = new RadioManager(this);

        getCommand("disc").setExecutor(new DiscCommand(this, discManager));
        Bukkit.getPluginManager().registerEvents(new JukeboxListener(this, discManager, audioPlaybackManager), this);
        Bukkit.getPluginManager().registerEvents(new VoiceListener(this, radioManager, audioPlaybackManager), this);

        getLogger().info("Hazer_2_0 enabled.");
    }

    @Override
    public void onDisable() {
        if (audioPlaybackManager != null) {
            audioPlaybackManager.stopAll();
        }
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
        if (sqLiteManager != null) {
            sqLiteManager.close();
        }
        getLogger().info("Hazer_2_0 disabled.");
    }
}
