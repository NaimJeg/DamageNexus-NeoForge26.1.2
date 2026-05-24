package io.github.naimjeg.damagenexus.api;

import io.github.naimjeg.damagenexus.DamageNexus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class DamageNexusTags {

    public static class DamageTypes {
        public static final TagKey<DamageType> IS_MAGIC = create("is_magic");
        public static final TagKey<DamageType> IS_COLD = create("is_cold");
        public static final TagKey<DamageType> IS_FIRE = create("is_fire");
        public static final TagKey<DamageType> IS_LIGHTNING = create("is_lightning");
        public static final TagKey<DamageType> IS_POISON = create("is_poison");
        public static final TagKey<DamageType> IS_WITHER = create("is_wither");

        public static final TagKey<DamageType> IS_KINETIC = create("is_kinetic");
        public static final TagKey<DamageType> IS_MELEE = create("is_melee");

        private static TagKey<DamageType> create(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(DamageNexus.MODID, name));
        }
    }
}