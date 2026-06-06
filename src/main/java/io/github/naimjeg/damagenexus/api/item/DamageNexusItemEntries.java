package io.github.naimjeg.damagenexus.api.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixDefinition;
import io.github.naimjeg.damagenexus.api.rule.affix.DamageAffixSelectionResolver;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntryDefinition;
import io.github.naimjeg.damagenexus.api.rule.entry.DamageEntrySelectionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record DamageNexusItemEntries(
        List<DamageEntryDefinition> entries,
        List<DamageAffixDefinition> affixes
) {
    public static final DamageNexusItemEntries EMPTY =
            new DamageNexusItemEntries(List.of(), List.of());

    public static final Codec<DamageNexusItemEntries> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    DamageEntryDefinition.CODEC
                            .listOf()
                            .optionalFieldOf("entries", List.of())
                            .forGetter(DamageNexusItemEntries::entries),

                    DamageAffixDefinition.CODEC
                            .listOf()
                            .optionalFieldOf("affixes", List.of())
                            .forGetter(DamageNexusItemEntries::affixes)
            ).apply(instance, DamageNexusItemEntries::new));

    public DamageNexusItemEntries {
        entries = copyNonNull(entries, "entries");
        affixes = copyNonNull(affixes, "affixes");
    }

    public boolean isEmpty() {
        return entries.isEmpty() && affixes.isEmpty();
    }

    public List<DamageEntryDefinition> resolvedEntries() {
        return DamageEntrySelectionResolver.resolve(entries);
    }

    public List<DamageAffixDefinition> resolvedAffixes() {
        return DamageAffixSelectionResolver.resolve(affixes);
    }

    public DamageNexusItemEntries withEntries(
            List<DamageEntryDefinition> entries
    ) {
        return new DamageNexusItemEntries(entries, this.affixes);
    }

    public DamageNexusItemEntries withAffixes(
            List<DamageAffixDefinition> affixes
    ) {
        return new DamageNexusItemEntries(this.entries, affixes);
    }

    public DamageNexusItemEntries withAddedEntry(
            DamageEntryDefinition entry
    ) {
        Objects.requireNonNull(entry, "entry must not be null");

        List<DamageEntryDefinition> next =
                new ArrayList<>(this.entries.size() + 1);

        next.addAll(this.entries);
        next.add(entry);

        return new DamageNexusItemEntries(next, this.affixes);
    }

    public DamageNexusItemEntries withAddedAffix(
            DamageAffixDefinition affix
    ) {
        Objects.requireNonNull(affix, "affix must not be null");

        List<DamageAffixDefinition> next =
                new ArrayList<>(this.affixes.size() + 1);

        next.addAll(this.affixes);
        next.add(affix);

        return new DamageNexusItemEntries(this.entries, next);
    }

    private static <T> List<T> copyNonNull(
            List<T> input,
            String name
    ) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }

        List<T> result = new ArrayList<>(input.size());

        for (T value : input) {
            result.add(Objects.requireNonNull(
                    value,
                    name + " must not contain null elements"
            ));
        }

        return List.copyOf(result);
    }
}