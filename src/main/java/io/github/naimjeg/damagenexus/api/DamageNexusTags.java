package io.github.naimjeg.damagenexus.api;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public class DamageNexusTags {

    public static final TagKey<Item> SPEARS = create("spears");

    public static class DamageTypes {
        public static final TagKey<DamageType> IS_PHYSICAL = create("is_physical");
        public static final TagKey<DamageType> IS_PROJECTILE = create("is_projectile");
        public static final TagKey<DamageType> IS_RANGED = create("is_ranged");
        public static final TagKey<DamageType> IS_SPELL = create("is_spell");
        public static final TagKey<DamageType> IS_EXPLOSION = create("is_explosion");
        public static final TagKey<DamageType> IS_ENVIRONMENTAL = create("is_environmental");
        public static final TagKey<DamageType> IS_MAGIC = create("is_magic");
        public static final TagKey<DamageType> IS_COLD = create("is_cold");
        public static final TagKey<DamageType> IS_FIRE = create("is_fire");
        public static final TagKey<DamageType> IS_LIGHTNING = create("is_lightning");
        public static final TagKey<DamageType> IS_POISON = create("is_poison");
        public static final TagKey<DamageType> IS_WITHER = create("is_wither");
        public static final TagKey<DamageType> IS_KINETIC = create("is_kinetic");
        public static final TagKey<DamageType> IS_MELEE = create("is_melee");

        public static final TagKey<DamageType> IS_SPEAR_STAB =
                create("is_spear_stab");

        public static final TagKey<DamageType> IS_SPEAR_CHARGE =
                create("is_spear_charge");

        public static final TagKey<DamageType> IS_SPEAR_ATTACK =
                create("is_spear_attack");

        private static TagKey<DamageType> create(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(DamageNexus.MODID, name));
        }

    }

    private static TagKey<Item> create(String name) {
        return TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(DamageNexus.MODID, name)
        );
    }
}