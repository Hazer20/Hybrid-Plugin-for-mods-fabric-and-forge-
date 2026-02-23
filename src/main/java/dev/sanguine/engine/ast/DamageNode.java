package dev.sanguine.engine.ast;

import java.util.List;

public record DamageNode(String raw, List<String> tokens) implements CommandAstNode {
    @Override
    public String keyword() { return "damage"; }
}
