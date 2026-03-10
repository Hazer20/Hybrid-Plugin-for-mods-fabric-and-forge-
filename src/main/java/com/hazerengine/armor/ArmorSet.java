package com.hazerengine.armor;

import com.hazerengine.core.api.Identifiable;

import java.util.List;

public record ArmorSet(String id, List<String> pieces, String bonusDescription) implements Identifiable { }
