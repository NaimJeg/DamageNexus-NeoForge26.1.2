package io.github.naimjeg.damagenexus.event.neoforge;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.trace.DamageNexusTransactionTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = DamageNexus.MODID)
public final class PostDamageHandler {

    private enum PostMismatchKind {
        NONE,
        OVERKILL_CAP,
        EVENT_AMOUNT_SET_FAILED,
        LATE_AMOUNT_CHANGED,
        LATE_ZERO_DAMAGE,
        PARTIAL_OBSERVED_DELTA,
        OBSERVED_DELTA_EXCEEDS_EVENT_AMOUNT,
        VANILLA_INVULNERABILITY_DELTA,
        UNKNOWN
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float EPSILON = 0.001f;

    private PostDamageHandler() {}

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();

        DamageNexusContext.DamageNexusTransaction tx =
                DamageNexusTransactionTracker.pollMatching(
                        victim,
                        event.getSource(),
                        event.getNewDamage()
                );

        if (tx == null) {
            if (ModConfig.isDebugMode()) {
                LOGGER.info(
                        "[DN-POST] unmatched victim={} source={} event_new_damage={} health_after={} absorption_after={} invul_after={}",
                        victim.getName().getString(),
                        sourceId(event.getSource()),
                        event.getNewDamage(),
                        victim.getHealth(),
                        victim.getAbsorptionAmount(),
                        victim.invulnerableTime
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

        if (!ModConfig.isDebugMode()) {
            return;
        }

        LOGGER.info(
                "[DN#{}] POST victim={} final_event_amount={} event_amount_before_set={} event_amount_after_set={} event_new_damage={} health_before={} health_after={} health_delta_damage={} absorption_before={} absorption_after={} absorption_delta_damage={} observed_total_delta={} invul_before={} invul_after={}",
                tx.damageId(),
                victim.getName().getString(),
                tx.finalEventAmount(),
                tx.eventAmountBeforeSet(),
                tx.eventAmountAfterSet(),
                event.getNewDamage(),
                tx.victimHealthBefore(),
                healthAfter,
                healthDeltaDamage,
                tx.victimAbsorptionBefore(),
                absorptionAfter,
                absorptionDeltaDamage,
                observedTotalDelta,
                tx.victimInvulnerableTimeBefore(),
                victim.invulnerableTime
        );

        PostMismatchKind mismatchKind =
                classifyPostMismatch(event, tx, observedTotalDelta);

        if (mismatchKind == PostMismatchKind.NONE) {
            return;
        }

        float observedDiff =
                Math.abs(tx.finalEventAmount() - observedTotalDelta);

        float eventAmountDiff =
                Math.abs(tx.finalEventAmount() - event.getNewDamage());

        String message =
                "[DN#{}] POST_MISMATCH kind={} final_event_amount={} event_amount_before_set={} event_amount_after_set={} event_new_damage={} observed_total_delta={} observed_diff={} event_amount_diff={} health_before={} absorption_before={} invul_before={} invul_after={}";

        if (isExpectedPostMismatch(mismatchKind)) {
            LOGGER.info(
                    message,
                    tx.damageId(),
                    mismatchKind,
                    tx.finalEventAmount(),
                    tx.eventAmountBeforeSet(),
                    tx.eventAmountAfterSet(),
                    event.getNewDamage(),
                    observedTotalDelta,
                    observedDiff,
                    eventAmountDiff,
                    tx.victimHealthBefore(),
                    tx.victimAbsorptionBefore(),
                    tx.victimInvulnerableTimeBefore(),
                    victim.invulnerableTime
            );
        } else {
            LOGGER.warn(
                    message,
                    tx.damageId(),
                    mismatchKind,
                    tx.finalEventAmount(),
                    tx.eventAmountBeforeSet(),
                    tx.eventAmountAfterSet(),
                    event.getNewDamage(),
                    observedTotalDelta,
                    observedDiff,
                    eventAmountDiff,
                    tx.victimHealthBefore(),
                    tx.victimAbsorptionBefore(),
                    tx.victimInvulnerableTimeBefore(),
                    victim.invulnerableTime
            );
        }
    }

    private static PostMismatchKind classifyPostMismatch(
            LivingDamageEvent.Post event,
            DamageNexusContext.DamageNexusTransaction tx,
            float observedTotalDelta
    ) {
        float finalEventAmount = tx.finalEventAmount();
        float eventAmountAfterSet = tx.eventAmountAfterSet();
        float eventNewDamage = event.getNewDamage();

        boolean observedMatchesFinal =
                Math.abs(finalEventAmount - observedTotalDelta) <= EPSILON;

        boolean eventAmountSetFailed =
                Math.abs(finalEventAmount - eventAmountAfterSet) > EPSILON;

        boolean postAmountChangedAfterSet =
                Math.abs(eventAmountAfterSet - eventNewDamage) > EPSILON;

        if (observedMatchesFinal && !eventAmountSetFailed && !postAmountChangedAfterSet) {
            return PostMismatchKind.NONE;
        }

        float availableBefore =
                Math.max(0.0f, tx.victimHealthBefore())
                        + Math.max(0.0f, tx.victimAbsorptionBefore());

        if (finalEventAmount > availableBefore
                && Math.abs(observedTotalDelta - availableBefore) <= EPSILON) {
            return PostMismatchKind.OVERKILL_CAP;
        }

        if (eventNewDamage <= EPSILON
                && observedTotalDelta <= EPSILON) {
            return PostMismatchKind.LATE_ZERO_DAMAGE;
        }

        if (eventAmountSetFailed) {
            return PostMismatchKind.EVENT_AMOUNT_SET_FAILED;
        }

        float amountLostAfterSet =
                tx.eventAmountAfterSet() - event.getNewDamage();

        if (amountLostAfterSet > EPSILON
                && tx.victimInvulnerableTimeBefore() > 0
                && Math.abs(observedTotalDelta - event.getNewDamage()) <= EPSILON) {
            return PostMismatchKind.VANILLA_INVULNERABILITY_DELTA;
        }

        if (postAmountChangedAfterSet) {
            return PostMismatchKind.LATE_AMOUNT_CHANGED;
        }

        if (observedTotalDelta > finalEventAmount + EPSILON) {
            return PostMismatchKind.OBSERVED_DELTA_EXCEEDS_EVENT_AMOUNT;
        }

        if (observedTotalDelta > EPSILON
                && observedTotalDelta < finalEventAmount) {
            return PostMismatchKind.PARTIAL_OBSERVED_DELTA;
        }

        return PostMismatchKind.UNKNOWN;
    }

    private static boolean isExpectedPostMismatch(PostMismatchKind kind) {
        return switch (kind) {
            case OVERKILL_CAP,
                 VANILLA_INVULNERABILITY_DELTA,
                 LATE_ZERO_DAMAGE -> true;

            case EVENT_AMOUNT_SET_FAILED,
                 LATE_AMOUNT_CHANGED,
                 PARTIAL_OBSERVED_DELTA,
                 OBSERVED_DELTA_EXCEEDS_EVENT_AMOUNT,
                 UNKNOWN -> false;

            case NONE -> true;
        };
    }

    private static String sourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }
}