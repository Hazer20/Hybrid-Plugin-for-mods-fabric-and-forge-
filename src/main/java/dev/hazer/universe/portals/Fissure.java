package dev.hazer.universe.portals;

import org.bukkit.Location;

import java.util.UUID;

public class Fissure {
    private final UUID id;
    private FissureType type;
    private Location location;
    private long expireAt;

    public Fissure(UUID id, FissureType type, Location location, long expireAt) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.expireAt = expireAt;
    }

    public UUID getId() {
        return id;
    }

    public FissureType getType() {
        return type;
    }

    public void setType(FissureType type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }
}
