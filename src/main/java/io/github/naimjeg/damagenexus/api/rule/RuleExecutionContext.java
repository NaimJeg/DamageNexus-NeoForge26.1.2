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
    public static RuleExecutionContext weaponAffix(
            LivingEntity owner,
            ItemStack stack,
            EquipmentSlot slot
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.WEAPON_AFFIX,
                DamageRuleRole.OFFENSIVE,
                owner,
                stack,
                slot,
                owner
        );
    }

    public static RuleExecutionContext armorAffix(
            LivingEntity owner,
            ItemStack stack,
            EquipmentSlot slot
    ) {
        return new RuleExecutionContext(
                DamageRuleProviderType.ARMOR_AFFIX,
                DamageRuleRole.DEFENSIVE,
                owner,
                stack,
                slot,
                owner
        );
    }

    public static RuleExecutionContext vanillaEnchantment(
            DamageRuleRole role,
            LivingEntity owner,
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
}