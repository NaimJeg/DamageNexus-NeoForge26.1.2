package io.github.naimjeg.damagenexus.diagnostics.logging;

public enum DamageNexusLogKind {
    /**
     * Full per-transaction trace:
     * PHASE, PROCESSOR_RUN, PROCESSOR_SKIP, RULE_SKIP, bucket details, mutations.
     * Server log only unless client forwarding verbosity is FULL.
     */
    TRACE_DETAIL,

    /**
     * Compact transaction-level lines:
     * BEGIN, APPLY, POST observed, CANDIDATE_PROMOTE.
     */
    TRACE_SUMMARY,

    /**
     * Diagnostics used for mod compatibility investigation:
     * bypass suspicion, unmatched Post, vanilla adjustment, transaction lifecycle.
     */
    COMPATIBILITY,

    /**
     * Warnings or suspicious states.
     * Always eligible for client forwarding if forwarding is enabled.
     */
    WARNING,

    /**
     * Lifecycle/startup/config/reload logs.
     * Should normally remain server log only.
     */
    LIFECYCLE
}