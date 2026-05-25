package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.core.pipeline.DamageNexusContext;
import io.github.naimjeg.damagenexus.registry.affix.AffixEffectTypes;
import net.minecraft.resources.Identifier;

public interface AffixEffect {

    Codec<AffixEffect> CODEC =
            Identifier.CODEC.dispatch(
                    "type",
                    AffixEffect::type,
                    AffixEffectTypes::codec
            );

    Identifier type();

    void apply(DamageNexusContext ctx);
}