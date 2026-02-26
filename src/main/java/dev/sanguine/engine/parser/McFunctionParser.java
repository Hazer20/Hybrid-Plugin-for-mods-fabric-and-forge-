package dev.sanguine.engine.parser;

import dev.sanguine.engine.ast.*;

import java.util.List;

public class McFunctionParser {
    private final McFunctionTokenizer tokenizer = new McFunctionTokenizer();

    public CommandAstNode parseLine(String line) {
        if (line == null || line.isBlank() || line.trim().startsWith("#")) {
            return new GenericCommandNode("comment", line == null ? "" : line);
        }

        List<McToken> tokens = tokenizer.tokenize(line.trim());
        if (tokens.isEmpty()) {
            return new GenericCommandNode("empty", line);
        }

        String first = tokens.get(0).text();
        return switch (first) {
            case "execute" -> new ExecuteNode(line, tokens.stream().map(McToken::text).toList());
            case "data" -> parseData(line, tokens);
            case "damage" -> new DamageNode(line, tokens.stream().map(McToken::text).toList());
            case "attribute" -> new AttributeNode(line);
            case "summon" -> new SummonNode(line);
            case "give" -> new GiveNode(line);
            case "item" -> new ItemNode(line);
            case "scoreboard" -> new ScoreboardNode(line);
            case "advancement" -> new AdvancementNode(line);
            case "predicate" -> new PredicateNode(line);
            case "storage" -> new StorageNode(line);
            default -> new GenericCommandNode(first, line);
        };
    }

    private CommandAstNode parseData(String line, List<McToken> tokens) {
        if (tokens.size() > 1 && "modify".equals(tokens.get(1).text())) {
            return new DataModifyNode(line);
        }
        if (tokens.size() > 1 && "get".equals(tokens.get(1).text())) {
            return new DataGetNode(line);
        }
        return new GenericCommandNode("data", line);
    }
}
