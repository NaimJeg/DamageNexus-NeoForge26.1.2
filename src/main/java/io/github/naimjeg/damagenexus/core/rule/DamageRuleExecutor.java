package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.core.trace.RuleSkipReason;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public final class DamageRuleExecutor {

    private DamageRuleExecutor() {}

    public static void execute(
            DamageNexusContext ctx,
            DamagePhase runningPhase,
            RuntimeDamageRule runtimeRule
    ) {
        DamageRuleDefinition rule = runtimeRule.definition();

        if (rule.phase() != runningPhase) {
            ctx.debugger.logRulePhaseMismatch(
                    runningPhase,
                    rule
            );
            return;
        }

        if (!rule.role().canRunAs(runtimeRule.executionContext().role())) {
            ctx.debugger.logRuleRoleMismatch(
                    runningPhase,
                    rule,
                    runtimeRule.executionContext()
            );
            return;
        }

        for (DamageRuleCondition condition : rule.conditions()) {
            if (!condition.test(ctx, runtimeRule.executionContext())) {
                ctx.debugger.logRuleConditionFailed(
                        runningPhase,
                        rule,
                        condition
                );
                return;
            }
        }

        if (rule.operations().isEmpty()) {
            ctx.debugger.logRuleSkipped(
                    runningPhase,
                    rule,
                    RuleSkipReason.EMPTY_OPERATIONS
            );
            return;
        }

        ctx.debugger.logRuleExecuted(
                runningPhase,
                rule
        );

        for (DamageRuleOperation operation : rule.operations()) {
            if (!isOperationAllowedInPhase(operation, runningPhase)) {
                ctx.debugger.logRuleSkipped(
                        runningPhase,
                        rule,
                        RuleSkipReason.PHASE_OPERATION_MISMATCH
                );
                continue;
            }

            operation.apply(ctx);
        }
    }

    public static boolean isOperationAllowedInPhase(
            DamageRuleOperation operation,
            DamagePhase phase
    ) {
        Identifier type = operation.type();

        if (type.equals(DamageRuleOperationTypes.ADD_BASE_DAMAGE)) {
            return phase == DamagePhase.BASE_MODIFICATION;
        }

        if (type.equals(DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER)
                || type.equals(DamageRuleOperationTypes.ADD_GLOBAL_PRE_MULTIPLIER)) {
            return phase == DamagePhase.TYPE_SCALING
                    || phase == DamagePhase.CRITICAL_HIT
                    || phase == DamagePhase.CONDITIONAL_MULTI
                    || phase == DamagePhase.GLOBAL_ADJUSTMENT;
        }

        if (type.equals(DamageRuleOperationTypes.ADD_CHANNEL_POST_MULTIPLIER)
                || type.equals(DamageRuleOperationTypes.ADD_GLOBAL_POST_MULTIPLIER)) {
            return phase == DamagePhase.CONDITIONAL_MULTI
                    || phase == DamagePhase.GLOBAL_ADJUSTMENT;
        }

        if (type.equals(DamageRuleOperationTypes.ADD_TEMPORARY_RESISTANCE)) {
            return phase == DamagePhase.MITIGATION_SETUP;
        }

        if (type.equals(DamageRuleOperationTypes.OVERRIDE_FINAL_DAMAGE)) {
            return phase == DamagePhase.FINAL_OVERRIDE;
        }

        return true;
    }
}