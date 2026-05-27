package io.github.naimjeg.damagenexus.core.trace;

import com.mojang.logging.LogUtils;
import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.ModAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.util.Deque;
import java.util.Iterator;

public final class DamageNexusTransactionTracker {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_QUEUE_SIZE_PER_VICTIM = 8;

    private static final long MAX_TRANSACTION_AGE_TICKS = 40L;
    private static final long SAME_POST_MAX_AGE_TICKS = 2L;

    private static final float ABSOLUTE_AMOUNT_EPSILON = 0.001f;
    private static final float RELATIVE_AMOUNT_EPSILON = 0.0001f;

    private enum LateMatchKind {
        AMOUNT_CHANGED,
        VANILLA_INVULNERABILITY_ADJUSTED
    }

    private DamageNexusTransactionTracker() {}

    public static void record(DamageNexusContext.DamageNexusTransaction tx) {
        DamageTransactionQueue txQueue =
                tx.victim().getData(ModAttachments.DAMAGE_TRANSACTIONS);

        Deque<DamageNexusContext.DamageNexusTransaction> queue =
                txQueue.entries();

        queue.addLast(tx);

        while (queue.size() > MAX_QUEUE_SIZE_PER_VICTIM) {
            DamageNexusContext.DamageNexusTransaction dropped = queue.removeFirst();

            logDrop(
                    "queue_overflow",
                    dropped,
                    null,
                    Float.NaN
            );
        }
    }

    public static DamageNexusContext.DamageNexusTransaction pollMatching(
            LivingEntity victim,
            DamageSource source,
            float eventNewDamage
    ) {
        DamageTransactionQueue txQueue =
                victim.getData(ModAttachments.DAMAGE_TRANSACTIONS);

        Deque<DamageNexusContext.DamageNexusTransaction> queue =
                txQueue.entries();

        if (queue.isEmpty()) {
            return null;
        }

        Identifier wantedSourceId = sourceId(source);
        long now = victim.level().getGameTime();

        dropExpired(queue, now, source, eventNewDamage);

        if (queue.isEmpty()) {
            return null;
        }

        DamageNexusContext.DamageNexusTransaction exact =
                findExactAmountCandidate(
                        queue,
                        wantedSourceId,
                        eventNewDamage
                );

        if (exact != null) {
            removeUpToCandidate(
                    queue,
                    exact,
                    "stale_before_exact_match",
                    source,
                    eventNewDamage
            );

            return exact;
        }

        DamageNexusContext.DamageNexusTransaction lateAmount =
                findRecentSameSourceCandidate(
                        queue,
                        wantedSourceId,
                        now
                );

        if (lateAmount != null) {
            LateMatchKind lateMatchKind =
                    classifyLateAmountMatch(lateAmount, eventNewDamage);

            boolean logStaleDrops =
                    lateMatchKind != LateMatchKind.VANILLA_INVULNERABILITY_ADJUSTED;

            int staleDropped = removeUpToCandidate(
                    queue,
                    lateAmount,
                    "stale_before_late_amount_match",
                    source,
                    eventNewDamage,
                    logStaleDrops
            );

            logLateAmountMatch(
                    lateAmount,
                    source,
                    eventNewDamage,
                    lateMatchKind,
                    staleDropped
            );

            return lateAmount;
        }

        dropAll(
                queue,
                "no_matching_post_candidate",
                source,
                eventNewDamage
        );

        logNoMatch(
                victim,
                source,
                eventNewDamage
        );

        return null;
    }

//    public static void clear(LivingEntity victim) {
//        victim.getData(ModAttachments.DAMAGE_TRANSACTIONS.get())
//                .clear();
//    }

    private static DamageNexusContext.DamageNexusTransaction findExactAmountCandidate(
            Deque<DamageNexusContext.DamageNexusTransaction> queue,
            Identifier wantedSourceId,
            float eventNewDamage
    ) {
        for (DamageNexusContext.DamageNexusTransaction tx : queue) {
            if (!sourceId(tx.source()).equals(wantedSourceId)) {
                continue;
            }

            if (amountClose(tx.eventAmountAfterSet(), eventNewDamage)) {
                return tx;
            }
        }

        return null;
    }

    private static DamageNexusContext.DamageNexusTransaction findRecentSameSourceCandidate(
            Deque<DamageNexusContext.DamageNexusTransaction> queue,
            Identifier wantedSourceId,
            long now
    ) {
        DamageNexusContext.DamageNexusTransaction candidate = null;

        for (DamageNexusContext.DamageNexusTransaction tx : queue) {
            if (!sourceId(tx.source()).equals(wantedSourceId)) {
                continue;
            }

            if (now - tx.gameTime() > SAME_POST_MAX_AGE_TICKS) {
                continue;
            }

            if (candidate != null) {
                logAmbiguousLateAmountMatch(
                        candidate,
                        tx,
                        wantedSourceId
                );

                return null;
            }

            candidate = tx;
        }

        return candidate;
    }

    private static int removeUpToCandidate(
            Deque<DamageNexusContext.DamageNexusTransaction> queue,
            DamageNexusContext.DamageNexusTransaction candidate,
            String staleReason,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        return removeUpToCandidate(
                queue,
                candidate,
                staleReason,
                wantedSource,
                eventNewDamage,
                true
        );
    }

