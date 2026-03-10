package com.desquad.engine.blocks;

import com.desquad.api.blocks.BlockRegistry;
import com.desquad.api.blocks.CustomBlock;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Потокобезопасный реестр кастомных блоков. */
public final class InMemoryBlockRegistry implements BlockRegistry {
    private final Map<String, CustomBlock> blocks = new ConcurrentHashMap<>();

    @Override
    public void register(CustomBlock block) {
        blocks.put(block.id(), block);
    }

    @Override
    public Optional<CustomBlock> get(String id) {
        return Optional.ofNullable(blocks.get(id));
    }

    @Override
    public Collection<CustomBlock> all() {
        return blocks.values();
    }
}
