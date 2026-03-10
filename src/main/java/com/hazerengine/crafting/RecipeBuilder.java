package com.hazerengine.crafting;

import java.util.ArrayList;
import java.util.List;

public class RecipeBuilder {
    private String id;
    private String type;
    private final List<String> shape = new ArrayList<>();
    private String result;

    public static RecipeBuilder create(String id) { RecipeBuilder b = new RecipeBuilder(); b.id = id; return b; }
    public RecipeBuilder shaped(String... lines) { this.type = "shaped"; this.shape.addAll(List.of(lines)); return this; }
    public RecipeBuilder shapeless(String... entries) { this.type = "shapeless"; this.shape.addAll(List.of(entries)); return this; }
    public RecipeBuilder result(String result) { this.result = result; return this; }
    public CustomRecipe build() { return new CustomRecipe(id, type, shape, result); }
}
