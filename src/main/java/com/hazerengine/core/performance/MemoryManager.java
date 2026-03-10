package com.hazerengine.core.performance;

public class MemoryManager {
    public long freeMemory() { return Runtime.getRuntime().freeMemory(); }
    public long maxMemory() { return Runtime.getRuntime().maxMemory(); }
    public void hintGc() { System.gc(); }
}
