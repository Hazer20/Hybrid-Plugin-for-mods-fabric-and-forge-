package com.desquad.modules.blocks;

import com.desquad.engine.module.Module;

/** Подключаемый модуль подсистемы кастомных блоков. */
public final class BlocksModule implements Module {
    @Override public String id() { return "blocks"; }
    @Override public void enable() {}
    @Override public void disable() {}
}
