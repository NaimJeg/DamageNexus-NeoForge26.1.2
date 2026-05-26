package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.registry.PreMultiplierBucketRegistry;
import net.minecraft.resources.Identifier;

public final class PreMultiplierBuckets {

    public static final Identifier CRIT_DAMAGE_ID = id("crit_damage");
    public static final Identifier PHYSICAL_DAMAGE_ID = id("physical_damage");
    public static final Identifier FIRE_DAMAGE_ID = id("fire_damage");
    public static final Identifier COLD_DAMAGE_ID = id("cold_damage");
    public static final Identifier LIGHTNING_DAMAGE_ID = id("lightning_damage");
    public static final Identifier MAGIC_DAMAGE_ID = id("magic_damage");
    public static final Identifier POISON_DAMAGE_ID = id("poison_damage");
    public static final Identifier WITHER_DAMAGE_ID = id("wither_damage");
    public static final Identifier KINETIC_DAMAGE_ID = id("kinetic_damage");
    public static final Identifier GENERIC_DAMAGE_ID = id("generic_damage");

    public static final Identifier VANILLA_DIFFICULTY_ID = id("vanilla_difficulty");
    public static final Identifier VANILLA_SPECIAL_ATTACK_ID = id("vanilla_special_attack");

    public static int CRIT_DAMAGE = -1;
    public static int PHYSICAL_DAMAGE = -1;
    public static int FIRE_DAMAGE = -1;
    public static int COLD_DAMAGE = -1;
    public static int LIGHTNING_DAMAGE = -1;
    public static int MAGIC_DAMAGE = -1;
    public static int POISON_DAMAGE = -1;
    public static int WITHER_DAMAGE = -1;
    public static int KINETIC_DAMAGE = -1;
    public static int GENERIC_DAMAGE = -1;

    public static int VANILLA_DIFFICULTY = -1;
    public static int VANILLA_SPECIAL_ATTACK = -1;

    private static boolean registered = false;

    private PreMultiplierBuckets() {}

    public static void register() {
        if (registered) {
            return;
        }

        CRIT_DAMAGE = register(CRIT_DAMAGE_ID);
        PHYSICAL_DAMAGE = register(PHYSICAL_DAMAGE_ID);
        FIRE_DAMAGE = register(FIRE_DAMAGE_ID);
        COLD_DAMAGE = register(COLD_DAMAGE_ID);
        LIGHTNING_DAMAGE = register(LIGHTNING_DAMAGE_ID);
        MAGIC_DAMAGE = register(MAGIC_DAMAGE_ID);
        POISON_DAMAGE = register(POISON_DAMAGE_ID);
        WITHER_DAMAGE = register(WITHER_DAMAGE_ID);
        KINETIC_DAMAGE = register(KINETIC_DAMAGE_ID);
        GENERIC_DAMAGE = register(GENERIC_DAMAGE_ID);

        VANILLA_DIFFICULTY = register(VANILLA_DIFFICULTY_ID);
        VANILLA_SPECIAL_ATTACK = register(VANILLA_SPECIAL_ATTACK_ID);

        registered = true;
    }

    public static int forChannelDamage(DamageChannel channel) {
        Identifier id = channel.id();

        if (id.equals(DamageChannel.PHYSICAL_ID)) {
            return PHYSICAL_DAMAGE;
        }

        if (id.equals(DamageChannel.FIRE_ID)) {
            return FIRE_DAMAGE;
        }

        if (id.equals(DamageChannel.COLD_ID)) {
            return COLD_DAMAGE;
        }

        if (id.equals(DamageChannel.LIGHTNING_ID)) {
            return LIGHTNING_DAMAGE;
        }

        if (id.equals(DamageChannel.MAGIC_ID)) {
            return MAGIC_DAMAGE;
        }

        if (id.equals(DamageChannel.POISON_ID)) {
            return POISON_DAMAGE;
        }

        if (id.equals(DamageChannel.WITHER_ID)) {
            return WITHER_DAMAGE;
        }

        if (id.equals(DamageChannel.KINETIC_ID)) {
            return KINETIC_DAMAGE;
        }

        return GENERIC_DAMAGE;
    }

    private static int register(Identifier id) {
        return PreMultiplierBucketRegistry.registerPreMultiplierBucket(id);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }
}