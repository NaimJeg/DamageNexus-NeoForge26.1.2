package io.github.naimjeg.damagenexus.api.rule.affix;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum DamageAffixSlot {
    ITEM,
    WEAPON,
    ARMOR,
    PROJECTILE,
    ENTITY,
    GLOBAL;

    public static final Codec<DamageAffixSlot> CODEC =
            Codec.STRING.xmap(
                    name -> DamageAffixSlot.valueOf(name.toUpperCase(Locale.ROOT)),
                    slot -> slot.name().toLowerCase(Locale.ROOT)
            );
}