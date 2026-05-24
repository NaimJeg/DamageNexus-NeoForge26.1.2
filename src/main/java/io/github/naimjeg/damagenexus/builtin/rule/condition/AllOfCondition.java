package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

import java.util.List;

public record AllOfCondition(
        List<DamageRuleCondition> conditions
) implements DamageRuleCondition {

    public static final MapCodec<AllOfCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCondition.CODEC
                            .listOf()
                            .fieldOf("conditions")
                            .forGetter(AllOfCondition::conditions)
            ).apply(instance, AllOfCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.ALL_OF;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        for (DamageRuleCondition condition : conditions) {
            if (!condition.test(ctx)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean test(
            DamageNexusContext ctx,
            RuleExecutionContext executionContext
    ) {
        for (DamageRuleCondition condition : conditions) {
            if (!condition.test(ctx, executionContext)) {
                return false;
            }
        }

        return true;
    }
}