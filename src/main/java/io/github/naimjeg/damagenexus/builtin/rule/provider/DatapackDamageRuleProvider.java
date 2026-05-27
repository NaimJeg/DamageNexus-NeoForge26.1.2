package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

import java.util.List;

public final class DatapackDamageRuleProvider implements DamageRuleProvider {

    private static volatile List<DamageRuleDefinition> RULES = List.of();

    public static void setRules(List<DamageRuleDefinition> rules) {
        RULES = DamageRuleValidator.filterValid(
                rules,
                "datapack"
        );
    }

    public static int ruleCount() {
        return RULES.size();
    }

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        for (DamageRuleDefinition rule : RULES) {
            if (rule.phase() != phase) {
                continue;
            }

            RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                    rule,
                    RuleExecutionContext.datapackRule(rule.role())
            );

            ctx.debugger.logRuleCollected(
                    phase,
                    rule,
                    runtimeRule.executionContext()
            );

            out.add(runtimeRule);
        }
    }
}