package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.core.registry.DamageModifierRegistry;
import net.minecraft.resources.Identifier;

public final class ModConstants {
    public static int BASE_ADDITIVE = -1;
    public static int CRIT_DAMAGE = -1;
    public static int FIRE_DAMAGE = -1;
    public static int COLD_DAMAGE = -1;
    public static int LIGHTNING_DAMAGE = -1;
    public static int MAGIC_DAMAGE = -1;
    public static int POISON_DAMAGE = -1;
    public static int WITHER_DAMAGE = -1;
    public static int KINETIC_DAMAGE = -1;

    private static boolean registered = false;

    private ModConstants() {}

    public static void register() {
        if (registered) return;

        BASE_ADDITIVE = register("base_additive");
        CRIT_DAMAGE = register("crit_damage");

        FIRE_DAMAGE = register("fire_damage");
        COLD_DAMAGE = register("cold_damage");
        LIGHTNING_DAMAGE = register("lightning_damage");
        MAGIC_DAMAGE = register("magic_damage");
        POISON_DAMAGE = register("poison_damage");
        WITHER_DAMAGE = register("wither_damage");
        KINETIC_DAMAGE = register("kinetic_damage");

        registered = true;
    }

    private static int register(String path) {
        return DamageModifierRegistry.registerPreModifier(
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, path)
        );
    }
}