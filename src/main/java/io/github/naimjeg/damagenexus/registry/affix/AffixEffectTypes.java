package io.github.naimjeg.damagenexus.registry.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.affix.AffixEffect;
import io.github.naimjeg.damagenexus.api.affix.effect.*;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class AffixEffectTypes {

    private static final Map<Identifier, MapCodec<? extends AffixEffect>> CODECS =
            new HashMap<>();

    public static final Identifier ADD_BASE_DAMAGE =
            id("add_base_damage");

    public static final Identifier ADD_CHANNEL_PRE_MULTIPLIER =
            id("add_channel_pre_multiplier");

    public static final Identifier ADD_CHANNEL_POST_MULTIPLIER =
            id("add_channel_post_multiplier");

    public static final Identifier ADD_GLOBAL_POST_MULTIPLIER =
            id("add_global_post_multiplier");

    public static final Identifier OVERRIDE_FINAL_DAMAGE =
            id("override_final_damage");

    static {
        register(ADD_BASE_DAMAGE, AddBaseDamageEffect.CODEC);
        register(ADD_CHANNEL_PRE_MULTIPLIER, AddChannelPreMultiplierEffect.CODEC);
        register(ADD_CHANNEL_POST_MULTIPLIER, AddChannelPostMultiplierEffect.CODEC);
        register(ADD_GLOBAL_POST_MULTIPLIER, AddGlobalPostMultiplierEffect.CODEC);
        register(OVERRIDE_FINAL_DAMAGE, OverrideFinalDamageEffect.CODEC);
    }

    private AffixEffectTypes() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    private static void register(
            Identifier id,
            MapCodec<? extends AffixEffect> codec
    ) {
        CODECS.put(id, codec);
    }

    public static MapCodec<? extends AffixEffect> codec(Identifier id) {
        MapCodec<? extends AffixEffect> codec = CODECS.get(id);

        if (codec == null) {
            throw new IllegalArgumentException(
                    "Unknown DamageNexus affix effect type: " + id
            );
        }

        return codec;
    }
}