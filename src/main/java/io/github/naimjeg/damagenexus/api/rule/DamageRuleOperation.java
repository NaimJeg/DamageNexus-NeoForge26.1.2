package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.resources.Identifier;

public interface DamageRuleOperation {

    Codec<DamageRuleOperation> CODEC =
            Identifier.CODEC.dispatch(
                    "type",
                    DamageRuleOperation::type,
                    DamageRuleOperationTypes::codec
            );

    Identifier type();

    void apply(DamageNexusContext ctx);

    default float stackingValue() {
        return 0.0f;
    }
}