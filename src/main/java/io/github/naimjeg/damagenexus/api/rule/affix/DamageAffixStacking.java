package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum DamageAffixStacking {
    STACK,
    UNIQUE_AFFIX,
    UNIQUE_GROUP,
    HIGHEST_LEVEL,
    REPLACE;

    public static final Codec<DamageAffixStacking> CODEC =
            Codec.STRING.xmap(
                    name -> DamageAffixStacking.valueOf(name.toUpperCase(Locale.ROOT)),
                    stacking -> stacking.name().toLowerCase(Locale.ROOT)
            );
}