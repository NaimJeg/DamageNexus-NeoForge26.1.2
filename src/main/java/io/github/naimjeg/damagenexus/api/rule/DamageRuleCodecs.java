package io.github.naimjeg.damagenexus.api.rule;

import com.mojang.serialization.Codec;
import io.github.naimjeg.damagenexus.api.enums.DamageChannel;
import io.github.naimjeg.damagenexus.core.registry.DamageChannelRegistry;
import net.minecraft.resources.Identifier;

public final class DamageRuleCodecs {

    public static final Codec<Identifier> DAMAGE_CHANNEL_ID =
            Identifier.CODEC;

    private DamageRuleCodecs() {}
}