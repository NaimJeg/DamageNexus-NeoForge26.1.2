package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record DamageRuleDisplay(
        RuleDisplayMode mode,
        Optional<String> name,
        Optional<String> description
) {
    /**
     * Backward-compatible default:
     * old rules keep showing as simple standalone rules.
     */
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

                    Codec.STRING
                            .optionalFieldOf("name")
                            .forGetter(DamageRuleDisplay::name),

                    Codec.STRING
                            .optionalFieldOf("description")
                            .forGetter(DamageRuleDisplay::description)
            ).apply(instance, DamageRuleDisplay::new));

    /**
     * Compatibility constructor for existing source code that calls:
     * new DamageRuleDisplay(Optional<String>, Optional<String>)
     */
    public DamageRuleDisplay(
            Optional<String> name,
            Optional<String> description
    ) {
        this(RuleDisplayMode.SIMPLE, name, description);
    }

    public DamageRuleDisplay {
        mode = mode != null ? mode : RuleDisplayMode.SIMPLE;
        name = name != null ? name : Optional.empty();
        description = description != null ? description : Optional.empty();
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