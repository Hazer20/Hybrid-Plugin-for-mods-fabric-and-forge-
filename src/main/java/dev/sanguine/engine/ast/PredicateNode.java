package dev.sanguine.engine.ast;

public record PredicateNode(String raw) implements CommandAstNode {
    @Override
    public String keyword() { return "predicate"; }
}
