package com.hazer.lightblock;

import com.hazer.lightblock.command.LightBlockCommand;
import com.hazer.lightblock.gui.LightBlockGUI;
import com.hazer.lightblock.item.LightBlockItemManager;
import com.hazer.lightblock.listener.LightBlockListener;
import com.hazer.lightblock.recipe.RecipeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LightBlockPlugin extends JavaPlugin {

    private LightBlockItemManager itemManager;
    private RecipeManager recipeManager;
    private LightBlockGUI lightBlockGUI;

    @Override
    public void onEnable() {
        this.itemManager = new LightBlockItemManager(this);
        this.recipeManager = new RecipeManager(this, itemManager);
        this.lightBlockGUI = new LightBlockGUI(this, itemManager, recipeManager);

        getServer().getPluginManager().registerEvents(new LightBlockListener(itemManager), this);
        getServer().getPluginManager().registerEvents(lightBlockGUI, this);

        LightBlockCommand command = new LightBlockCommand(lightBlockGUI);
        if (getCommand("lightblock") != null) {
            getCommand("lightblock").setExecutor(command);
            getCommand("lightblock").setTabCompleter(command);
        }

        getLogger().info("LightBlockPlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("LightBlockPlugin disabled.");
    }

    public LightBlockItemManager getItemManager() {
        return itemManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
