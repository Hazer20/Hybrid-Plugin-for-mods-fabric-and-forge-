package dev.sanguine.engine.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VersionTranslationLayer {
    public void translatePackMeta(JsonObject root, int packFormat) {
        JsonObject pack = root.has("pack") && root.get("pack").isJsonObject() ? root.getAsJsonObject("pack") : new JsonObject();
        pack.addProperty("pack_format", packFormat);
        root.add("pack", pack);
    }

    public void translatePredicate(JsonObject root) {
        if (root.has("condition") && root.get("condition").isJsonPrimitive()) {
            String cond = root.get("condition").getAsString();
            if (!cond.contains(":")) {
                root.addProperty("condition", "minecraft:" + cond);
            }
        }
    }

    public void translateLootTable(JsonObject root) {
        if (!root.has("pools") || !root.get("pools").isJsonArray()) return;
        for (JsonElement poolEl : root.getAsJsonArray("pools")) {
            if (!poolEl.isJsonObject()) continue;
            JsonObject pool = poolEl.getAsJsonObject();
            if (pool.has("functions") && pool.get("functions").isJsonArray()) {
                for (JsonElement fnEl : pool.getAsJsonArray("functions")) {
                    if (!fnEl.isJsonObject()) continue;
                    JsonObject fn = fnEl.getAsJsonObject();
                    if (fn.has("function") && fn.get("function").isJsonPrimitive()) {
                        String function = fn.get("function").getAsString();
                        if (!function.contains(":")) fn.addProperty("function", "minecraft:" + function);
                    }
                }
            }
        }
    }

    public void translateAdvancement(JsonObject root) {
        if (!root.has("criteria") || !root.get("criteria").isJsonObject()) return;
        JsonObject criteria = root.getAsJsonObject("criteria");
        for (String key : criteria.keySet()) {
            JsonObject criterion = criteria.getAsJsonObject(key);
            if (criterion.has("trigger") && criterion.get("trigger").isJsonPrimitive()) {
                String trigger = criterion.get("trigger").getAsString();
                if (!trigger.contains(":")) criterion.addProperty("trigger", "minecraft:" + trigger);
            }
        }
    }

    public void translateRecipe(JsonObject root) {
        if (root.has("result") && root.get("result").isJsonPrimitive()) {
            JsonObject result = new JsonObject();
            result.addProperty("id", root.get("result").getAsString());
            root.add("result", result);
        }
    }

    public void translateBlockstate(JsonObject root) {
        if (!root.has("multipart") || !root.get("multipart").isJsonArray()) return;
        JsonArray multipart = root.getAsJsonArray("multipart");
        for (JsonElement element : multipart) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("when") && obj.get("when").isJsonPrimitive()) {
                JsonObject when = new JsonObject();
                when.addProperty("state", obj.get("when").getAsString());
                obj.add("when", when);
            }
        }
    }

    public void translateModel(JsonObject root) {
        if (root.has("overrides") && root.get("overrides").isJsonArray()) {
            root.addProperty("_legacy_overrides_detected", true);
        }
    }

    public String translateCommand(String line) {
        String out = line;
        if (out.startsWith("attribute ")) out = out.replace(" generic.", " minecraft:generic.");
        if (out.contains("CustomModelData:")) out = out.replace("CustomModelData:", "custom_model_data:");
        return out;
    }
}
