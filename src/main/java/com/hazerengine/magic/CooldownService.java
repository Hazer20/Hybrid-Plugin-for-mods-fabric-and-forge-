package com.hazerengine.magic;

import java.util.HashMap;
import java.util.Map;

public class CooldownService { private final Map<String,Long> data = new HashMap<>(); public boolean ready(String key){return System.currentTimeMillis()>=data.getOrDefault(key,0L);} public void set(String key,long ms){data.put(key,System.currentTimeMillis()+ms);} }
