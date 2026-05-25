package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixConditionTypes;
import net.minecraft.resources.Identifier;

public interface AffixCondition {

    Codec<AffixCondition> CODEC =
            Identifier.CODEC.dispatch(
                    "type",
                    AffixCondition::type,
                    AffixConditionTypes::codec
            );

    Identifier type();

    boolean test(DamageNexusContext ctx);
}