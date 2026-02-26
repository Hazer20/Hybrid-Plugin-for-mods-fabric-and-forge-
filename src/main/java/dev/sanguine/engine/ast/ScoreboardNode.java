package dev.sanguine.engine.ast;

public record ScoreboardNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "scoreboard"; }
}
