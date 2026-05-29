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
    public static final Identifier VANILLA_SPEAR_STAB_ID = id("vanilla_spear_stab");
    public static final Identifier VANILLA_SPEAR_CHARGE_ID = id("vanilla_spear_charge");
    public static final Identifier VANILLA_SPEAR_ATTACK_ID = id("vanilla_spear_attack");
    public static final Identifier VANILLA_PLAYER_ATTACK_ID = id("vanilla_player_attack");
    public static final Identifier VANILLA_PROJECTILE_ID = id("vanilla_projectile");

    public static final Identifier VANILLA_MELEE_BASE_ID = id("vanilla_melee_base");
    public static final Identifier VANILLA_MELEE_ENCHANTMENT_ID = id("vanilla_melee_enchantment");
    public static final Identifier VANILLA_WEAPON_SPECIAL_ID = id("vanilla_weapon_special");
    public static final Identifier VANILLA_PROJECTILE_BASE_ID = id("vanilla_projectile_base");
    public static final Identifier VANILLA_PROJECTILE_ENCHANTMENT_ID = id("vanilla_projectile_enchantment");
    public static final Identifier DN_RULE_BASE_ID = id("dn_rule_base");
    public static final Identifier DN_TRUE_DAMAGE_ID = id("dn_true_damage");

    public static int VANILLA_MELEE_BASE = -1;
    public static int VANILLA_MELEE_ENCHANTMENT = -1;
    public static int VANILLA_WEAPON_SPECIAL = -1;
    public static int VANILLA_PROJECTILE_BASE = -1;
    public static int VANILLA_PROJECTILE_ENCHANTMENT = -1;
    public static int DN_RULE_BASE = -1;
    public static int DN_TRUE_DAMAGE = -1;

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
    public static int VANILLA_SPEAR_STAB = -1;
    public static int VANILLA_SPEAR_CHARGE = -1;
    public static int VANILLA_SPEAR_ATTACK = -1;
    public static int VANILLA_PLAYER_ATTACK = -1;
    public static int VANILLA_PROJECTILE = -1;

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
        VANILLA_SPEAR_STAB = register(VANILLA_SPEAR_STAB_ID);
        VANILLA_SPEAR_CHARGE = register(VANILLA_SPEAR_CHARGE_ID);
        VANILLA_SPEAR_ATTACK = register(VANILLA_SPEAR_ATTACK_ID);
        VANILLA_PLAYER_ATTACK = register(VANILLA_PLAYER_ATTACK_ID);
        VANILLA_PROJECTILE = register(VANILLA_PROJECTILE_ID);

        VANILLA_MELEE_BASE = register(VANILLA_MELEE_BASE_ID);
        VANILLA_MELEE_ENCHANTMENT = register(VANILLA_MELEE_ENCHANTMENT_ID);
        VANILLA_WEAPON_SPECIAL = register(VANILLA_WEAPON_SPECIAL_ID);
        VANILLA_PROJECTILE_BASE = register(VANILLA_PROJECTILE_BASE_ID);
        VANILLA_PROJECTILE_ENCHANTMENT = register(VANILLA_PROJECTILE_ENCHANTMENT_ID);
        DN_RULE_BASE = register(DN_RULE_BASE_ID);
        DN_TRUE_DAMAGE = register(DN_TRUE_DAMAGE_ID);

        VANILLA_PLAYER_ATTACK = VANILLA_MELEE_BASE;
        VANILLA_PROJECTILE = VANILLA_PROJECTILE_BASE;
        VANILLA_SPECIAL_ATTACK = VANILLA_WEAPON_SPECIAL;

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