package ru.desquad.hybrid.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.market.MarketListing;
import ru.desquad.hybrid.market.MarketManager;
import ru.desquad.hybrid.npc.BuilderNPCManager;
import ru.desquad.hybrid.quest.MiniQuest;
import ru.desquad.hybrid.quest.QuestManager;

import java.util.List;

public class GUIListener implements Listener {

    private final DESHybridPlugin plugin;
    private final QuestManager questManager;
    private final MarketManager marketManager;
    private final BuilderNPCManager npcManager;

    public GUIListener(DESHybridPlugin plugin, QuestManager questManager, MarketManager marketManager, BuilderNPCManager npcManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.marketManager = marketManager;
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().title().toString();

        if (title.contains("NPC-Строитель")) {
            event.setCancelled(true);
            handleNpcGui(player, event.getCurrentItem(), event.getSlot());
            return;
        }
        if (title.contains("Выбор схематики")) {
            event.setCancelled(true);
            handleSchematicGui(player, event.getCurrentItem());
            return;
        }
        if (title.contains("Мини-квесты DES")) {
            event.setCancelled(true);
            handleQuestGui(player, event.getCurrentItem());
            return;
        }
        if (title.contains("Рынок игроков DES")) {
            event.setCancelled(true);
            handleMarketGui(player, event.getCurrentItem(), event.getSlot());
            return;
        }
        if (title.contains("История сделок")) {
            event.setCancelled(true);
        }
    }

    private void handleNpcGui(Player player, ItemStack item, int slot) {
        if (item == null || item.getType().isAir()) return;
        switch (slot) {
            case 10 -> player.openInventory(GUIFactory.createSchematicGUI(plugin, player, npcManager));
            case 12 -> {
                BuilderNPCManager.BuildQuote quote = npcManager.quote(player);
                if (quote == null) {
                    player.sendMessage("§cСначала выбери схему.");
                    return;
                }
                player.sendMessage("§6--- Параметры стройки ---");
                player.sendMessage("§eСхема: §f" + quote.displayName());
                player.sendMessage("§eЦена: §6" + quote.descoin() + " DESCoin");
                player.sendMessage("§eОпыт: §6" + quote.expLevels() + " уровней");
                player.sendMessage("§eКоординаты: §fX=" + quote.x() + " Y=" + quote.y() + " Z=" + quote.z());
                player.sendMessage("§eРазмеры: §f" + quote.sizeX() + "x" + quote.sizeY() + "x" + quote.sizeZ());
                player.sendMessage("§eРесурсы:");
                quote.resources().forEach((k, v) -> player.sendMessage(" §7- " + k + ": " + v));
            }
            case 14 -> {
                BuilderNPCManager.BuildQuote quote = npcManager.quote(player);
                if (quote == null) {
                    player.sendMessage("§cСначала выбери схему.");
                    return;
                }
                npcManager.startBuild(player, quote);
                npcManager.npcComment(player);
            }
            case 16 -> {
                player.sendMessage(plugin.getConfig().getString("messages.build-cancelled", "").replace('&', '§'));
                player.closeInventory();
            }
        }
    }

    private void handleSchematicGui(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        if (item.getType() == Material.ARROW) {
            player.openInventory(GUIFactory.createMainNPCGUI(plugin, player, npcManager));
            return;
        }
        String key = item.getItemMeta().displayName().toString().replace("§6", "");
        key = key.replace("TextComponentImpl{content=", "").replace("}", "").trim();
        if (key.contains("content=")) {
            key = key.substring(key.indexOf("content=") + 8).replace("'", "").replace("]", "");
        }
        npcManager.selectSchematic(player, key);
        player.sendMessage("§aСхематика выбрана: §f" + key);
        player.openInventory(GUIFactory.createMainNPCGUI(plugin, player, npcManager));
    }

    private void handleQuestGui(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        List<MiniQuest> quests = questManager.getAssignedQuests(player.getUniqueId());
        for (MiniQuest quest : quests) {
            if (item.getItemMeta() != null && item.getItemMeta().displayName() != null
                    && item.getItemMeta().displayName().toString().contains(quest.getDescription())) {
                boolean done = questManager.completeQuest(player, quest.getId());
                if (done) {
                    player.openInventory(GUIFactory.createQuestGUI(player, questManager));
                }
                return;
            }
        }
    }

    private void handleMarketGui(Player player, ItemStack item, int slot) {
        if (item == null || item.getType().isAir()) return;
        if (slot < 45) {
            List<MarketListing> listings = marketManager.getListings();
            if (slot >= listings.size()) return;
            marketManager.buy(player, listings.get(slot).getId());
            player.openInventory(GUIFactory.createMarketGUI(player, marketManager, null, null, null, null));
            return;
        }
        if (slot == 46) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                player.sendMessage("§cВозьми предмет в руку.");
                return;
            }
            boolean ok = marketManager.addListing(player, hand.clone(), 50);
            if (ok) {
                player.sendMessage("§aЛот выставлен за 50 DESCoin.");
                player.openInventory(GUIFactory.createMarketGUI(player, marketManager, null, null, null, null));
            }
            return;
        }
        if (slot == 47) {
            player.openInventory(GUIFactory.createMarketHistoryGUI(player, marketManager));
        }
    }
}
