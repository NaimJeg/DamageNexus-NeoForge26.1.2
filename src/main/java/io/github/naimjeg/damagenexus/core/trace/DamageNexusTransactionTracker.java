package io.github.naimjeg.damagenexus.core.trace;

import io.github.naimjeg.damagenexus.ModConfig;
import io.github.naimjeg.damagenexus.diagnostics.logging.TransactionDiagnosticsLog;
import io.github.naimjeg.damagenexus.registry.ModAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.*;

public final class DamageNexusTransactionTracker {

    private static final Map<DamageContainer, DamageNexusTransaction>
            INCOMING_CANDIDATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static final int MAX_QUEUE_SIZE_PER_VICTIM = 64;

    private static final long MAX_TRANSACTION_AGE_TICKS = 40L;
    private static final long SAME_POST_MAX_AGE_TICKS = 2L;

    private static final float ABSOLUTE_AMOUNT_EPSILON = 0.001f;
    private static final float RELATIVE_AMOUNT_EPSILON = 0.0001f;

    private enum LateMatchKind {
        AMOUNT_CHANGED,
        VANILLA_INVULNERABILITY_ADJUSTED
    }

    private DamageNexusTransactionTracker() {}

    public static void recordIncomingCandidate(
            DamageContainer container,
            DamageNexusTransaction tx
    ) {
        if (!enabled() || container == null || tx == null) {
            return;
        }

        if (!isTrackableAmount(tx.finalEventAmount())) {
            return;
        }

        pruneIncomingCandidates(tx.gameTime());

        synchronized (INCOMING_CANDIDATES) {
            INCOMING_CANDIDATES.put(container, tx);
        }

        logCandidateRecorded(container, tx);
    }

    public static DamageNexusTransaction pollMatchingPostTrackable(
            LivingEntity victim,
            DamageSource source,
            float eventNewDamage
    ) {
        if (!enabled()) {
            return null;
        }

        DamageTransactionQueue txQueue =
                victim.getData(ModAttachments.DAMAGE_TRANSACTIONS);

        Deque<DamageNexusTransaction> queue =
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

        DamageNexusTransaction exact =
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

        DamageNexusTransaction lateAmount =
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

            removeUpToCandidate(
                    queue,
                    lateAmount,
                    logStaleDrops ? "stale_before_late_match" : null,
                    source,
                    eventNewDamage
            );

            return lateAmount;
        }

