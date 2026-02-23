package dev.sanguine.engine;

import dev.sanguine.engine.ast.AttributeNode;
import dev.sanguine.engine.ast.DamageNode;
import dev.sanguine.engine.ast.ExecuteNode;
import dev.sanguine.engine.parser.McFunctionParser;
import dev.sanguine.engine.parser.McFunctionTokenizer;
import dev.sanguine.engine.transpile.CommandTranspiler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McFunctionParserTest {
    @Test
    void tokenizerKeepsNbtBlobTogether() {
        McFunctionTokenizer tokenizer = new McFunctionTokenizer();
        var tokens = tokenizer.tokenize("give @s minecraft:stick{tag:{display:{Name:'\"X\"'}}} 1");
        assertEquals("give", tokens.getFirst().text());
        assertTrue(tokens.stream().anyMatch(t -> t.text().contains("display")));
    }

    @Test
    void parserBuildsAstNodes() {
        McFunctionParser parser = new McFunctionParser();
        assertInstanceOf(ExecuteNode.class, parser.parseLine("execute as @a run say hi"));
        assertInstanceOf(DamageNode.class, parser.parseLine("damage @s 5 sanguine:blood_magic"));
        assertInstanceOf(AttributeNode.class, parser.parseLine("attribute @s generic.max_health base set 10"));
    }

    @Test
    void transpilerMigratesDamageAndAttributes() {
        McFunctionParser parser = new McFunctionParser();
        CommandTranspiler transpiler = new CommandTranspiler();

        String dmg = transpiler.transpile(parser.parseLine("damage @s 5 sanguine:blood_magic"));
        String attr = transpiler.transpile(parser.parseLine("attribute @s generic.max_health base set 20"));

        assertTrue(dmg.contains("runtime_damage_bridge"));
        assertTrue(attr.contains("minecraft:generic.max_health"));
    }
}
