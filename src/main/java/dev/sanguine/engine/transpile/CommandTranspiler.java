package dev.sanguine.engine.transpile;

import dev.sanguine.engine.ast.*;

public class CommandTranspiler {
    private final ItemComponentTranspiler itemComponentTranspiler = new ItemComponentTranspiler();

    public String transpile(CommandAstNode node) {
        return switch (node) {
            case DamageNode damageNode -> transpileDamage(damageNode);
            case AttributeNode attributeNode -> attributeNode.raw().replace("generic.", "minecraft:generic.");
            case GiveNode giveNode -> itemComponentTranspiler.convertLegacyNbtInGiveLikeCommand(giveNode.raw());
            case ItemNode itemNode -> itemComponentTranspiler.convertLegacyNbtInGiveLikeCommand(itemNode.raw());
            case ExecuteNode executeNode -> executeNode.raw();
            case DataModifyNode dataModifyNode -> dataModifyNode.raw();
            case DataGetNode dataGetNode -> dataGetNode.raw();
            case SummonNode summonNode -> summonNode.raw();
            case ScoreboardNode scoreboardNode -> scoreboardNode.raw();
            case AdvancementNode advancementNode -> advancementNode.raw();
            case PredicateNode predicateNode -> predicateNode.raw();
            case StorageNode storageNode -> storageNode.raw();
            case GenericCommandNode genericCommandNode -> genericCommandNode.raw();
        };
    }

    private String transpileDamage(DamageNode node) {
        String raw = node.raw();
        if (raw.contains(" sanguine:")) {
            return raw + " # runtime_damage_bridge";
        }
        return raw;
    }
}
