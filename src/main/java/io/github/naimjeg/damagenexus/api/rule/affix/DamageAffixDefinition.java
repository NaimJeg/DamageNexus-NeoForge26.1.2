package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDisplay;
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
        List<DamageRuleDefinition> rules,
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

                    DamageRuleDefinition.CODEC
                            .listOf()
                            .fieldOf("rules")
                            .forGetter(DamageAffixDefinition::rules),

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

        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException(
                    "Damage affix must contain at least one rule: " + id
            );
        }

        List<DamageRuleDefinition> normalizedRules =
                new ArrayList<>(rules.size());

        for (DamageRuleDefinition rule : rules) {
            DamageRuleDefinition nonNullRule =
                    Objects.requireNonNull(
                            rule,
                            "Damage affix rule must not be null: " + id
                    );

            /*
             * Rule execution is unchanged, but display ownership moves to affix.
             * This does not affect rule conditions, operations, phase, priority,
             * stacking policy, or trace label.
             */
            normalizedRules.add(asAffixMember(nonNullRule));
        }

        rules = List.copyOf(normalizedRules);
    }

    private static DamageRuleDefinition asAffixMember(
            DamageRuleDefinition rule
    ) {
        DamageRuleDisplay display = rule.display() == null
                ? DamageRuleDisplay.AFFIX_MEMBER
                : rule.display().asAffixMember();

        return new DamageRuleDefinition(
                rule.id(),
                rule.role(),
                rule.phase(),
                rule.priority(),
                display,
                rule.conditions(),
                rule.operations(),
                rule.stacking(),
                rule.stackingGroup(),
                rule.traceLabel()
        );
    }

    public Identifier stackingKey() {
        return stackingGroup.orElse(id);
    }
}