package io.github.naimjeg.damagenexus.api.rule;

import net.minecraft.resources.Identifier;

import java.util.Optional;

public record RuntimeDamageRule(
        DamageRuleDefinition definition,
        RuleExecutionContext executionContext,
        Optional<Identifier> affixId
) {
    public RuntimeDamageRule(
            DamageRuleDefinition definition,
            RuleExecutionContext executionContext
    ) {
        this(definition, executionContext, Optional.empty());
    }

    public RuntimeDamageRule {
        affixId = affixId != null ? affixId : Optional.empty();
    }

    public boolean belongsToAffix() {
        return affixId.isPresent();
    }
}