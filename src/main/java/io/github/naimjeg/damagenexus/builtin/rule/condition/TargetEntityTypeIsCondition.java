package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import io.github.naimjeg.damagenexus.util.EntityConditionUtil;
import net.minecraft.resources.Identifier;

public record TargetEntityTypeIsCondition(
        Identifier entityType
) implements DamageRuleCondition {

    public static final MapCodec<TargetEntityTypeIsCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC
                            .fieldOf("entity_type")
                            .forGetter(TargetEntityTypeIsCondition::entityType)
            ).apply(instance, TargetEntityTypeIsCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.TARGET_ENTITY_TYPE_IS;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return EntityConditionUtil.isEntityType(
                ctx.victim(),
                entityType
        );
    }
}