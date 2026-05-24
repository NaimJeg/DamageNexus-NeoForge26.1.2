package io.github.naimjeg.damagenexus.api.enums;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.minecraft.resources.Identifier;

public record DamageChannel(Identifier id, int index) {

    public static final Identifier UNTYPED_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "untyped");

    public static final Identifier PHYSICAL_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "physical");

    public static final Identifier FIRE_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "fire");

    public static final Identifier COLD_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "cold");

    public static final Identifier LIGHTNING_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "lightning");

    public static final Identifier MAGIC_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "magic");

    public static final Identifier WITHER_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "wither");

    public static final Identifier KINETIC_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "kinetic");

    public static final Identifier POISON_ID =
            Identifier.fromNamespaceAndPath(DamageNexus.MODID, "poison");

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DamageChannel other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}