package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.model.AuctionEntry;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AuctionManager {
    private final Map<UUID, AuctionEntry> listings = new ConcurrentHashMap<>();

    public AuctionEntry create(UUID seller, UUID rodId, double price) {
        AuctionEntry entry = new AuctionEntry(UUID.randomUUID(), seller, rodId, price, Instant.now().plus(Duration.ofHours(24)));
        listings.put(entry.listingId(), entry);
        return entry;
    }

    public Collection<AuctionEntry> active() {
        Instant now = Instant.now();
        listings.values().removeIf(entry -> entry.expiresAt().isBefore(now));
        return List.copyOf(listings.values());
    }

    public Optional<AuctionEntry> byId(UUID id) {
        return Optional.ofNullable(listings.get(id));
    }

    public void remove(UUID id) {
        listings.remove(id);
    }
}
