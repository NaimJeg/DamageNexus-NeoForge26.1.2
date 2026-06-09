package io.github.naimjeg.damagenexus.diagnostics.logging;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.core.trace.DamageNexusTransaction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public final class TransactionDiagnosticsLog {

    private static final Logger LOGGER = LogUtils.getLogger();

    private TransactionDiagnosticsLog() {
    }

    public static void drop(
            String reason,
            DamageNexusTransaction tx,
            DamageSource wantedSource,
            float eventInflictedDamage
    ) {
        DamageNexusLogSink.info(
                DamageNexusLogKind.TRACE_DETAIL,
                LOGGER,
                tx.attacker(),
                tx.victim(),
                "[DN-TX] DROP reason={} txId={} victim={} tx_source={} tx_final_event_amount={} tx_event_amount_after_set={} wanted_source={} event_inflicted_damage={} tx_game_time={}",
                reason,
                tx.damageId(),
                tx.victim().getName().getString(),
                sourceId(tx.source()),
                tx.finalEventAmount(),
                tx.eventAmountAfterSet(),
                sourceId(wantedSource),
                eventInflictedDamage,
                tx.gameTime()
        );
    }

    public static void lateAmountMatch(
            String label,
            DamageNexusTransaction tx,
            DamageSource wantedSource,
            float eventInflictedDamage,
            float diff,
            int staleDropped
    ) {
        DamageNexusLogSink.info(
                LOGGER,
                tx.attacker(),
                tx.victim(),
                "[DN-TX] {} txId={} victim={} tx_source={} tx_final_event_amount={} tx_event_amount_after_set={} wanted_source={} post_event_inflicted_damage={} diff={} invul_before={} stale_dropped={} tx_game_time={}",
                label,
                tx.damageId(),
                tx.victim().getName().getString(),
                sourceId(tx.source()),
                tx.finalEventAmount(),
                tx.eventAmountAfterSet(),
                sourceId(wantedSource),
                eventInflictedDamage,
                diff,
                tx.victimInvulnerableTimeBefore(),
                staleDropped,
                tx.gameTime()
        );
    }

    public static void ambiguousLateAmountMatch(
            String wantedSourceId,
            DamageNexusTransaction first,
            DamageNexusTransaction second
    ) {
        DamageNexusLogSink.warn(
                LOGGER,
                first.attacker(),
                first.victim(),
                "[DN-TX] AMBIGUOUS_LATE_AMOUNT_MATCH wanted_source={} first_tx={} first_amount={} first_after_set={} second_tx={} second_amount={} second_after_set={}",
                wantedSourceId,
                first.damageId(),
                first.finalEventAmount(),
                first.eventAmountAfterSet(),
                second.damageId(),
                second.finalEventAmount(),
                second.eventAmountAfterSet()
        );
    }

    public static void noMatch(
            LivingEntity victim,
            DamageSource source,
            float eventInflictedDamage
    ) {
        DamageNexusLogSink.info(
                LOGGER,
                null,
                victim,
                "[DN-TX] NO_MATCH victim={} wanted_source={} event_inflicted_damage={}",
                victim.getName().getString(),
                sourceId(source),
                eventInflictedDamage
        );
    }

    public static void candidateRecord(
            DamageNexusTransaction tx,
            int containerIdentity
    ) {
        DamageNexusLogSink.info(
                DamageNexusLogKind.TRACE_DETAIL,
                LOGGER,
                tx.attacker(),
                tx.victim(),
                "[DN-TX] CANDIDATE_RECORD id={} victim={} source={} gt={} final={} after_set={} container={}",
                tx.damageId(),
                tx.victim().getName().getString(),
                sourceId(tx.source()),
                tx.gameTime(),
                tx.finalEventAmount(),
                tx.eventAmountAfterSet(),
                containerIdentity
        );
    }

    public static void candidatePromote(
            DamageNexusTransaction tx,
            float preDamage,
            int containerIdentity
    ) {
        DamageNexusLogSink.info(
                DamageNexusLogKind.TRACE_SUMMARY,
                LOGGER,
                tx.attacker(),
                tx.victim(),
                "[DN-TX] CANDIDATE_PROMOTE id={} victim={} source={} pre_damage={} pre_health={} pre_absorption={} pre_invul={} gt={} container={}",
                tx.damageId(),
                tx.victim().getName().getString(),
                sourceId(tx.source()),
                preDamage,
                tx.victimHealthBefore(),
                tx.victimAbsorptionBefore(),
                tx.victimInvulnerableTimeBefore(),
                tx.gameTime(),
                containerIdentity
        );
    }

    public static void preWithoutCandidate(
            LivingEntity victim,
            DamageSource source,
            float preDamage,
            long gameTime,
            int containerIdentity
    ) {
        DamageNexusLogSink.warn(
                DamageNexusLogKind.WARNING,
                LOGGER,
                null,
                victim,
                "[DN-TX] PRE_WITHOUT_CANDIDATE victim={} source={} pre_damage={} gt={} container={}",
                victim.getName().getString(),
                sourceId(source),
                preDamage,
                gameTime,
                containerIdentity
        );
    }

    public static void candidatePrune(
            int removed,
            int remaining,
            long now
    ) {
        if (removed <= 0) {
            return;
        }

        DamageNexusLogSink.info(
                DamageNexusLogKind.TRACE_DETAIL,
                LOGGER,
                null,
                null,
                "[DN-TX] CANDIDATE_PRUNE removed={} remaining={} gt={}",
                removed,
                remaining,
                now
        );
    }

    private static String sourceId(DamageSource source) {
        if (source == null) {
            return "null";
        }

        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElse(source.type().msgId());
    }
}

