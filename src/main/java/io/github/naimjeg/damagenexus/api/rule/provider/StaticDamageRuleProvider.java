package io.github.naimjeg.damagenexus.api.rule.provider;

import io.github.naimjeg.damagenexus.api.enums.DamagePhase;
import io.github.naimjeg.damagenexus.api.rule.*;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StaticDamageRuleProvider implements DamageRuleProvider {

    private final List<DamageRuleDefinition> rules;

    public StaticDamageRuleProvider(DamageRuleDefinition rule) {
        this(List.of(rule));
    }

    public StaticDamageRuleProvider(List<DamageRuleDefinition> rules) {
        Objects.requireNonNull(rules, "rules");

        for (DamageRuleDefinition rule : rules) {
            DamageRuleValidator.requireValid(
                    rule,
                    "java_api/static_provider"
            );
        }

        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean supportsPhase(DamagePhase phase) {
        for (DamageRuleDefinition rule : rules) {
            if (rule.phase() == phase) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void collect(
            DamageNexusContext ctx,
            DamagePhase phase,
            List<RuntimeDamageRule> out
    ) {
        for (DamageRuleDefinition rule : rules) {
            if (rule.phase() != phase) {
                continue;
            }

            out.add(new RuntimeDamageRule(
                    rule,
                    RuleExecutionContext.javaApiRule(rule.role())
            ));
        }
    }

    public List<DamageRuleDefinition> rules() {
        return rules;
    }

    public static StaticDamageRuleProvider of(DamageRuleDefinition rule) {
        return new StaticDamageRuleProvider(rule);
    }

    public static StaticDamageRuleProvider ofAll(List<DamageRuleDefinition> rules) {
        return new StaticDamageRuleProvider(new ArrayList<>(rules));
    }
}