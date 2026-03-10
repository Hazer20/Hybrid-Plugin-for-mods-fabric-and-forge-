package com.desquad.modules.performance;

import com.desquad.engine.module.Module;

/** Runtime-модуль performance подсистемы. */
public final class PerformanceModule implements Module {
    @Override public String id() { return "performance"; }
    @Override public void enable() {}
    @Override public void disable() {}
}
