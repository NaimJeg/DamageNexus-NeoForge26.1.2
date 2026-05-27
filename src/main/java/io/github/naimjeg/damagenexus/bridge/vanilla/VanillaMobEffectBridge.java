package io.github.naimjeg.damagenexus.bridge.vanilla;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class VanillaMobEffectBridge {

    private static final float STRENGTH_ATTACK_DAMAGE_PER_LEVEL = 3.0f;
    private static final float WEAKNESS_ATTACK_DAMAGE_PER_LEVEL = -4.0f;

    private VanillaMobEffectBridge() {}

    public static float computeOffensiveBaseDelta(
            @Nullable Entity attacker,
            DamageSource source
    ) {
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return 0.0f;
        }

        /*
         * Strength / Weakness are attack-damage attribute modifiers.
         * They should only be reconstructed for direct living attacks.
         *
         * This intentionally excludes arrows/projectiles:
         * source.getEntity() may be the shooter,
         * but source.getDirectEntity() is the projectile.
         */
        if (!isDirectLivingAttack(attacker, source)) {
            return 0.0f;
        }

        float delta = 0.0f;

        MobEffectInstance strength =
                livingAttacker.getEffect(MobEffects.STRENGTH);

        if (strength != null) {
            int level = strength.getAmplifier() + 1;
            delta += STRENGTH_ATTACK_DAMAGE_PER_LEVEL * level;
        }

        MobEffectInstance weakness =
                livingAttacker.getEffect(MobEffects.WEAKNESS);

        if (weakness != null) {
            int level = weakness.getAmplifier() + 1;
            delta += WEAKNESS_ATTACK_DAMAGE_PER_LEVEL * level;
        }

        if (!Float.isFinite(delta)) {
            return 0.0f;
        }

        return delta;
    }

    private static boolean isDirectLivingAttack(
            Entity attacker,
            DamageSource source
    ) {
        return source.getEntity() == attacker
                && source.getDirectEntity() == attacker;
    }
}