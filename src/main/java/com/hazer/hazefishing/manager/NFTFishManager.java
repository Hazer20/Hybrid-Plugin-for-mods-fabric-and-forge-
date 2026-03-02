package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import com.hazer.hazefishing.model.Rarity;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NFTFishManager {
    private final HazerFishingPlugin plugin;
    private final Map<Long, NFTFish> registry = new ConcurrentHashMap<>();
    private long currentIndex = 1;

    public NFTFishManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public NFTFish generate(Player player, Rarity rarity) {
        UUID fishId = UUID.randomUUID();
        long seed = Objects.hash(fishId, System.nanoTime(), player.getUniqueId());
        Random random = new Random(seed);
        Map<String, String> traits = new LinkedHashMap<>();
        traits.put("scaleColor", pick(random, "Crimson", "Azure", "Emerald", "Obsidian", "Prismatic"));
        traits.put("finType", pick(random, "Royal", "Barbed", "Flow", "Arc", "Myth"));
        traits.put("aura", pick(random, "Solar", "Lunar", "Void", "Stellar", "Chaos"));
        traits.put("glow", pick(random, "Soft", "Radiant", "Pulse", "Hyper"));
        traits.put("mutation", pick(random, "None", "TwinTail", "Halo", "Ether", "Dragon"));
        traits.put("element", pick(random, "Fire", "Water", "Void", "Light"));

        NFTFish fish = new NFTFish(
                currentIndex,
                fishId,
                player.getUniqueId(),
                500 + random.nextDouble(25000),
                20 + random.nextDouble(280),
                UUID.nameUUIDFromBytes((fishId + ":" + seed).getBytes()).toString(),
                random.nextDouble(100),
                currentIndex,
                rarity,
                traits.get("element"),
                traits,
                Instant.now(),
                seed
        );
        registry.put(currentIndex, fish);
        currentIndex++;
        return fish;
    }

    public Collection<NFTFish> topRarest(int amount) {
        return registry.values().stream()
                .sorted(Comparator.comparingDouble(NFTFish::rarityFactor).reversed())
                .limit(amount)
                .toList();
    }

    public Optional<NFTFish> byId(long id) {
        return Optional.ofNullable(registry.get(id));
    }

    public Optional<NFTFish> lastCaught() {
        return byId(currentIndex - 1);
    }

    public String serializeTraits(NFTFish fish) {
        StringBuilder sb = new StringBuilder();
        fish.traits().forEach((k, v) -> {
            if (!sb.isEmpty()) sb.append(';');
            sb.append(k).append('=').append(v);
        });
        return sb.toString();
    }

    private String pick(Random random, String... values) {
        return values[random.nextInt(values.length)];
    }
}
