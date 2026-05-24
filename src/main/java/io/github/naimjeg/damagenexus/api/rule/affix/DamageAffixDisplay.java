package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record DamageAffixDisplay(
        String name,
        List<String> tooltip,
        Optional<String> flavorText,
        boolean showRuleBreakdown
) {
    public static final DamageAffixDisplay EMPTY =
            new DamageAffixDisplay(
                    "",
                    List.of(),
                    Optional.empty(),
                    false
            );

    public static final Codec<DamageAffixDisplay> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING
                            .fieldOf("name")
                            .forGetter(DamageAffixDisplay::name),

                    Codec.STRING
                            .listOf()
                            .optionalFieldOf("tooltip", List.of())
                            .forGetter(DamageAffixDisplay::tooltip),

                    Codec.STRING
                            .optionalFieldOf("flavor_text")
                            .forGetter(DamageAffixDisplay::flavorText),

                    Codec.BOOL
                            .optionalFieldOf("show_rule_breakdown", false)
                            .forGetter(DamageAffixDisplay::showRuleBreakdown)
            ).apply(instance, DamageAffixDisplay::new));

    public DamageAffixDisplay {
        name = name != null ? name : "";
        tooltip = tooltip != null ? List.copyOf(tooltip) : List.of();
        flavorText = flavorText != null ? flavorText : Optional.empty();
    }

    public boolean hasVisibleText() {
        return !name.isBlank()
                || !tooltip.isEmpty()
                || flavorText.filter(text -> !text.isBlank()).isPresent();
    }
}