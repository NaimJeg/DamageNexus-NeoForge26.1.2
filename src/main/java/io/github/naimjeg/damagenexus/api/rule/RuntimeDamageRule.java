package io.github.naimjeg.damagenexus.api.rule;

public record RuntimeDamageRule(
        DamageRuleDefinition definition,
        RuleExecutionContext executionContext
) {}