package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DamageAffixDefinition(
        Identifier id,
        DamageAffixDisplay display,
        DamageAffixSlot slot,
        DamageAffixRarity rarity,
        List<DamageEntryDefinition> entries,
        DamageAffixStacking stacking,
        Optional<Identifier> stackingGroup
) {
    public static final Codec<DamageAffixDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("id")
                            .forGetter(DamageAffixDefinition::id),

                    DamageAffixDisplay.CODEC
                            .fieldOf("display")
                            .forGetter(DamageAffixDefinition::display),

                    DamageAffixSlot.CODEC
                            .optionalFieldOf("slot", DamageAffixSlot.ITEM)
                            .forGetter(DamageAffixDefinition::slot),

                    DamageAffixRarity.CODEC
                            .optionalFieldOf("rarity", DamageAffixRarity.COMMON)
                            .forGetter(DamageAffixDefinition::rarity),

                    DamageEntryDefinition.CODEC
                            .listOf()
                            .fieldOf("entries")
                            .forGetter(DamageAffixDefinition::entries),

                    DamageAffixStacking.CODEC
                            .optionalFieldOf("stacking", DamageAffixStacking.STACK)
                            .forGetter(DamageAffixDefinition::stacking),

                    Identifier.CODEC
                            .optionalFieldOf("stacking_group")
                            .forGetter(DamageAffixDefinition::stackingGroup)
            ).apply(instance, DamageAffixDefinition::new));

    public DamageAffixDefinition {
        id = Objects.requireNonNull(id, "Damage affix id must not be null");
        display = Objects.requireNonNull(display, "Damage affix display must not be null");
        slot = slot != null ? slot : DamageAffixSlot.ITEM;
        rarity = rarity != null ? rarity : DamageAffixRarity.COMMON;
        stacking = stacking != null ? stacking : DamageAffixStacking.STACK;
        stackingGroup = stackingGroup != null ? stackingGroup : Optional.empty();

        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException(
                    "Damage affix must contain at least one entry: " + id
            );
        }

        List<DamageEntryDefinition> normalizedEntries =
                new ArrayList<>(entries.size());

        for (DamageEntryDefinition entry : entries) {
            normalizedEntries.add(Objects.requireNonNull(
                    entry,
                    "Damage affix entry must not be null: " + id
            ));
        }

        entries = List.copyOf(normalizedEntries);
    }
    
    public Identifier stackingKey() {
        return stackingGroup.orElse(id);
    }
}
