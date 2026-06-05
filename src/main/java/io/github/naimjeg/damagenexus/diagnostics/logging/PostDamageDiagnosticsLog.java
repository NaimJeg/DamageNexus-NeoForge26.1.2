package io.github.naimjeg.damagenexus.diagnostics.logging;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.core.trace.DamageNexusTransaction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slf4j.Logger;

public final class PostDamageDiagnosticsLog {

    private static final Logger LOGGER = LogUtils.getLogger();

    private PostDamageDiagnosticsLog() {
    }

    public static void unmatched(
            LivingEntity victim,
            DamageSource source,
            float eventNewDamage
    ) {
        DamageNexusLogSink.info(
                DamageNexusLogKind.COMPATIBILITY,
                LOGGER,
                null,
                victim,
                "[DN-POST] unmatched victim={} source={} event_new_damage={} health_after={} absorption_after={} invul_after={}",
                victim.getName().getString(),
                sourceId(source),
                eventNewDamage,
                victim.getHealth(),
                victim.getAbsorptionAmount(),
                victim.invulnerableTime
        );
    }

    public static void observed(
            LivingDamageEvent.Post event,
            DamageNexusTransaction tx,
            LivingEntity victim,
            float healthAfter,
            float absorptionAfter,
            float healthDeltaDamage,
            float absorptionDeltaDamage,
            float observedTotalDelta
    ) {
        DamageNexusLogSink.info(
                DamageNexusLogKind.TRACE_SUMMARY,
                LOGGER,
                tx.attacker(),
                tx.victim(),
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
    }

    public static void adjusted(
            Object kind,
            LivingDamageEvent.Post event,
            DamageNexusTransaction tx,
            float observedTotalDelta,
            float observedDiff,
            float eventAmountDiff
    ) {
        DamageNexusLogSink.info(
                DamageNexusLogKind.COMPATIBILITY,
                LOGGER,
                tx.attacker(),
                tx.victim(),
                "[DN#{}] POST_ADJUSTED kind={} final_event_amount={} event_amount_before_set={} event_amount_after_set={} event_new_damage={} observed_total_delta={} observed_diff={} event_amount_diff={} health_before={} absorption_before={} invul_before={} invul_after={}",
                tx.damageId(),
                kind,
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
                event.getEntity().invulnerableTime
        );
    }

    public static void mismatch(
            Object kind,
            LivingDamageEvent.Post event,
            DamageNexusTransaction tx,
            float observedTotalDelta,
            float observedDiff,
            float eventAmountDiff
    ) {
        DamageNexusLogSink.warn(
                DamageNexusLogKind.WARNING,
                LOGGER,
                tx.attacker(),
                tx.victim(),
                "[DN#{}] POST_MISMATCH kind={} final_event_amount={} event_amount_before_set={} event_amount_after_set={} event_new_damage={} observed_total_delta={} observed_diff={} event_amount_diff={} health_before={} absorption_before={} invul_before={} invul_after={}",
                tx.damageId(),
                kind,
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
                event.getEntity().invulnerableTime
        );
    }

    private static String sourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }
}

