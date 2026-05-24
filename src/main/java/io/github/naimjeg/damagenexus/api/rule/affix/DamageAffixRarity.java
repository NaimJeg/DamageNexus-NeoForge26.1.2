package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum DamageAffixRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    UNIQUE;

    public static final Codec<DamageAffixRarity> CODEC =
            Codec.STRING.xmap(
                    name -> DamageAffixRarity.valueOf(name.toUpperCase(Locale.ROOT)),
                    rarity -> rarity.name().toLowerCase(Locale.ROOT)
            );
}