# HazerEngine

Massive modular Minecraft Paper 1.21.8 framework by **Hazer_2_0**.

## HazerEngine as central server API kernel
HazerEngine exposes public Bukkit services so third-party plugins can connect at runtime via `Bukkit.getServicesManager()`.

### Public APIs
- `HazerAPI`
- `ItemAPI`
- `AbilityAPI`
- `RecipeAPI`
- `GUIAPI`
- `EconomyAPI`
- `NPCAPI`
- `ResourcePackAPI`

### Third-party plugin dependency
```yaml
depend:
  - HazerEngine
# or
softdepend:
  - HazerEngine
required-hazer-version: 1.1
```

### Example
```java
ItemAPI itemAPI = HazerAPI.getItemAPI();
CustomItem item = itemAPI.createItem("shadow_blade")
        .name("§5Shadow Blade")
        .modelData(2001)
        .build();
itemAPI.register(item);
```

## Modules
- HazerEngineCore
- HazerItems
- HazerFood
- HazerCombat
- HazerAbilities
- HazerCrafting
- HazerGUI
- HazerResourcePack
- HazerNPC
- HazerQuests
- HazerEconomy
- HazerMagic
- HazerDungeons
- HazerMobs
