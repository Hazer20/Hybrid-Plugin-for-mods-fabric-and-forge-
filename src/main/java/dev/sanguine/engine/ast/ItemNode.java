package dev.sanguine.engine.ast;

public record ItemNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "item"; }
}
