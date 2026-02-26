package dev.sanguine.engine.ast;

public record StorageNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "storage"; }
}
