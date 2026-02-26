package dev.sanguine.engine.ast;

public record AttributeNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "attribute"; }
}
