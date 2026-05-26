package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record DamageRuleDisplay(
        Optional<String> name,
        Optional<String> description
) {
    public static final DamageRuleDisplay EMPTY =
            new DamageRuleDisplay(Optional.empty(), Optional.empty());

    public static final Codec<DamageRuleDisplay> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING
                            .optionalFieldOf("name")
                            .forGetter(DamageRuleDisplay::name),

                    Codec.STRING
                            .optionalFieldOf("description")
                            .forGetter(DamageRuleDisplay::description)
            ).apply(instance, DamageRuleDisplay::new));
}