    private static int removeUpToCandidate(
            Deque<DamageNexusContext.DamageNexusTransaction> queue,
            DamageNexusContext.DamageNexusTransaction candidate,
            String staleReason,
            DamageSource wantedSource,
            float eventNewDamage,
            boolean logStaleDrops
    ) {
        int staleDropped = 0;

        Iterator<DamageNexusContext.DamageNexusTransaction> iterator =
                queue.iterator();

        while (iterator.hasNext()) {
            DamageNexusContext.DamageNexusTransaction tx = iterator.next();
            iterator.remove();

            if (tx == candidate) {
                return staleDropped;
            }

            staleDropped++;

            if (logStaleDrops) {
                logDrop(
                        staleReason,
                        tx,
                        wantedSource,
                        eventNewDamage
                );
            }
        }

        return staleDropped;
    }

    private static void dropExpired(
            Deque<DamageNexusContext.DamageNexusTransaction> queue,
            long now,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        Iterator<DamageNexusContext.DamageNexusTransaction> iterator =
                queue.iterator();

        while (iterator.hasNext()) {
            DamageNexusContext.DamageNexusTransaction tx = iterator.next();

            if (now - tx.gameTime() <= MAX_TRANSACTION_AGE_TICKS) {
                continue;
            }

            iterator.remove();

            logDrop(
                    "expired",
                    tx,
                    wantedSource,
                    eventNewDamage
            );
        }
    }

    private static void dropAll(
            Deque<DamageNexusContext.DamageNexusTransaction> queue,
            String reason,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        while (!queue.isEmpty()) {
            DamageNexusContext.DamageNexusTransaction tx = queue.removeFirst();

            logDrop(
                    reason,
                    tx,
                    wantedSource,
                    eventNewDamage
            );
        }
    }

    private static boolean amountClose(float a, float b) {
        float diff = Math.abs(a - b);

        if (diff <= ABSOLUTE_AMOUNT_EPSILON) {
            return true;
        }

        float scale = Math.max(Math.abs(a), Math.abs(b));

        return diff <= scale * RELATIVE_AMOUNT_EPSILON;
    }

    private static Identifier sourceId(DamageSource source) {
        return source.typeHolder()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> Identifier.fromNamespaceAndPath(
                        "unknown",
                        sanitizePath(source.type().msgId())
                ));
    }

    private static String sanitizePath(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9_./-]", "_");
    }

    private static void logDrop(
            String reason,
            DamageNexusContext.DamageNexusTransaction tx,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        LOGGER.info(
                "[DN-TX] DROP reason={} txId={} victim={} tx_source={} tx_final_event_amount={} tx_event_amount_after_set={} wanted_source={} event_new_damage={} tx_game_time={}",
                reason,
                tx.damageId(),
                tx.victim().getName().getString(),
                sourceId(tx.source()),
                tx.finalEventAmount(),
                tx.eventAmountAfterSet(),
                wantedSource != null ? sourceId(wantedSource) : "null",
                eventNewDamage,
                tx.gameTime()
        );
    }

    private static void logLateAmountMatch(
            DamageNexusContext.DamageNexusTransaction tx,
            DamageSource wantedSource,
            float eventNewDamage,
            LateMatchKind kind,
            int staleDropped
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        String label =
                kind == LateMatchKind.VANILLA_INVULNERABILITY_ADJUSTED
                        ? "LATE_VANILLA_ADJUSTED_MATCH"
                        : "LATE_AMOUNT_MATCH";

        LOGGER.info(
                "[DN-TX] {} txId={} victim={} tx_source={} tx_final_event_amount={} tx_event_amount_after_set={} wanted_source={} post_event_new_damage={} diff={} invul_before={} stale_dropped={} tx_game_time={}",
                label,
                tx.damageId(),
                tx.victim().getName().getString(),
                sourceId(tx.source()),
                tx.finalEventAmount(),
                tx.eventAmountAfterSet(),
                sourceId(wantedSource),
                eventNewDamage,
                Math.abs(tx.eventAmountAfterSet() - eventNewDamage),
                tx.victimInvulnerableTimeBefore(),
                staleDropped,
                tx.gameTime()
        );
    }

    private static void logAmbiguousLateAmountMatch(
            DamageNexusContext.DamageNexusTransaction first,
            DamageNexusContext.DamageNexusTransaction second,
            Identifier wantedSourceId
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        LOGGER.warn(
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

    private static void logNoMatch(
            LivingEntity victim,
            DamageSource source,
            float eventNewDamage
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        LOGGER.info(
                "[DN-TX] NO_MATCH victim={} wanted_source={} event_new_damage={}",
                victim.getName().getString(),
                sourceId(source),
                eventNewDamage
        );
    }

    private static LateMatchKind classifyLateAmountMatch(
            DamageNexusContext.DamageNexusTransaction tx,
            float eventNewDamage
    ) {
        if (isLikelyVanillaInvulnerabilityAdjustment(tx, eventNewDamage)) {
            return LateMatchKind.VANILLA_INVULNERABILITY_ADJUSTED;
        }

        return LateMatchKind.AMOUNT_CHANGED;
    }

    private static boolean isLikelyVanillaInvulnerabilityAdjustment(
            DamageNexusContext.DamageNexusTransaction tx,
            float eventNewDamage
    ) {
        return tx.victimInvulnerableTimeBefore() > 0
                && eventNewDamage + ABSOLUTE_AMOUNT_EPSILON < tx.eventAmountAfterSet();
    }
}