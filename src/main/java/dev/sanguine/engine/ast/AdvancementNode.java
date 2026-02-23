package dev.sanguine.engine.ast;

public record AdvancementNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "advancement"; }
}
