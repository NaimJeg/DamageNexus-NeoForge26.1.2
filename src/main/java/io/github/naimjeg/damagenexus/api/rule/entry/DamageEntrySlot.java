package io.github.naimjeg.damagenexus.api.rule.entry;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum DamageEntrySlot {
    ITEM,
    WEAPON,
    ARMOR,
    PROJECTILE,
    ENTITY,
    GLOBAL;

    public static final Codec<DamageEntrySlot> CODEC =
            Codec.STRING.xmap(
                    name -> DamageEntrySlot.valueOf(name.toUpperCase(Locale.ROOT)),
                    slot -> slot.name().toLowerCase(Locale.ROOT)
            );
}
