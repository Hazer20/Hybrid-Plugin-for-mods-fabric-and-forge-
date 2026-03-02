package ru.desquad.hybrid.schematic;

import net.kyori.adventure.nbt.*;
import org.bukkit.Material;
import ru.desquad.hybrid.npc.BuilderNPCManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SchematicParser {

    public ParsedSchematic parse(Path file) throws IOException {
        CompoundBinaryTag root = BinaryTagIO.unlimitedReader().read(file, BinaryTagIO.Compression.GZIP);
        String n = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (n.endsWith(".litematic")) {
            return parseLitematic(root);
        }
        return parseSchem(root);
    }

    private ParsedSchematic parseSchem(CompoundBinaryTag root) {
        int width = root.getInt("Width", 1);
        int height = root.getInt("Height", 1);
        int length = root.getInt("Length", 1);

        CompoundBinaryTag paletteTag = root.getCompound("Palette");
        Map<Integer, Material> palette = new HashMap<>();
        for (String key : paletteTag.keySet()) {
            int id = paletteTag.getInt(key);
            Material m = toMaterial(key);
            if (m != null) palette.put(id, m);
        }

        byte[] dataBytes = root.getByteArray("BlockData");
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

    private ParsedSchematic parseLitematic(CompoundBinaryTag root) {
        CompoundBinaryTag regions = root.getCompound("Regions");
        String firstRegion = regions.keySet().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Нет Regions"));
        CompoundBinaryTag region = regions.getCompound(firstRegion);

        CompoundBinaryTag size = region.getCompound("Size");
        int width = Math.abs(size.getInt("x", 1));
        int height = Math.abs(size.getInt("y", 1));
        int length = Math.abs(size.getInt("z", 1));

        ListBinaryTag paletteList = region.getList("BlockStatePalette", BinaryTagTypes.COMPOUND);
        List<Material> palette = new ArrayList<>();
        for (BinaryTag tag : paletteList) {
            CompoundBinaryTag st = (CompoundBinaryTag) tag;
            palette.add(toMaterial(st.getString("Name", "minecraft:air")));
        }

        long[] states = region.getLongArray("BlockStates");
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

    public record ParsedSchematic(int width, int height, int length, List<BuilderNPCManager.PlannedBlock> plan) {}
}