        return null;
    }

    private static DamageNexusTransaction findExactAmountCandidate(
            Deque<DamageNexusTransaction> queue,
            Identifier wantedSourceId,
            float eventNewDamage
    ) {
        DamageNexusTransaction first = null;
        DamageNexusTransaction last = null;
        int exactCount = 0;

        for (DamageNexusTransaction tx : queue) {
            if (!sourceId(tx.source()).equals(wantedSourceId)) {
                continue;
            }

            if (amountClose(tx.eventAmountAfterSet(), eventNewDamage)) {
                exactCount++;

                if (first == null) {
                    first = tx;
                }

                last = tx;
            }
        }
        return last;
    }

    private static DamageNexusTransaction findRecentSameSourceCandidate(
            Deque<DamageNexusTransaction> queue,
            Identifier wantedSourceId,
            long now
    ) {
        DamageNexusTransaction candidate = null;

        for (DamageNexusTransaction tx : queue) {
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
            Deque<DamageNexusTransaction> queue,
            DamageNexusTransaction candidate,
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
            Deque<DamageNexusTransaction> queue,
            DamageNexusTransaction candidate,
            String staleReason,
            DamageSource wantedSource,
            float eventNewDamage,
            boolean logStaleDrops
    ) {
        int staleDropped = 0;

        Iterator<DamageNexusTransaction> iterator =
                queue.iterator();

        while (iterator.hasNext()) {
            DamageNexusTransaction tx = iterator.next();
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
            Deque<DamageNexusTransaction> queue,
            long now,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        Iterator<DamageNexusTransaction> iterator =
                queue.iterator();

        while (iterator.hasNext()) {
            DamageNexusTransaction tx = iterator.next();

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
            Deque<DamageNexusTransaction> queue,
            String reason,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        while (!queue.isEmpty()) {
            DamageNexusTransaction tx = queue.removeFirst();

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
            DamageNexusTransaction tx,
            DamageSource wantedSource,
            float eventNewDamage
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        TransactionDiagnosticsLog.drop(
                reason,
                tx,
                wantedSource,
                eventNewDamage
        );
    }

    private static void logLateAmountMatch(
            DamageNexusTransaction tx,
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

        TransactionDiagnosticsLog.lateAmountMatch(
                label,
                tx,
                wantedSource,
                eventNewDamage,
                Math.abs(tx.eventAmountAfterSet() - eventNewDamage),
                staleDropped
        );
    }

    public static DamageNexusTransaction promoteIncomingCandidate(
            DamageContainer container,
            LivingDamageEvent.Pre event
    ) {
        if (!enabled() || container == null || event == null) {
            return null;
        }

        LivingEntity victim = event.getEntity();
        long now = victim.level().getGameTime();

        pruneIncomingCandidates(now);

        DamageNexusTransaction incoming;

        synchronized (INCOMING_CANDIDATES) {
            incoming = INCOMING_CANDIDATES.remove(container);
        }

        if (incoming == null) {
            logMissingCandidateAtPre(container, event);
            return null;
        }

        if (!isTrackableAmount(event.getNewDamage())) {
            logDrop(
                    "pre_zero_or_invalid_damage",
                    incoming,
                    event.getSource(),
                    event.getNewDamage()
            );
            return null;
        }

        DamageNexusTransaction promoted =
                withPreSnapshot(incoming, event);

        logCandidatePromoted(container, incoming, promoted, event);

        recordPostTrackable(promoted);

        return promoted;
    }

    private static void logAmbiguousLateAmountMatch(
            DamageNexusTransaction first,
            DamageNexusTransaction second,
            Identifier wantedSourceId
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        TransactionDiagnosticsLog.ambiguousLateAmountMatch(
                wantedSourceId.toString(),
                first,
                second
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

        TransactionDiagnosticsLog.noMatch(
                victim,
                source,
                eventNewDamage
        );
    }

    private static LateMatchKind classifyLateAmountMatch(
            DamageNexusTransaction tx,
            float eventNewDamage
    ) {
        if (isLikelyVanillaInvulnerabilityAdjustment(tx, eventNewDamage)) {
            return LateMatchKind.VANILLA_INVULNERABILITY_ADJUSTED;
        }

        return LateMatchKind.AMOUNT_CHANGED;
    }

    private static boolean isLikelyVanillaInvulnerabilityAdjustment(
            DamageNexusTransaction tx,
            float eventNewDamage
    ) {
        return tx.victimInvulnerableTimeBefore() > 0
                && eventNewDamage + ABSOLUTE_AMOUNT_EPSILON < tx.eventAmountAfterSet();
    }

    private static DamageNexusTransaction withPreSnapshot(
            DamageNexusTransaction incoming,
            LivingDamageEvent.Pre event
    ) {
        LivingEntity victim = event.getEntity();

        return new DamageNexusTransaction(
                incoming.damageId(),
                incoming.attacker(),
                incoming.victim(),
                incoming.source(),

                incoming.eventOriginalAmount(),
                incoming.initialBaseAmount(),
                incoming.offensiveTotal(),
                incoming.finalEventAmount(),

                incoming.eventAmountBeforeSet(),
                incoming.eventAmountAfterSet(),

                victim.getHealth(),
                victim.getAbsorptionAmount(),
                victim.invulnerableTime,
                victim.level().getGameTime()
        );
    }

    private static void recordPostTrackable(
            DamageNexusTransaction tx
    ) {
        DamageTransactionQueue txQueue =
                tx.victim().getData(ModAttachments.DAMAGE_TRANSACTIONS);

        Deque<DamageNexusTransaction> queue =
                txQueue.entries();

        queue.addLast(tx);

        while (queue.size() > MAX_QUEUE_SIZE_PER_VICTIM) {
            DamageNexusTransaction dropped = queue.removeFirst();

            logDrop(
                    "post_queue_overflow",
                    dropped,
                    null,
                    Float.NaN
            );
        }
    }

    private static boolean isTrackableAmount(float amount) {
        return Float.isFinite(amount) && amount > ABSOLUTE_AMOUNT_EPSILON;
    }

    private static void logCandidateRecorded(
            DamageContainer container,
            DamageNexusTransaction tx
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        TransactionDiagnosticsLog.candidateRecord(
                tx,
                System.identityHashCode(container)
        );
    }

    private static void logCandidatePromoted(
            DamageContainer container,
            DamageNexusTransaction incoming,
            DamageNexusTransaction promoted,
            LivingDamageEvent.Pre event
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        TransactionDiagnosticsLog.candidatePromote(
                promoted,
                event.getNewDamage(),
                System.identityHashCode(container)
        );
    }

    private static void logMissingCandidateAtPre(
            DamageContainer container,
            LivingDamageEvent.Pre event
    ) {
        if (!ModConfig.isDebugMode()) {
            return;
        }

        TransactionDiagnosticsLog.preWithoutCandidate(
                event.getEntity(),
                event.getSource(),
                event.getNewDamage(),
                event.getEntity().level().getGameTime(),
                System.identityHashCode(container)
        );
    }

    private static void pruneIncomingCandidates(long now) {
        int removed = 0;
        int remaining;

        synchronized (INCOMING_CANDIDATES) {
            var iterator = INCOMING_CANDIDATES.entrySet().iterator();

            while (iterator.hasNext()) {
                DamageNexusTransaction tx =
                        iterator.next().getValue();

                if (tx == null
                        || tx.victim() == null
                        || tx.victim().isRemoved()
                        || now - tx.gameTime() > MAX_TRANSACTION_AGE_TICKS) {
                    iterator.remove();
                    removed++;
                }
            }

            remaining = INCOMING_CANDIDATES.size();
        }

        if (ModConfig.isDebugMode()) {
            TransactionDiagnosticsLog.candidatePrune(
                    removed,
                    remaining,
                    now
            );
        }
    }

    public static boolean enabled() {
        return ModConfig.postDamageDiagnosticsEnabled();
    }
}
