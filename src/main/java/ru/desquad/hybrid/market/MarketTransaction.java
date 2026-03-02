package ru.desquad.hybrid.market;

import java.util.HashMap;
import java.util.Map;

public class MarketTransaction {
    private String buyer;
    private String seller;
    private String item;
    private int amount;
    private double price;
    private long timestamp;

    public MarketTransaction() {
    }

    public MarketTransaction(String buyer, String seller, String item, int amount, double price, long timestamp) {
        this.buyer = buyer;
        this.seller = seller;
        this.item = item;
        this.amount = amount;
        this.price = price;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("buyer", buyer);
        map.put("seller", seller);
        map.put("item", item);
        map.put("amount", amount);
        map.put("price", price);
        map.put("timestamp", timestamp);
        return map;
    }

    public static MarketTransaction fromMap(Map<?, ?> map) {
        return new MarketTransaction(
                map.get("buyer").toString(),
                map.get("seller").toString(),
                map.get("item").toString(),
                Integer.parseInt(map.get("amount").toString()),
                Double.parseDouble(map.get("price").toString()),
                Long.parseLong(map.get("timestamp").toString())
        );
    }

    public String getBuyer() { return buyer; }
    public String getSeller() { return seller; }
    public String getItem() { return item; }
    public int getAmount() { return amount; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }
}
