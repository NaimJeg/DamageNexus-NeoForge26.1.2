package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.api.context.DamageRuleContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.resources.Identifier;

public interface DamageRuleCondition {

    Codec<DamageRuleCondition> CODEC =
            Identifier.CODEC.dispatch(
                    "type",
                    DamageRuleCondition::type,
                    DamageRuleConditionTypes::codec
            );

    Identifier type();

    boolean test(DamageRuleContext ctx);

    default boolean test(
            DamageRuleContext ctx,
            RuleExecutionContext executionContext
    ) {
        return test(ctx);
    }
}
