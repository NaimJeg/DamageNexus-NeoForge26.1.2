package io.github.naimjeg.damagenexus.core.trace;

public enum RuleSkipReason {
    PHASE_MISMATCH,
    ROLE_MISMATCH,
    CONDITION_FAILED,
    STACKING_DROPPED,
    EMPTY_OPERATION,
    PROVIDER_FILTERED
}