package io.github.naimjeg.damagenexus.api;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.minecraft.resources.Identifier;

/**
 * Central factory for DamageNexus-owned Minecraft resource identifiers.
 */
public final class DamageNexusIds {

    private DamageNexusIds() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }
}
