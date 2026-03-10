package com.hazerengine.items;

import com.hazerengine.abilities.Ability;
import com.hazerengine.components.ItemComponent;
import com.hazerengine.core.api.Identifiable;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CustomItem implements Identifiable {
    protected final String id;
    protected final String name;
    protected final Material material;
    protected final int modelData;
    protected final List<ItemComponent> components;
    protected final Ability ability;

    public CustomItem(String id, String name, Material material, int modelData, List<ItemComponent> components, Ability ability) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.modelData = modelData;
        this.components = components;
        this.ability = ability;
    }

    public static Builder builder(String id) { return new Builder(id); }
    @Override public String id() { return id; }
    public String name() { return name; }
    public Material material() { return material; }
    public int modelData() { return modelData; }
    public List<ItemComponent> components() { return components; }
    public Ability ability() { return ability; }

    public static class Builder {
        private final String id;
        private String name;
        private Material material = Material.STICK;
        private int modelData;
        private final List<ItemComponent> components = new ArrayList<>();
        private Ability ability;

        Builder(String id) { this.id = id; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder material(Material material) { this.material = material; return this; }
        public Builder modelData(int modelData) { this.modelData = modelData; return this; }
        public Builder component(ItemComponent component) { this.components.add(component); return this; }
        public Builder ability(Ability ability) { this.ability = ability; return this; }
        public CustomItem build() { return new CustomItem(id, name, material, modelData, components, ability); }
    }
}
