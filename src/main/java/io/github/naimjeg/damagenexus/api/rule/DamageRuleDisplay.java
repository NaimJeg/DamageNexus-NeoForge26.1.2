package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.display.DisplayText;

import java.util.Optional;

public record DamageRuleDisplay(
        RuleDisplayMode mode,
        Optional<DisplayText> name,
        Optional<DisplayText> description
) {
    public static final DamageRuleDisplay EMPTY =
            new DamageRuleDisplay(
                    RuleDisplayMode.SIMPLE,
                    Optional.empty(),
                    Optional.empty()
            );

    public static final DamageRuleDisplay HIDDEN =
            new DamageRuleDisplay(
                    RuleDisplayMode.HIDDEN,
                    Optional.empty(),
                    Optional.empty()
            );

    public static final DamageRuleDisplay AFFIX_MEMBER =
            new DamageRuleDisplay(
                    RuleDisplayMode.AFFIX_MEMBER,
                    Optional.empty(),
                    Optional.empty()
            );

    public static final Codec<DamageRuleDisplay> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    RuleDisplayMode.CODEC
                            .optionalFieldOf("mode", RuleDisplayMode.SIMPLE)
                            .forGetter(DamageRuleDisplay::mode),

                    DisplayText.CODEC
                            .optionalFieldOf("name")
                            .forGetter(DamageRuleDisplay::name),

                    DisplayText.CODEC
                            .optionalFieldOf("description")
                            .forGetter(DamageRuleDisplay::description)
            ).apply(instance, DamageRuleDisplay::new));

    public DamageRuleDisplay {
        mode = mode != null ? mode : RuleDisplayMode.SIMPLE;
        name = name != null ? name : Optional.empty();
        description = description != null ? description : Optional.empty();
    }

    public static DamageRuleDisplay simple(
            Optional<DisplayText> name,
            Optional<DisplayText> description
    ) {
        return new DamageRuleDisplay(
                RuleDisplayMode.SIMPLE,
                name,
                description
        );
    }

    public static DamageRuleDisplay literal(
            String name,
            String description
    ) {
        return simple(
                Optional.ofNullable(name).map(DisplayText::literal),
                Optional.ofNullable(description).map(DisplayText::literal)
        );
    }

    public static DamageRuleDisplay translatable(
            String nameKey,
            String descriptionKey
    ) {
        return simple(
                Optional.ofNullable(nameKey).map(DisplayText::translatable),
                Optional.ofNullable(descriptionKey).map(DisplayText::translatable)
        );
    }

    public DamageRuleDisplay withName(DisplayText name) {
        return new DamageRuleDisplay(
                mode,
                Optional.ofNullable(name),
                description
        );
    }

    public DamageRuleDisplay withDescription(DisplayText description) {
        return new DamageRuleDisplay(
                mode,
                name,
                Optional.ofNullable(description)
        );
    }

    public boolean shouldShowStandalone() {
        return mode == RuleDisplayMode.SIMPLE;
    }

    public DamageRuleDisplay asAffixMember() {
        return new DamageRuleDisplay(
                RuleDisplayMode.AFFIX_MEMBER,
                name,
                description
        );
    }
}