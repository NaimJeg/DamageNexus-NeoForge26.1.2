package io.github.naimjeg.damagenexus.builtin.rule.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import io.github.naimjeg.damagenexus.util.EntityConditionUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record TargetEntityTypeTagCondition(
        TagKey<EntityType<?>> tag
) implements DamageRuleCondition {

    public static final MapCodec<TargetEntityTypeTagCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    TagKey.codec(Registries.ENTITY_TYPE)
                            .fieldOf("tag")
                            .forGetter(TargetEntityTypeTagCondition::tag)
            ).apply(instance, TargetEntityTypeTagCondition::new));

    @Override
    public Identifier type() {
        return DamageRuleConditionTypes.TARGET_ENTITY_TYPE_TAG;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return EntityConditionUtil.isEntityTypeTag(
                ctx.victim,
                tag
        );
    }
}