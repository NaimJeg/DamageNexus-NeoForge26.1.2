package io.github.naimjeg.damagenexus.registry.rule;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleOperation;
import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class DamageRuleOperationTypes {

    private static final Map<Identifier, MapCodec<? extends DamageRuleOperation>> CODECS =
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

    public static final Identifier ADD_TEMPORARY_RESISTANCE =
            id("add_temporary_resistance");

    static {
        register(ADD_BASE_DAMAGE, AddBaseDamageOperation.CODEC);
        register(ADD_CHANNEL_PRE_MULTIPLIER, AddChannelPreMultiplierOperation.CODEC);
        register(ADD_CHANNEL_POST_MULTIPLIER, AddChannelPostMultiplierOperation.CODEC);
        register(ADD_GLOBAL_POST_MULTIPLIER, AddGlobalPostMultiplierOperation.CODEC);
        register(OVERRIDE_FINAL_DAMAGE, OverrideFinalDamageOperation.CODEC);
        register(ADD_TEMPORARY_RESISTANCE, AddTemporaryResistanceOperation.CODEC);
    }

    private DamageRuleOperationTypes() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    private static void register(
            Identifier id,
            MapCodec<? extends DamageRuleOperation> codec
    ) {
        CODECS.put(id, codec);
    }

    public static MapCodec<? extends DamageRuleOperation> codec(Identifier id) {
        MapCodec<? extends DamageRuleOperation> codec = CODECS.get(id);

        if (codec == null) {
            throw new IllegalArgumentException(
                    "Unknown DamageNexus rule operation type: " + id
            );
        }

        return codec;
    }
}