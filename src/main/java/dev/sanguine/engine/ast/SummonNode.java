package dev.sanguine.engine.ast;

public record SummonNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "summon"; }
}
