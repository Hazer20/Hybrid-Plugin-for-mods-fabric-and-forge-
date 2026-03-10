package com.hazerengine.bootstrap;

import com.hazerengine.abilities.impl.*;
import com.hazerengine.armor.ArmorSet;
import com.hazerengine.components.DamageComponent;
import com.hazerengine.components.DurabilityComponent;
import com.hazerengine.components.FoodComponent;
import com.hazerengine.components.TextureComponent;
import com.hazerengine.core.HazerEnginePlugin;
import com.hazerengine.crafting.RecipeBuilder;
import com.hazerengine.dungeons.DungeonDefinition;
import com.hazerengine.items.CustomItem;
import com.hazerengine.mobs.CustomMob;
import com.hazerengine.npc.EngineNPC;
import com.hazerengine.quests.Quest;
import org.bukkit.Material;

import java.util.List;

public class ExampleContentLoader {
    public static void load(HazerEnginePlugin plugin) {
        var reg = plugin.getRegistryHub();

        reg.abilities.register(new FireBurstAbility());
        reg.abilities.register(new DashAbility());
        reg.abilities.register(new LightningStrikeAbility());
        reg.abilities.register(new VampireAbility());
        reg.abilities.register(new ShadowStepAbility());
        reg.abilities.register(new IceNovaAbility());
        reg.abilities.register(new EarthShatterAbility());
        reg.abilities.register(new WindSlashAbility());
        reg.abilities.register(new ArcanePulseAbility());
        reg.abilities.register(new SolarFlareAbility());

        for (int i = 1; i <= 30; i++) {
            CustomItem item = CustomItem.builder("custom_item_" + i)
                    .name("§6Hazer Item " + i)
                    .material(i <= 20 ? Material.NETHERITE_SWORD : Material.BLAZE_ROD)
                    .modelData(1000 + i)
                    .component(new DamageComponent(10 + i))
                    .component(new DurabilityComponent(500 + i * 10))
                    .component(new TextureComponent(1000 + i, "item/custom_" + i))
                    .ability(new FireBurstAbility())
                    .build();
            reg.items.register(item);
        }

        for (int i = 1; i <= 15; i++) {
            reg.items.register(CustomItem.builder("food_" + i)
                    .name("§dMagic Food " + i)
                    .material(Material.COOKIE)
                    .modelData(2000 + i)
                    .component(new FoodComponent(4 + i % 6, 0.4f + i * 0.03f))
                    .build());
        }

        for (int i = 1; i <= 10; i++) {
            plugin.getArmorSets().add(new ArmorSet("armor_set_" + i, List.of("helmet", "chestplate", "leggings", "boots"), "Bonus " + i));
        }

        for (int i = 1; i <= 5; i++) {
            reg.npcs.register(new EngineNPC("npc_" + i, "NPC " + i, "Welcome to HazerEngine"));
            reg.quests.register(new Quest("quest_" + i, i % 2 == 0 ? "KillQuest" : "CollectQuest", "Complete target " + i, 10 * i));
            reg.mobs.register(new CustomMob("mob_" + i, "ZOMBIE", List.of("dashability"), List.of("custom_item_" + i)));
            reg.dungeons.register(new DungeonDefinition("dungeon_" + i, 5 + i, "boss_" + i, List.of("coins", "gems")));
        }

        reg.recipes.register(RecipeBuilder.create("lava_sword").shaped("ABC", " D ", " D ").result("custom_item_1").build());
    }
}
