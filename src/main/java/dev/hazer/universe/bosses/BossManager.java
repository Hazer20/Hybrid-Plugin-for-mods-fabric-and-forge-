package dev.hazer.universe.bosses;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class BossManager {
    public LivingEntity spawnHollowKing(World world, Location location) {
        LivingEntity boss = (LivingEntity) world.spawnEntity(location, EntityType.WITHER_SKELETON);
        boss.customName(Component.text("The Hollow King"));
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(400.0);
        boss.setHealth(400.0);
        return boss;
    }

    public LivingEntity spawnCoreAI(World world, Location location) {
        LivingEntity boss = (LivingEntity) world.spawnEntity(location, EntityType.IRON_GOLEM);
        boss.customName(Component.text("Core AI"));
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(500.0);
        boss.setHealth(500.0);
        return boss;
    }

    public LivingEntity spawnLeviathan(World world, Location location) {
        LivingEntity boss = (LivingEntity) world.spawnEntity(location, EntityType.ELDER_GUARDIAN);
        boss.customName(Component.text("Leviathan"));
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(600.0);
        boss.setHealth(600.0);
        return boss;
    }
}
