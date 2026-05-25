package io.github.naimjeg.damagenexus.api.affix.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record TargetEntityTypeTagCondition(
        TagKey<EntityType<?>> tag
) implements AffixCondition {

    public static final MapCodec<TargetEntityTypeTagCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    TagKey.codec(Registries.ENTITY_TYPE)
                            .fieldOf("tag")
                            .forGetter(TargetEntityTypeTagCondition::tag)
            ).apply(instance, TargetEntityTypeTagCondition::new));

    @Override
    public Identifier type() {
        return AffixConditionTypes.TARGET_ENTITY_TYPE_TAG;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return ctx.victim != null
                && ctx.victim.getType()
                .getTags()
                .anyMatch(tag::equals);
    }
}