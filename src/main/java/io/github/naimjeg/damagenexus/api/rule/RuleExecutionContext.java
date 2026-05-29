package io.github.naimjeg.damagenexus.api.rule;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record RuleExecutionContext(
        DamageRuleProviderType providerType,
        DamageRuleRole role,
        @Nullable LivingEntity owner,
        ItemStack sourceStack,
        @Nullable EquipmentSlot equipmentSlot,
        @Nullable Entity sourceEntity
) {
    public static RuleExecutionContext itemWeapon(
            LivingEntity owner,
            ItemStack stack,
            EquipmentSlot slot
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.ITEM_WEAPON,
                DamageRuleRole.OFFENSIVE,
                owner,
                stack,
                slot,
                owner
        );
    }

    public static RuleExecutionContext itemArmor(
            LivingEntity owner,
            ItemStack stack,
            EquipmentSlot slot
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.ITEM_ARMOR,
                DamageRuleRole.DEFENSIVE,
                owner,
                stack,
                slot,
                owner
        );
    }

    public static RuleExecutionContext vanillaEnchantment(
            DamageRuleRole role,
            @Nullable LivingEntity owner,
            ItemStack stack,
            @Nullable EquipmentSlot slot
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.VANILLA_ENCHANTMENT,
                role,
                owner,
                stack,
                slot,
                owner
        );
    }

    public static RuleExecutionContext vanillaMobEffect(
            DamageRuleRole role,
            @Nullable LivingEntity owner
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.VANILLA_MOB_EFFECT,
                role,
                owner,
                ItemStack.EMPTY,
                null,
                owner
        );
    }

    public static RuleExecutionContext datapackRule(DamageRuleRole role) {
        return new RuleExecutionContext(
                DamageRuleProviderType.DATAPACK_RULE,
                role,
                null,
                ItemStack.EMPTY,
                null,
                null
        );
    }

    public static RuleExecutionContext javaApiRule(DamageRuleRole role) {
        return new RuleExecutionContext(
                DamageRuleProviderType.JAVA_API,
                role,
                null,
                ItemStack.EMPTY,
                null,
                null
        );
    }
}