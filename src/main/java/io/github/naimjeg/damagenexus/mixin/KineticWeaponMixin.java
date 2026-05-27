package io.github.naimjeg.damagenexus.mixin;

import io.github.naimjeg.damagenexus.bridge.vanilla.SpearDamageCapture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KineticWeapon.class)
public abstract class KineticWeaponMixin {

    @Redirect(
            method = "damageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;stabAttack(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/Entity;FZZZ)Z"
            )
    )
    private boolean damagenexus$captureKineticSpearAttack(
            LivingEntity attacker,
            EquipmentSlot slot,
            Entity target,
            float baseDamage,
            boolean dealsDamage,
            boolean dealsKnockback,
            boolean dismounts,

            ItemStack stack,
            int ticksRemaining,
            LivingEntity livingEntity,
            EquipmentSlot equipmentSlot
    ) {
        SpearDamageCapture.captureCharge(
                attacker,
                target,
                stack,
                slot,
                baseDamage,
                dealsDamage,
                dealsKnockback,
                dismounts
        );

        try {
            return attacker.stabAttack(
                    slot,
                    target,
                    baseDamage,
                    dealsDamage,
                    dealsKnockback,
                    dismounts
            );
        } finally {
            SpearDamageCapture.clear();
        }
    }
}