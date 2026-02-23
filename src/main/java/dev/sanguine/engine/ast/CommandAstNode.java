package dev.sanguine.engine.ast;

public sealed interface CommandAstNode permits ExecuteNode, DataModifyNode, DataGetNode, DamageNode,
    AttributeNode, SummonNode, GiveNode, ItemNode, ScoreboardNode, AdvancementNode, PredicateNode,
    StorageNode, GenericCommandNode {

    String raw();

    String keyword();
}
