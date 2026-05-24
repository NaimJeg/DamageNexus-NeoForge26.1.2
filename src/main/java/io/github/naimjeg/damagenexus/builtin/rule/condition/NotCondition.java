package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.api.rule.RuleExecutionContext;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
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
    public boolean test(DamageNexusContext ctx) {
        return !condition.test(ctx);
    }

    @Override
    public boolean test(
            DamageNexusContext ctx,
            RuleExecutionContext executionContext
    ) {
        return !condition.test(ctx, executionContext);
    }
}