package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
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

    boolean test(DamageNexusContext ctx);

    default boolean test(DamageNexusContext ctx, RuleExecutionContext executionContext) {
        return test(ctx);
    }
}