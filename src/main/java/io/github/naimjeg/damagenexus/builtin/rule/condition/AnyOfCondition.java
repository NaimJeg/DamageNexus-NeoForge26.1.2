package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

import java.util.List;

public record AnyOfCondition(
        List<DamageRuleCondition> conditions
) implements DamageRuleCondition {

    public static final MapCodec<AnyOfCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCondition.CODEC
                            .listOf()
                            .fieldOf("conditions")
                            .forGetter(AnyOfCondition::conditions)
            ).apply(instance, AnyOfCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.ANY_OF;
    }

    @Override
    public boolean test(DamageRuleContext ctx) {
        for (DamageRuleCondition condition : conditions) {
            if (condition.test(ctx)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean test(
            DamageRuleContext ctx,
            RuleExecutionContext executionContext
    ) {
        for (DamageRuleCondition condition : conditions) {
            if (condition.test(ctx, executionContext)) {
                return true;
            }
        }

        return false;
    }
}
