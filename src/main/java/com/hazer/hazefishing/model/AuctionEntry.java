package com.hazer.hazefishing.model;

import java.time.Instant;
import java.util.UUID;

public record AuctionEntry(
        UUID listingId,
        UUID seller,
        UUID rodId,
        double price,
        Instant expiresAt
) {
}
