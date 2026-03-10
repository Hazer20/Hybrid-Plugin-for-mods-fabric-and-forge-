package com.hazerengine.core.registry;

public class RegistryHub {
    public final ItemRegistry items = new ItemRegistry();
    public final AbilityRegistry abilities = new AbilityRegistry();
    public final RecipeRegistry recipes = new RecipeRegistry();
    public final NPCRegistry npcs = new NPCRegistry();
    public final QuestRegistry quests = new QuestRegistry();
    public final MobRegistry mobs = new MobRegistry();
    public final DungeonRegistry dungeons = new DungeonRegistry();
}
