package dev.sanguine.engine.ast;

public record DataGetNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "data get"; }
}
