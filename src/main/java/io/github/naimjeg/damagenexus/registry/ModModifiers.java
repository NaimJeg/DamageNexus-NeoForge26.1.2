package io.github.naimjeg.damagenexus.registry;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.IDamageModifier;
import io.github.naimjeg.damagenexus.builtin.bridge.VanillaArmorEffectivenessBridge;
import io.github.naimjeg.damagenexus.builtin.bridge.VanillaDefensiveEnchantmentBridge;
import io.github.naimjeg.damagenexus.builtin.modifier.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModModifiers {

    public static final ResourceKey<Registry<IDamageModifier>> MODIFIERS_KEY =
            ResourceKey.createRegistryKey(
                    Identifier.fromNamespaceAndPath(DamageNexus.MODID, "damage_modifiers")
            );

    public static final DeferredRegister<IDamageModifier> MODIFIERS =
            DeferredRegister.create(MODIFIERS_KEY, DamageNexus.MODID);

    public static final Registry<IDamageModifier> REGISTRY =
            MODIFIERS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<IDamageModifier, CritModifier> CRIT =
            MODIFIERS.register("crit", CritModifier::new);

    public static final DeferredHolder<IDamageModifier, VanillaCritModifier> VANILLA_CRIT =
            MODIFIERS.register("vanilla_crit", VanillaCritModifier::new);

    public static final DeferredHolder<IDamageModifier, VanillaArmorEffectivenessBridge> VANILLA_ARMOR_EFFECTIVENESS =
            MODIFIERS.register("vanilla_armor_effectiveness", VanillaArmorEffectivenessBridge::new);

    public static final DeferredHolder<IDamageModifier, VanillaDefensiveEnchantmentBridge> VANILLA_DEFENSIVE_ENCHANTMENT =
            MODIFIERS.register("vanilla_defensive_enchantment", VanillaDefensiveEnchantmentBridge::new);

    public static final DeferredHolder<IDamageModifier, ArmorModifier> ARMOR =
            MODIFIERS.register("armor", ArmorModifier::new);

    public static final DeferredHolder<IDamageModifier, ResistanceModifier> RESISTANCE =
            MODIFIERS.register("resistance", ResistanceModifier::new);

    public static final DeferredHolder<IDamageModifier, VanillaArmorModifier> VANILLA_ARMOR =
            MODIFIERS.register("vanilla_armor", VanillaArmorModifier::new);

    public static void register(IEventBus modBus) {
        MODIFIERS.register(modBus);
    }
}