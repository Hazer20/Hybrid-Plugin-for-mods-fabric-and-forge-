package dev.sanguine.engine.ast;

public record DataModifyNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "data modify"; }
}
