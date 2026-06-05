package io.github.naimjeg.damagenexus.builtin.rule.provider;

import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.core.pipeline.DamageInternalContexts;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

import java.util.List;

public final class DatapackDamageRuleProvider implements DamageRuleProvider {

    private static volatile List<DamageRuleDefinition> RULES = List.of();

    public static void setRules(List<DamageRuleDefinition> rules) {
        List<DamageRuleDefinition> structurallyValid =
                DamageRuleValidator.filterValid(
                        rules,
                        "datapack"
                );

        RULES = structurallyValid.stream()
                .filter(rule -> DamageRuleReferenceValidator.validateDatapackReferences(
                        rule,
                        "datapack/reference",
                        DamageRuleValidator.Policy.WARN
                ))
                .toList();
    }

    public static int ruleCount() {
        return RULES.size();
    }

    @Override
    public void collect(
            DamageRuleContext context,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        DamageNexusContext ctx = DamageInternalContexts.require(
                context,
                "datapack rule provider"
        );

        for (DamageRuleDefinition rule : RULES) {
            if (rule.phase() != phase) {
                continue;
            }

            RuntimeDamageRule runtimeRule = new RuntimeDamageRule(
                    rule,
                    RuleExecutionContext.datapackRule(rule.role())
            );

            ctx.trace().rules().collected(
                    phase,
                    rule,
                    runtimeRule.executionContext()
            );

            out.add(runtimeRule);
        }
    }
}

