package com.desquad.api.modules;

import java.util.Set;

/** Управление подключаемыми модулями ядра. */
public interface ModuleManager {
    Set<String> knownModules();
    Set<String> enabledModules();
    boolean enable(String moduleId);
    boolean disable(String moduleId);
    void reload();
}
