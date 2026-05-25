package io.github.naimjeg.damagenexus.api.affix.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.api.affix.AffixEntityTarget;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public record EntityCounterAtLeastCondition(
        AffixEntityTarget target,
        Identifier counter,
        int value
) implements AffixCondition {

    public static final MapCodec<EntityCounterAtLeastCondition> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AffixEntityTarget.CODEC
                            .fieldOf("target")
                            .forGetter(EntityCounterAtLeastCondition::target),

                    Identifier.CODEC
                            .fieldOf("counter")
                            .forGetter(EntityCounterAtLeastCondition::counter),

                    Codec.INT
                            .fieldOf("value")
                            .forGetter(EntityCounterAtLeastCondition::value)
            ).apply(instance, EntityCounterAtLeastCondition::new));

    @Override
    public Identifier type() {
        return AffixConditionTypes.ENTITY_COUNTER_AT_LEAST;
    }

    @Override
    public boolean test(DamageNexusContext ctx) {
        LivingEntity entity = target.resolve(ctx);

        if (entity == null) {
            return false;
        }

//        CombatRuntimeData data =
//                entity.getData(ModAttachments.COMBAT_RUNTIME.get());

        //data.getCounter(counter) >= value;
        return false;
    }
}