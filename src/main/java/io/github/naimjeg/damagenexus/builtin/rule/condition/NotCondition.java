package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public record NotCondition(
        DamageRuleCondition condition
) implements DamageRuleCondition {

    public static final MapCodec<NotCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DamageRuleCondition.CODEC
                            .fieldOf("condition")
                            .forGetter(NotCondition::condition)
            ).apply(instance, NotCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.NOT;
    }

    @Override
    public boolean test(DamageRuleContext ctx) {
        return !condition.test(ctx);
    }

    @Override
    public boolean test(
            DamageRuleContext ctx,
            RuleExecutionContext executionContext
    ) {
        return !condition.test(ctx, executionContext);
    }
}
