package io.github.naimjeg.damagenexus.core.trace;

import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

import java.util.ArrayDeque;
import java.util.Deque;

public final class DamageTransactionQueue {

    private final Deque<DamageNexusContext.DamageNexusTransaction> entries =
            new ArrayDeque<>();

    public Deque<DamageNexusContext.DamageNexusTransaction> entries() {
        return entries;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public void addLast(DamageNexusContext.DamageNexusTransaction tx) {
        entries.addLast(tx);
    }

    public DamageNexusContext.DamageNexusTransaction removeFirst() {
        return entries.removeFirst();
    }


}