package ru.desquad.hybrid.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.market.MarketListing;
import ru.desquad.hybrid.market.MarketManager;
import ru.desquad.hybrid.market.MarketTransaction;
import ru.desquad.hybrid.npc.BuilderNPCManager;
import ru.desquad.hybrid.quest.MiniQuest;
import ru.desquad.hybrid.quest.QuestManager;
import ru.desquad.hybrid.util.ItemBuilder;

import java.text.SimpleDateFormat;
import java.util.*;

public class GUIFactory {

    public static Inventory createMainNPCGUI(DESHybridPlugin plugin, Player player, BuilderNPCManager npc) {
        Inventory inv = Bukkit.createInventory(player, 54, color(plugin.getConfig().getString("messages.gui-title-main", "NPC")));
        inv.setItem(10, new ItemBuilder(Material.MAP)
                .name("&6Загрузить/выбрать схематику")
                .lore(List.of("&7Выбери проект для постройки", "&7Поддержка: .litematic / .schematic (через список шаблонов)"))
                .build());
        inv.setItem(12, new ItemBuilder(Material.BOOK)
                .name("&eТребуемые ресурсы и цена")
                .lore(List.of("&7Показывает DESCoin, опыт", "&7и координаты зоны строительства"))
                .build());
        inv.setItem(14, new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&aПодтвердить строительство")
                .lore(List.of("&7Запустить сборку и списать ресурсы"))
                .build());
        inv.setItem(16, new ItemBuilder(Material.BARRIER)
                .name("&cОтмена")
                .lore(List.of("&7Отменить процесс"))
                .build());

        String selected = npc.getSelectedSchematic(player);
        inv.setItem(31, new ItemBuilder(Material.PAPER)
                .name("&bТекущая схема: &f" + (selected == null ? "не выбрана" : selected))
                .lore(List.of("&7Выбери схему через кнопку слева"))
                .build());
        return inv;
    }

    public static Inventory createSchematicGUI(DESHybridPlugin plugin, Player player, BuilderNPCManager npc) {
        Inventory inv = Bukkit.createInventory(player, 54, color(plugin.getConfig().getString("messages.gui-title-schematics", "Схемы")));
        int slot = 10;
        for (String key : npc.getAvailableSchematics()) {
            BuilderNPCManager.BuildQuote quote = npc.quote(player);
            inv.setItem(slot++, new ItemBuilder(Material.STRUCTURE_BLOCK)
                    .name("&6" + key)
                    .lore(List.of("&7Нажми, чтобы выбрать схему", "&8Форматы: litematic/schematic"))
                    .build());
            if (slot >= 44) break;
        }
        inv.setItem(49, new ItemBuilder(Material.ARROW).name("&eНазад").build());
        return inv;
    }

    public static Inventory createQuestGUI(Player player, QuestManager manager) {
        Inventory inv = Bukkit.createInventory(player, 54, "§8Мини-квесты DES");
        List<MiniQuest> quests = manager.getAssignedQuests(player.getUniqueId());
        int slot = 10;
        for (MiniQuest quest : quests) {
            Material m = switch (quest.getDifficulty()) {
                case EASY -> Material.LIME_WOOL;
                case MEDIUM -> Material.YELLOW_WOOL;
                case HARD -> Material.RED_WOOL;
            };
            inv.setItem(slot++, new ItemBuilder(m)
                    .name("&6" + quest.getDescription())
                    .lore(List.of(
                            "&7Сложность: &f" + quest.getDifficulty().getDisplay(),
                            "&7Награда: &6" + quest.getReward() + " DESCoin",
                            "&aНажми для симуляции выполнения"
                    )).build());
        }
        inv.setItem(49, new ItemBuilder(Material.CLOCK)
                .name("&eЛимит: 3 квеста в день")
                .lore(List.of("&7Новые квесты будут завтра"))
                .build());
        return inv;
    }

    public static Inventory createMarketGUI(Player player, MarketManager manager, Material materialFilter,
                                            String sellerFilter, Double minPrice, Double maxPrice) {
        Inventory inv = Bukkit.createInventory(player, 54, "§8Рынок игроков DES");
        List<MarketListing> listings = manager.filter(materialFilter, sellerFilter, minPrice, maxPrice);
        int slot = 0;
        for (MarketListing listing : listings) {
            if (slot >= 45) break;
            ItemStack stack = listing.toItemStack();
            inv.setItem(slot, new ItemBuilder(stack.getType())
                    .amount(stack.getAmount())
                    .name("&f" + stack.getType().name())
                    .lore(List.of(
                            "&7Продавец: &e" + listing.getSellerName(),
                            "&7Цена: &6" + listing.getPrice() + " DESCoin",
                            "&8ID: " + listing.getId(),
                            "&aЛКМ - купить"
                    )).build());
            slot++;
        }

        inv.setItem(45, new ItemBuilder(Material.HOPPER)
                .name("&bФильтры")
                .lore(List.of("&7Цена/предмет/продавец", "&8(в демо через config/API)"))
                .build());
        inv.setItem(46, new ItemBuilder(Material.CHEST)
                .name("&aПродать предмет из руки")
                .lore(List.of("&7Цена по умолчанию: 50 DESCoin"))
                .build());
        inv.setItem(47, new ItemBuilder(Material.BOOK)
                .name("&eИстория сделок")
                .build());
        return inv;
    }

    public static Inventory createMarketHistoryGUI(Player player, MarketManager manager) {
        Inventory inv = Bukkit.createInventory(player, 54, "§8История сделок");
        List<MarketTransaction> transactions = manager.getTransactions();
        Collections.reverse(transactions);
        int slot = 0;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM HH:mm");
        for (MarketTransaction transaction : transactions) {
            if (slot >= 54) break;
            inv.setItem(slot++, new ItemBuilder(Material.PAPER)
                    .name("&f" + transaction.getItem() + " x" + transaction.getAmount())
                    .lore(List.of(
                            "&7Покупатель: &a" + transaction.getBuyer(),
                            "&7Продавец: &e" + transaction.getSeller(),
                            "&7Цена: &6" + transaction.getPrice(),
                            "&8" + format.format(new Date(transaction.getTimestamp()))
                    )).build());
        }
        return inv;
    }

    private static String color(String s) {
        return s.replace('&', '§');
    }
}
