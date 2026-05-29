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
    private static final float EPSILON = 0.0001f;

    private VanillaMobEffectBridge() {}

    public static OffensiveMobEffectBreakdown computeOffensiveBreakdown(
            VanillaDamageSourceProfile profile
    ) {
        LivingEntity livingAttacker = profile.livingAttacker();

        if (livingAttacker == null) {
            return OffensiveMobEffectBreakdown.NONE;
        }

        if (!profile.shouldApplyMeleeOffensiveMobEffects()) {
            return OffensiveMobEffectBreakdown.NONE;
        }

        return computeBreakdownForLivingAttacker(livingAttacker);
    }

    /**
     * Compatibility method for older callers.
     *
     * Prefer computeOffensiveBreakdown(VanillaDamageSourceProfile).
     *
     * Important:
     * This compatibility path is intentionally conservative. It only allows
     * vanilla direct melee-like attack sources. In particular, minecraft:spear
     * must not receive Strength / Weakness melee reconstruction here.
     */
    @Deprecated
    public static float computeOffensiveBaseDelta(
            @Nullable Entity attacker,
            DamageSource source
    ) {
        return computeOffensiveBreakdown(attacker, source).enabledDelta();
    }

    /**
     * Legacy compatibility overload.
     *
     * Prefer computeOffensiveBreakdown(VanillaDamageSourceProfile).
     */
    @Deprecated
    public static OffensiveMobEffectBreakdown computeOffensiveBreakdown(
            @Nullable Entity attacker,
            DamageSource source
    ) {
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return OffensiveMobEffectBreakdown.NONE;
        }

        if (!isDirectLivingAttack(livingAttacker, source)) {
            return OffensiveMobEffectBreakdown.NONE;
        }

        if (!isVanillaMeleeAttackSource(source)) {
            return OffensiveMobEffectBreakdown.NONE;
        }

        return computeBreakdownForLivingAttacker(livingAttacker);
    }

    private static OffensiveMobEffectBreakdown computeBreakdownForLivingAttacker(
            LivingEntity livingAttacker
    ) {
        float strengthDelta = computeStrengthDelta(livingAttacker);
        float weaknessDelta = computeWeaknessDelta(livingAttacker);

        float observedDelta = strengthDelta + weaknessDelta;

        float enabledDelta = observedDelta;

        /*
         * Strength and Weakness are both enabled.
         *
         * Negative deltas are valid here. The bridge plan removes the observed
         * vanilla mob-effect delta from the reconstructed base, then re-adds the
         * enabled delta into VANILLA_MELEE_BASE.
         */
        return new OffensiveMobEffectBreakdown(
                strengthDelta,
                weaknessDelta,
                observedDelta,
                enabledDelta
        );
    }

    private static float computeStrengthDelta(LivingEntity attacker) {
        MobEffectInstance strength = attacker.getEffect(MobEffects.STRENGTH);

        if (strength == null) {
            return 0.0f;
        }

        int level = strength.getAmplifier() + 1;
        return STRENGTH_ATTACK_DAMAGE_PER_LEVEL * level;
    }

    private static float computeWeaknessDelta(LivingEntity attacker) {
        MobEffectInstance weakness = attacker.getEffect(MobEffects.WEAKNESS);

        if (weakness == null) {
            return 0.0f;
        }

        int level = weakness.getAmplifier() + 1;
        return WEAKNESS_ATTACK_DAMAGE_PER_LEVEL * level;
    }

    private static boolean isDirectLivingAttack(
            LivingEntity attacker,
            DamageSource source
    ) {
        return source.getEntity() == attacker
                && source.getDirectEntity() == attacker;
    }

    private static boolean isVanillaMeleeAttackSource(DamageSource source) {
        String msgId = source.type().msgId();

        /*
         * NeoForge / Mojang mappings commonly expose player_attack as msgId "player".
         * Keep both names to avoid mapping/version drift.
         *
         * Do not include:
         * - "spear"
         * - "arrow"
         * - "trident"
         * - projectile / thrown / indirect sources
         */
        return "player".equals(msgId)
                || "player_attack".equals(msgId)
                || "mob".equals(msgId)
                || "mob_attack".equals(msgId)
                || "mob_attack_no_aggro".equals(msgId);
    }

    public record OffensiveMobEffectBreakdown(
            float strengthDelta,
            float weaknessDelta,
            float observedDelta,
            float enabledDelta
    ) {
        public static final OffensiveMobEffectBreakdown NONE =
                new OffensiveMobEffectBreakdown(
                        0.0f,
                        0.0f,
                        0.0f,
                        0.0f
                );

        public boolean hasEnabledDelta() {
            return Math.abs(enabledDelta) > EPSILON;
        }

        public boolean hasObservedDelta() {
            return Math.abs(observedDelta) > EPSILON;
        }

        public boolean hasObservedWeaknessDelta() {
            return Math.abs(weaknessDelta) > EPSILON;
        }
    }
}