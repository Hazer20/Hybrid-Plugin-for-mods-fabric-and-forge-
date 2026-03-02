package ru.desquad.hybrid.market;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarketListing {
    private String id;
    private UUID seller;
    private String sellerName;
    private Material material;
    private int amount;
    private double price;

    public MarketListing() {
    }

    public MarketListing(String id, UUID seller, String sellerName, Material material, int amount, double price) {
        this.id = id;
        this.seller = seller;
        this.sellerName = sellerName;
        this.material = material;
        this.amount = amount;
        this.price = price;
    }

    public ItemStack toItemStack() {
        return new ItemStack(material, amount);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("seller", seller.toString());
        map.put("sellerName", sellerName);
        map.put("material", material.name());
        map.put("amount", amount);
        map.put("price", price);
        return map;
    }

    public static MarketListing fromMap(Map<?, ?> map) {
        return new MarketListing(
                map.get("id").toString(),
                UUID.fromString(map.get("seller").toString()),
                map.get("sellerName").toString(),
                Material.valueOf(map.get("material").toString()),
                Integer.parseInt(map.get("amount").toString()),
                Double.parseDouble(map.get("price").toString())
        );
    }

    public String getId() { return id; }
    public UUID getSeller() { return seller; }
    public String getSellerName() { return sellerName; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public double getPrice() { return price; }
}
