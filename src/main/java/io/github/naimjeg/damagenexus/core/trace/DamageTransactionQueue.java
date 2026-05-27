package io.github.naimjeg.damagenexus.core.trace;

import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

import java.util.ArrayDeque;
import java.util.Deque;

public final class DamageTransactionQueue {

    private final ArrayDeque<DamageNexusContext.DamageNexusTransaction> queue =
            new ArrayDeque<>();

    public Deque<DamageNexusContext.DamageNexusTransaction> queue() {
        return queue;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void clear() {
        queue.clear();
    }
}