package com.desquad.api.blocks;

import java.util.Collection;
import java.util.Optional;

/** Реестр кастомных блоков. */
public interface BlockRegistry {
    void register(CustomBlock block);
    Optional<CustomBlock> get(String id);
    Collection<CustomBlock> all();
}
