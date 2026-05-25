package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record AffixDisplay(
        Optional<String> name,
        Optional<String> description
) {
    public static final AffixDisplay EMPTY =
            new AffixDisplay(Optional.empty(), Optional.empty());

    public static final Codec<AffixDisplay> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING
                            .optionalFieldOf("name")
                            .forGetter(AffixDisplay::name),

                    Codec.STRING
                            .optionalFieldOf("description")
                            .forGetter(AffixDisplay::description)
            ).apply(instance, AffixDisplay::new));
}