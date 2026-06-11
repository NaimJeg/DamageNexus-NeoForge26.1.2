package io.github.naimjeg.damagenexus.core.trace;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamageNexusTransactionTrackerTest {

    private static final Identifier SOURCE =
            Identifier.fromNamespaceAndPath("test", "source");

    private static final Identifier OTHER_SOURCE =
            Identifier.fromNamespaceAndPath("test", "other_source");

    @Test
    void exactMatchRemovesMatchedTransaction() {
        DamageNexusTransaction exact = tx(1, 6.0f, 100);
        Deque<DamageNexusTransaction> queue =
                new ArrayDeque<>(List.of(exact));

        DamageNexusTransaction result = poll(queue, SOURCE, 101, 6.0f);

        assertSame(exact, result);
        assertTrue(queue.isEmpty());
    }

    @Test
    void lateMatchRemovesMatchedTransaction() {
        DamageNexusTransaction late = tx(1, 10.0f, 100);
        Deque<DamageNexusTransaction> queue =
                new ArrayDeque<>(List.of(late));

        DamageNexusTransaction result = poll(queue, SOURCE, 101, 7.0f);

        assertSame(late, result);
        assertTrue(queue.isEmpty());
    }

    @Test
    void staleTransactionBeforeLateMatchIsDropped() {
        DamageNexusTransaction stale = tx(1, 10.0f, 97);
        DamageNexusTransaction late = tx(2, 10.0f, 100);
        Deque<DamageNexusTransaction> queue =
                new ArrayDeque<>(List.of(stale, late));

        DamageNexusTransaction result = poll(queue, SOURCE, 101, 7.0f);

        assertSame(late, result);
        assertTrue(queue.isEmpty());
    }

    @Test
    void transactionAfterLateMatchRemainsQueued() {
        DamageNexusTransaction late = tx(1, 10.0f, 100);
        DamageNexusTransaction nextValid = tx(2, 4.0f, 101);
        Deque<DamageNexusTransaction> queue =
                new ArrayDeque<>(List.of(late, nextValid));

        DamageNexusTransaction result = poll(
                queue,
                SOURCE,
                101,
                7.0f,
                Map.of(nextValid.damageId(), OTHER_SOURCE)
        );

        assertSame(late, result);
        assertEquals(List.of(nextValid), new ArrayList<>(queue));
    }

    @Test
    void lateMatchDoesNotRemoveNextValidAfterStalePrefix() {
        DamageNexusTransaction stale = tx(1, 10.0f, 97);
        DamageNexusTransaction late = tx(2, 10.0f, 100);
        DamageNexusTransaction nextValid = tx(3, 4.0f, 101);
        Deque<DamageNexusTransaction> queue =
                new ArrayDeque<>(List.of(stale, late, nextValid));

        DamageNexusTransaction result = poll(
                queue,
                SOURCE,
                101,
                7.0f,
                Map.of(nextValid.damageId(), OTHER_SOURCE)
        );

        assertSame(late, result);
        assertEquals(List.of(nextValid), new ArrayList<>(queue));
    }

    private static DamageNexusTransaction poll(
            Deque<DamageNexusTransaction> queue,
            Identifier wantedSource,
            long now,
            float eventDamage
    ) {
        return poll(queue, wantedSource, now, eventDamage, Map.of());
    }

    private static DamageNexusTransaction poll(
            Deque<DamageNexusTransaction> queue,
            Identifier wantedSource,
            long now,
            float eventDamage,
            Map<Long, Identifier> overrides
    ) {
        Function<DamageNexusTransaction, Identifier> sourceResolver =
                tx -> overrides.getOrDefault(tx.damageId(), SOURCE);

        return DamageNexusTransactionTracker.pollMatchingPostTrackable(
                queue,
                wantedSource,
                now,
                sourceResolver,
                null,
                eventDamage
        );
    }

    private static DamageNexusTransaction tx(
            long id,
            float amountAfterSet,
            long gameTime
    ) {
        return new DamageNexusTransaction(
                id,
                null,
                null,
                null,
                amountAfterSet,
                amountAfterSet,
                amountAfterSet,
                amountAfterSet,
                amountAfterSet,
                amountAfterSet,
                20.0f,
                0.0f,
                0,
                gameTime
        );
    }
}
