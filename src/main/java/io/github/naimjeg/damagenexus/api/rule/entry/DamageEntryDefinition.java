package io.github.naimjeg.damagenexus.api.rule.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DamageEntryDefinition(
        Identifier id,
        DamageEntryDisplay display,
        DamageEntrySlot slot,
        List<DamageRuleDefinition> rules,
        DamageEntryStacking stacking,
        Optional<Identifier> stackingGroup
) {
    public static final Codec<DamageEntryDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("id")
                            .forGetter(DamageEntryDefinition::id),

                    DamageEntryDisplay.CODEC
                            .fieldOf("display")
                            .forGetter(DamageEntryDefinition::display),

                    DamageEntrySlot.CODEC
                            .optionalFieldOf("slot", DamageEntrySlot.ITEM)
                            .forGetter(DamageEntryDefinition::slot),

                    DamageRuleDefinition.CODEC
                            .listOf()
                            .fieldOf("rules")
                            .forGetter(DamageEntryDefinition::rules),

                    DamageEntryStacking.CODEC
                            .optionalFieldOf("stacking", DamageEntryStacking.STACK)
                            .forGetter(DamageEntryDefinition::stacking),

                    Identifier.CODEC
                            .optionalFieldOf("stacking_group")
                            .forGetter(DamageEntryDefinition::stackingGroup)
            ).apply(instance, DamageEntryDefinition::new));

    public DamageEntryDefinition {
        id = Objects.requireNonNull(id, "Damage entry id must not be null");
        display = display == null ? DamageEntryDisplay.EMPTY : display;
        slot = slot == null ? DamageEntrySlot.ITEM : slot;
        stacking = stacking == null ? DamageEntryStacking.STACK : stacking;
        stackingGroup = stackingGroup == null ? Optional.empty() : stackingGroup;

        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException(
                    "Damage entry must contain at least one rule: " + id
            );
        }

        List<DamageRuleDefinition> normalizedRules =
                new ArrayList<>(rules.size());

        for (DamageRuleDefinition rule : rules) {
            normalizedRules.add(Objects.requireNonNull(
                    rule,
                    "Damage entry rule must not be null: " + id
            ));
        }

        rules = List.copyOf(normalizedRules);
    }
    
    public Identifier stackingKey() {
        return stackingGroup.orElse(id);
    }
}
