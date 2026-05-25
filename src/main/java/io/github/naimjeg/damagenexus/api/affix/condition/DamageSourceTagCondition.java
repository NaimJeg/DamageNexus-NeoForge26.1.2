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
import net.minecraft.world.damagesource.DamageType;

public record DamageSourceTagCondition(
        TagKey<DamageType> tag
) implements AffixCondition {

    public static final MapCodec<DamageSourceTagCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    TagKey.codec(Registries.DAMAGE_TYPE)
                            .fieldOf("tag")
                            .forGetter(DamageSourceTagCondition::tag)
            ).apply(instance, DamageSourceTagCondition::new));

    @Override
    public Identifier type() {
        return AffixConditionTypes.DAMAGE_SOURCE_TAG;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        return ctx.source != null && ctx.source.is(tag);
    }
}