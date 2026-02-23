package dev.sanguine.engine.ast;

public record GiveNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "give"; }
}
