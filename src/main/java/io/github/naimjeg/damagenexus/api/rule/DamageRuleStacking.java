package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum DamageRuleStacking {
    STACK,
    UNIQUE_SOURCE,
    HIGHEST_VALUE,
    LOWEST_VALUE,
    REPLACE;

    public static final Codec<DamageRuleStacking> CODEC =
            Codec.STRING.xmap(
                    name -> DamageRuleStacking.valueOf(name.toUpperCase(Locale.ROOT)),
                    stacking -> stacking.name().toLowerCase(Locale.ROOT)
            );
}