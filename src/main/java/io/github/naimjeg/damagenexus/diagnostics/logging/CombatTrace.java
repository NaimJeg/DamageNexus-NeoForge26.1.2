package io.github.naimjeg.damagenexus.diagnostics.logging;

public interface CombatTrace {

    boolean enabled();

    CombatTransactionLog transaction();

    CombatPipelineLog pipeline();

    CombatRuleLog rules();

    CombatMutationLog mutations();

    CombatCalculationLog calculation();
}
