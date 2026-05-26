package io.github.naimjeg.damagenexus.core.rule;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleDefinition;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

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
            if (!condition.test(ctx)) {
                ctx.debugger.logRuleConditionFailed(
                        runningPhase,
                        rule,
                        condition
                );
                return;
            }
        }

        ctx.debugger.logRuleExecuted(
                runningPhase,
                rule
        );

        for (DamageRuleOperation operation : rule.operations()) {
            operation.apply(ctx);
        }
    }
}