package dev.sanguine.bridge;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McFunctionTransformerTest {
    @Test
    void keepsCommentsUntouched() {
        McFunctionTransformer transformer = new McFunctionTransformer(Map.of("a", "b"));
        assertEquals("# comment", transformer.transformLine("# comment"));
    }

    @Test
    void appliesConfiguredReplacements() {
        McFunctionTransformer transformer = new McFunctionTransformer(Map.of("generic.max_health", "minecraft:generic.max_health"));
        String transformed = transformer.transformLine("attribute @s generic.max_health base set 40");
        assertEquals("attribute @s minecraft:generic.max_health base set 40", transformed);
    }
}
