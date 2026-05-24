package io.github.naimjeg.damagenexus.event.neoforge;

import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.core.trace.DamageNexusTransaction;
import io.github.naimjeg.damagenexus.core.trace.DamageNexusTransactionTracker;
import io.github.naimjeg.damagenexus.diagnostics.logging.PostDamageDiagnosticsLog;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = DamageNexus.MODID)
public final class PostDamageHandler {

    private enum PostMismatchKind {
        NONE,
        OVERKILL_CAP,
        EVENT_AMOUNT_SET_FAILED,
        LATE_AMOUNT_CHANGED,
        LATE_ZERO_DAMAGE,
        PARTIAL_OBSERVED_DELTA,
        BATCHED_OBSERVED_DELTA,
        OBSERVED_DELTA_EXCEEDS_EVENT_AMOUNT,
        POST_ATTACK_COOLDOWN_DELTA,
        UNKNOWN
    }

    private static final float EPSILON = 0.001f;

    private PostDamageHandler() {}

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!DamageNexusTransactionTracker.enabled()) {
            return;
        }

        LivingEntity victim = event.getEntity();

        DamageNexusTransaction tx =
                DamageNexusTransactionTracker.pollMatchingPostTrackable(
                        victim,
                        event.getSource(),
                        event.getNewDamage()
                );

        if (tx == null) {
            if (ModConfig.postDamageDiagnosticsEnabled()) {
                PostDamageDiagnosticsLog.unmatched(
                        victim,
                        event.getSource(),
                        event.getNewDamage()
                );
            }
            return;
        }

        float healthAfter = victim.getHealth();
        float absorptionAfter = victim.getAbsorptionAmount();

        float healthDeltaDamage =
                Math.max(0.0f, tx.victimHealthBefore() - healthAfter);

        float absorptionDeltaDamage =
                Math.max(0.0f, tx.victimAbsorptionBefore() - absorptionAfter);

        float observedTotalDelta = healthDeltaDamage + absorptionDeltaDamage;

        if (!ModConfig.postDamageDiagnosticsEnabled()) {
            return;
        }

        PostDamageDiagnosticsLog.observed(
                event,
                tx,
                victim,
                healthAfter,
                absorptionAfter,
                healthDeltaDamage,
                absorptionDeltaDamage,
                observedTotalDelta
        );

        PostMismatchKind mismatchKind =
                classifyPostMismatch(event, tx, observedTotalDelta);

        if (mismatchKind == PostMismatchKind.NONE) {
            return;
        }

        float observedDiff =
                Math.abs(tx.finalEventAmount() - observedTotalDelta);

        float eventAmountDiff =
                Math.abs(tx.eventAmountAfterSet() - event.getNewDamage());

        if (isExpectedPostAdjustment(mismatchKind)) {
            PostDamageDiagnosticsLog.adjusted(
                    mismatchKind,
                    event,
                    tx,
                    observedTotalDelta,
                    observedDiff,
                    eventAmountDiff
            );
            return;
        }

        PostDamageDiagnosticsLog.mismatch(
                mismatchKind,
                event,
                tx,
                observedTotalDelta,
                observedDiff,
                eventAmountDiff
        );
    }

    private static PostMismatchKind classifyPostMismatch(
            LivingDamageEvent.Post event,
            DamageNexusTransaction tx,
            float observedTotalDelta
    ) {
        float expectedFinal = tx.finalEventAmount();
        float eventAmountAfterSet = tx.eventAmountAfterSet();
        float eventNewDamage = event.getNewDamage();

        if (amountClose(expectedFinal, observedTotalDelta)
                && amountClose(eventAmountAfterSet, eventNewDamage)) {
            return PostMismatchKind.NONE;
        }

        if (isOverkillCap(tx, observedTotalDelta)) {
            return PostMismatchKind.OVERKILL_CAP;
        }

        /*
         * Vanilla hurt-resistance / invulnerability can reduce the post event's
         * effective new damage after DamageNexus has already set the event amount.
         *
         * This is not a DamageNexus calculation failure.
         */
        if (isVanillaInvulnerabilityAdjustment(event, tx, observedTotalDelta)) {
            return PostMismatchKind.POST_ATTACK_COOLDOWN_DELTA;
        }

        if (!amountClose(eventAmountAfterSet, eventNewDamage)) {
            if (eventNewDamage <= EPSILON && eventAmountAfterSet > EPSILON) {
                return PostMismatchKind.LATE_ZERO_DAMAGE;
            }

            return PostMismatchKind.LATE_AMOUNT_CHANGED;
        }

        if (observedTotalDelta + EPSILON < expectedFinal) {
            return PostMismatchKind.PARTIAL_OBSERVED_DELTA;
        }

        if (isBatchedObservedDelta(event, tx, observedTotalDelta)) {
            return PostMismatchKind.BATCHED_OBSERVED_DELTA;
        }

        if (observedTotalDelta > expectedFinal + EPSILON) {
            return PostMismatchKind.OBSERVED_DELTA_EXCEEDS_EVENT_AMOUNT;
        }

        return PostMismatchKind.UNKNOWN;
    }

    private static boolean isVanillaInvulnerabilityAdjustment(
            LivingDamageEvent.Post event,
            DamageNexusTransaction tx,
            float observedTotalDelta
    ) {
        if (tx.victimInvulnerableTimeBefore() <= 0) {
            return false;
        }

        float eventNewDamage = event.getNewDamage();

        return eventNewDamage + EPSILON < tx.eventAmountAfterSet()
                && amountClose(eventNewDamage, observedTotalDelta);
    }

    private static boolean isOverkillCap(
            DamageNexusTransaction tx,
            float observedTotalDelta
    ) {
        float maxObservableDamage =
                tx.victimHealthBefore() + tx.victimAbsorptionBefore();

        return tx.finalEventAmount() > maxObservableDamage + EPSILON
                && amountClose(observedTotalDelta, maxObservableDamage);
    }

    private static boolean amountClose(float a, float b) {
        return Math.abs(a - b) <= EPSILON;
    }

    private static boolean isExpectedPostAdjustment(PostMismatchKind kind) {
        return kind == PostMismatchKind.OVERKILL_CAP
                || kind == PostMismatchKind.POST_ATTACK_COOLDOWN_DELTA
                || kind == PostMismatchKind.BATCHED_OBSERVED_DELTA;
    }

    private static boolean isBatchedObservedDelta(
            LivingDamageEvent.Post event,
            DamageNexusTransaction tx,
            float observedTotalDelta
    ) {
        return amountClose(tx.eventAmountAfterSet(), event.getNewDamage())
                && observedTotalDelta > tx.finalEventAmount() + EPSILON
                && tx.victimInvulnerableTimeBefore() > 0;
    }

    private static String sourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }
}
