package io.github.naimjeg.damagenexus.api.rule;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record RuleExecutionContext(
        DamageRuleProviderType providerType,
        RuleSourceLocation sourceLocation,
        DamageRuleRole role,
        @Nullable LivingEntity owner,
        ItemStack sourceStack,
        @Nullable EquipmentSlot equipmentSlot,
        @Nullable Entity sourceEntity
) {
    public static RuleExecutionContext itemEquipment(
            RuleSourceLocation sourceLocation,
            DamageRuleRole role,
            @Nullable LivingEntity owner,
            ItemStack stack,
            @Nullable EquipmentSlot slot
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.ITEM_EQUIPMENT,
                sourceLocation,
                role,
                owner,
                stack == null ? ItemStack.EMPTY : stack,
                slot,
                owner
        );
    }

    public static RuleExecutionContext projectileSource(
            DamageRuleRole role,
            @Nullable LivingEntity owner,
            ItemStack stack,
            @Nullable Entity projectile
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.PROJECTILE_SOURCE,
                RuleSourceLocation.PROJECTILE,
                role,
                owner,
                stack == null ? ItemStack.EMPTY : stack,
                null,
                projectile
        );
    }

    public static RuleExecutionContext entitySource(
            RuleSourceLocation sourceLocation,
            DamageRuleRole role,
            @Nullable LivingEntity owner,
            @Nullable Entity sourceEntity
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.ENTITY,
                sourceLocation,
                role,
                owner,
                ItemStack.EMPTY,
                null,
                sourceEntity
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
                RuleSourceLocation.VANILLA,
                role,
                owner,
                stack == null ? ItemStack.EMPTY : stack,
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
                RuleSourceLocation.VANILLA,
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
                RuleSourceLocation.DATAPACK,
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
                RuleSourceLocation.JAVA_API,
                role,
                null,
                ItemStack.EMPTY,
                null,
                null
        );
    }
}

