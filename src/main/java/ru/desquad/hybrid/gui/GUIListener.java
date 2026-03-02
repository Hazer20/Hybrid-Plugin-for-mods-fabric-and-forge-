package ru.desquad.hybrid.gui;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import ru.desquad.hybrid.quest.QuestManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

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
        String title = PLAIN.serialize(event.getView().title());

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
            return;
        }
        if (title.contains("Крафт призыва NPC")) {
            event.setCancelled(true);
            handleNpcCraftGui(player, event.getCurrentItem(), event.getSlot());
        }
    }

    private void handleNpcCraftGui(Player player, ItemStack item, int slot) {
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot != 31 || item == null || item.getType() != Material.EMERALD_BLOCK) return;
        if (npcManager.hasCraftedNpcToken(player.getUniqueId()) && plugin.getConfig().getBoolean("npc-craft.one-time-per-player", true)) {
            player.sendMessage("§cТы уже создавал предмет призыва NPC.");
            player.closeInventory();
            return;
        }

        Map<Material, Integer> recipe = new LinkedHashMap<>();
        if (plugin.getConfig().getConfigurationSection("npc-craft.recipe") != null) {
            for (String key : plugin.getConfig().getConfigurationSection("npc-craft.recipe").getKeys(false)) {
                try {
                    recipe.put(Material.valueOf(key), plugin.getConfig().getInt("npc-craft.recipe." + key));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        for (Map.Entry<Material, Integer> entry : recipe.entrySet()) {
            if (count(player, entry.getKey()) < entry.getValue()) {
                player.sendMessage("§cНедостаточно ресурсов для сложного крафта: " + entry.getKey().name() + " x" + entry.getValue());
                return;
            }
        }

        for (Map.Entry<Material, Integer> entry : recipe.entrySet()) {
            remove(player, entry.getKey(), entry.getValue());
        }

        player.getInventory().addItem(GUIFactory.createNpcTokenItem());
        npcManager.setCraftedNpcToken(player.getUniqueId(), true);
        player.sendMessage("§aКрафт завершён! Ты получил предмет призыва NPC (доступно только 1 раз). ");
        player.closeInventory();
    }

    private int count(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) count += stack.getAmount();
        }
        return count;
    }

    private void remove(Player player, Material material, int amount) {
        int left = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material) continue;
            int take = Math.min(left, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            left -= take;
            if (stack.getAmount() <= 0) contents[i] = null;
        }
        player.getInventory().setContents(contents);
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
        String key = PLAIN.serialize(item.getItemMeta().displayName());
        npcManager.selectSchematic(player, key);
        player.sendMessage("§aСхематика выбрана: §f" + key);
        player.openInventory(GUIFactory.createMainNPCGUI(plugin, player, npcManager));
    }

    private void handleQuestGui(Player player, ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null || item.getItemMeta().lore() == null) return;
        String questId = null;
        for (var line : item.getItemMeta().lore()) {
            String text = PLAIN.serialize(line);
            if (text.startsWith("ID:")) {
                questId = text.substring(3);
                break;
            }
        }
        if (questId == null) return;

        boolean claimed = questManager.tryClaimQuest(player, questId);
        if (!claimed) {
            player.sendMessage("§cКвест ещё не выполнен или награда уже получена.");
        }
        player.openInventory(GUIFactory.createQuestGUI(player, questManager));
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
