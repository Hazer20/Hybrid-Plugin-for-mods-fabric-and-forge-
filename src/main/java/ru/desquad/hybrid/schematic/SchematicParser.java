package ru.desquad.hybrid.schematic;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.*;
import org.bukkit.Material;
import ru.desquad.hybrid.npc.BuilderNPCManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SchematicParser {

    public ParsedSchematic parse(Path file) throws IOException {
        Tag<?> rootTag = NBTUtil.read(file.toFile());
        if (!(rootTag instanceof CompoundTag root)) {
            throw new IOException("Корневой NBT-тег не CompoundTag");
        }

        String n = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (n.endsWith(".litematic")) {
            return parseLitematic(root);
        }
        return parseSchem(root);
    }

    private ParsedSchematic parseSchem(CompoundTag root) throws IOException {
        int width = getInt(root, "Width", 1);
        int height = getInt(root, "Height", 1);
        int length = getInt(root, "Length", 1);

        CompoundTag paletteTag = root.getCompoundTag("Palette");
        if (paletteTag == null) throw new IOException("В .schem отсутствует Palette");

        Map<Integer, Material> palette = new HashMap<>();
        for (String key : paletteTag.keySet()) {
            int id = getInt(paletteTag, key, -1);
            Material m = toMaterial(key);
            if (id >= 0 && m != null) palette.put(id, m);
        }

        byte[] dataBytes = root.getByteArray("BlockData");
        if (dataBytes == null) throw new IOException("В .schem отсутствует BlockData");
        List<Integer> paletteIds = decodeVarInts(dataBytes);

        List<BuilderNPCManager.PlannedBlock> plan = new ArrayList<>();
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    if (i >= paletteIds.size()) break;
                    Material m = palette.getOrDefault(paletteIds.get(i++), Material.AIR);
                    if (m != Material.AIR) {
                        plan.add(new BuilderNPCManager.PlannedBlock(x, y, z, m));
                    }
                }
            }
        }
        return new ParsedSchematic(width, height, length, plan);
    }

    private ParsedSchematic parseLitematic(CompoundTag root) throws IOException {
        CompoundTag regions = root.getCompoundTag("Regions");
        if (regions == null || regions.size() == 0) throw new IOException("В .litematic отсутствуют Regions");

        String firstRegion = regions.keySet().iterator().next();
        CompoundTag region = regions.getCompoundTag(firstRegion);
        if (region == null) throw new IOException("Region не найден");

        CompoundTag size = region.getCompoundTag("Size");
        if (size == null) throw new IOException("В region отсутствует Size");

        int width = Math.abs(getInt(size, "x", 1));
        int height = Math.abs(getInt(size, "y", 1));
        int length = Math.abs(getInt(size, "z", 1));

        ListTag<?> paletteListRaw = region.getListTag("BlockStatePalette");
        if (!(paletteListRaw instanceof ListTag<?> paletteList) || paletteList.size() == 0) {
            throw new IOException("В .litematic отсутствует BlockStatePalette");
        }

        List<Material> palette = new ArrayList<>();
        for (Tag<?> tag : paletteList) {
            if (tag instanceof CompoundTag st) {
                String name = st.getString("Name");
                palette.add(toMaterial(name));
            }
        }

        long[] states = region.getLongArray("BlockStates");
        if (states == null || states.length == 0) throw new IOException("В .litematic отсутствует BlockStates");

        int total = width * height * length;
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
        long mask = (1L << bits) - 1L;

        List<BuilderNPCManager.PlannedBlock> plan = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            int bitIndex = i * bits;
            int longIndex = bitIndex >>> 6;
            int startBit = bitIndex & 63;
            if (longIndex >= states.length) break;

            long value = (states[longIndex] >>> startBit);
            int endBits = startBit + bits;
            if (endBits > 64 && longIndex + 1 < states.length) {
                value |= (states[longIndex + 1] << (64 - startBit));
            }

            int paletteIndex = (int) (value & mask);
            Material m = paletteIndex >= 0 && paletteIndex < palette.size() ? palette.get(paletteIndex) : Material.AIR;
            if (m == null || m == Material.AIR) continue;

            int x = i % width;
            int z = (i / width) % length;
            int y = (i / (width * length));
            plan.add(new BuilderNPCManager.PlannedBlock(x, y, z, m));
        }
        return new ParsedSchematic(width, height, length, plan);
    }

    private List<Integer> decodeVarInts(byte[] bytes) {
        List<Integer> out = new ArrayList<>();
        int value = 0;
        int position = 0;
        for (byte b : bytes) {
            value |= (b & 0x7F) << position;
            if ((b & 0x80) == 0) {
                out.add(value);
                value = 0;
                position = 0;
            } else {
                position += 7;
            }
        }
        return out;
    }

    private Material toMaterial(String blockId) {
        if (blockId == null || blockId.isEmpty()) return Material.AIR;
        String plain = blockId.contains("[") ? blockId.substring(0, blockId.indexOf('[')) : blockId;
        plain = plain.replace("minecraft:", "").toUpperCase(Locale.ROOT);
        try {
            return Material.valueOf(plain);
        } catch (IllegalArgumentException e) {
            return Material.AIR;
        }
    }

    private int getInt(CompoundTag tag, String key, int def) {
        NumberTag<?> n = tag.getNumberTag(key);
        return n != null ? n.asInt() : def;
    }

    public record ParsedSchematic(int width, int height, int length, List<BuilderNPCManager.PlannedBlock> plan) {}
}
