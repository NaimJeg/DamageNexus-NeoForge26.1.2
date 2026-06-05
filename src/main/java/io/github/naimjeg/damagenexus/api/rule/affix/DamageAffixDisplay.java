package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.display.DisplayText;

import java.util.List;
import java.util.Optional;

public record DamageAffixDisplay(
        DisplayText name,
        List<DisplayText> tooltip,
        Optional<DisplayText> flavorText,
        boolean showRuleBreakdown
) {
    public static final DamageAffixDisplay EMPTY =
            new DamageAffixDisplay(
                    DisplayText.EMPTY,
                    List.of(),
                    Optional.empty(),
                    false
            );

    public static final Codec<DamageAffixDisplay> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    DisplayText.CODEC
                            .fieldOf("name")
                            .forGetter(DamageAffixDisplay::name),

                    DisplayText.CODEC
                            .listOf()
                            .optionalFieldOf("tooltip", List.of())
                            .forGetter(DamageAffixDisplay::tooltip),

                    DisplayText.CODEC
                            .optionalFieldOf("flavor_text")
                            .forGetter(DamageAffixDisplay::flavorText),

                    Codec.BOOL
                            .optionalFieldOf("show_rule_breakdown", false)
                            .forGetter(DamageAffixDisplay::showRuleBreakdown)
            ).apply(instance, DamageAffixDisplay::new));

    /**
     * Source compatibility for old Java calls.
     */
    public DamageAffixDisplay(
            String name,
            List<String> tooltip,
            Optional<String> flavorText,
            boolean showRuleBreakdown
    ) {
        this(
                DisplayText.literal(name),
                tooltip == null
                        ? List.of()
                        : tooltip.stream()
                        .map(DisplayText::literal)
                        .toList(),
                flavorText == null
                        ? Optional.empty()
                        : flavorText.map(DisplayText::literal),
                showRuleBreakdown
        );
    }

    public DamageAffixDisplay {
        name = name == null ? DisplayText.EMPTY : name;
        tooltip = tooltip == null ? List.of() : List.copyOf(tooltip);
        flavorText = flavorText == null ? Optional.empty() : flavorText;
    }

    public boolean hasVisibleText() {
        return !name.isBlank()
                || !tooltip.isEmpty()
                || flavorText.filter(text -> !text.isBlank()).isPresent();
    }
}
