package io.github.naimjeg.damagenexus.api.rule.affix;

import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.api.rule.RuntimeDamageRule;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record RuntimeDamageAffix(
        DamageAffixDefinition definition,
        RuleExecutionContext executionContext
) {
    public Identifier id() {
        return definition.id();
    }

    public List<RuntimeDamageRule> expandRules() {
        return definition.rules()
                .stream()
                .map(rule -> new RuntimeDamageRule(
                        rule,
                        executionContext,
                        Optional.of(definition.id())
                ))
                .toList();
    }
}