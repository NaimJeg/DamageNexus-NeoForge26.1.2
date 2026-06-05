package io.github.naimjeg.damagenexus.api.rule.entry;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum DamageEntryStacking {
    STACK,
    UNIQUE_ENTRY,
    UNIQUE_GROUP,
    REPLACE;

    public static final Codec<DamageEntryStacking> CODEC =
            Codec.STRING.xmap(
                    name -> DamageEntryStacking.valueOf(name.toUpperCase(Locale.ROOT)),
                    stacking -> stacking.name().toLowerCase(Locale.ROOT)
            );
}
