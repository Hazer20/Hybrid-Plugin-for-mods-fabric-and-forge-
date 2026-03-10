package com.hazerengine.magic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaService { private final Map<UUID,Integer> mana = new HashMap<>(); public int get(UUID id){return mana.getOrDefault(id,100);} public void set(UUID id,int val){mana.put(id,val);} }
