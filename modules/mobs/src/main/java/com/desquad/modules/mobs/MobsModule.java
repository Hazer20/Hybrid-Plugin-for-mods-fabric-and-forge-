package com.desquad.modules.mobs;

import com.desquad.engine.module.Module;

/** Подключаемый модуль подсистемы кастомных мобов. */
public final class MobsModule implements Module {
    @Override public String id() { return "mobs"; }
    @Override public void enable() {}
    @Override public void disable() {}
}
