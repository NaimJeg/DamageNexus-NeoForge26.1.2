package io.github.naimjeg.damagenexus.api.affix;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import net.minecraft.resources.Identifier;

public final class AffixCodecs {

    public static final Codec<DamageChannel> DAMAGE_CHANNEL =
            Identifier.CODEC.xmap(
                    DamageChannelRegistry::getChannelOrUntyped,
                    DamageChannel::id
            );

    private AffixCodecs() {}
}