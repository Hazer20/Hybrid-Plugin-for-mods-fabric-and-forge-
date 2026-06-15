package com.hybrid.rpgcharacter.data;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/** Mutable character profile stored on the server and mirrored to clients. */
public class CharacterData {
    private UUID playerId;
    private String characterName = "";
    private Gender gender = Gender.MALE;
    private int age = 16;
    private long ageProgress;
    private int heightCm;
    private float heightScale = 1.0F;
    private float geneticsFactor = 1.0F;
    private float weightKg;
    private BodyType bodyType = BodyType.NORMAL;
    private final Set<CharacterTrait> traits = EnumSet.noneOf(CharacterTrait.class);
    private long geneticsSeed;
    private boolean initialized;

    public CharacterData(UUID playerId) {
        this.playerId = playerId;
    }

    /** Serializes this profile to NBT for Forge persistent storage and network packets. */
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerId", playerId);
        tag.putString("CharacterName", characterName);
        tag.putString("Gender", gender.name());
        tag.putInt("Age", age);
        tag.putLong("AgeProgress", ageProgress);
        tag.putInt("HeightCm", heightCm);
        tag.putFloat("HeightScale", heightScale);
        tag.putFloat("GeneticsFactor", geneticsFactor);
        tag.putFloat("WeightKg", weightKg);
        tag.putString("BodyType", bodyType.name());
        ListTag traitList = new ListTag();
        traits.forEach(trait -> traitList.add(StringTag.valueOf(trait.name())));
        tag.put("Traits", traitList);
        tag.putLong("GeneticsSeed", geneticsSeed);
        tag.putBoolean("Initialized", initialized);
        return tag;
    }

    /** Restores a profile from NBT while tolerating missing future fields. */
    public static CharacterData deserialize(CompoundTag tag) {
        CharacterData data = new CharacterData(tag.hasUUID("PlayerId") ? tag.getUUID("PlayerId") : new UUID(0L, 0L));
        data.characterName = tag.getString("CharacterName");
        data.gender = readEnum(tag.getString("Gender"), Gender.MALE, Gender.class);
        data.age = tag.getInt("Age");
        data.ageProgress = tag.getLong("AgeProgress");
        data.heightCm = tag.getInt("HeightCm");
        data.heightScale = tag.getFloat("HeightScale");
        data.geneticsFactor = tag.getFloat("GeneticsFactor");
        data.weightKg = tag.getFloat("WeightKg");
        data.bodyType = readEnum(tag.getString("BodyType"), BodyType.NORMAL, BodyType.class);
        data.traits.clear();
        ListTag traitList = tag.getList("Traits", Tag.TAG_STRING);
        traitList.forEach(traitTag -> data.traits.add(readEnum(traitTag.getAsString(), CharacterTrait.NONE, CharacterTrait.class)));
        data.geneticsSeed = tag.getLong("GeneticsSeed");
        data.initialized = tag.getBoolean("Initialized");
        return data;
    }

    private static <E extends Enum<E>> E readEnum(String value, E fallback, Class<E> type) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public long getAgeProgress() { return ageProgress; }
    public void setAgeProgress(long ageProgress) { this.ageProgress = ageProgress; }
    public int getHeightCm() { return heightCm; }
    public void setHeightCm(int heightCm) { this.heightCm = heightCm; }
    public float getHeightScale() { return heightScale; }
    public void setHeightScale(float heightScale) { this.heightScale = heightScale; }
    public float getGeneticsFactor() { return geneticsFactor; }
    public void setGeneticsFactor(float geneticsFactor) { this.geneticsFactor = geneticsFactor; }
    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public BodyType getBodyType() { return bodyType; }
    public void setBodyType(BodyType bodyType) { this.bodyType = bodyType; }
    public Set<CharacterTrait> getTraits() { return traits; }
    public long getGeneticsSeed() { return geneticsSeed; }
    public void setGeneticsSeed(long geneticsSeed) { this.geneticsSeed = geneticsSeed; }
    public boolean isInitialized() { return initialized; }
    public void setInitialized(boolean initialized) { this.initialized = initialized; }
}
