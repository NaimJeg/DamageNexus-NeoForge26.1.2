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

    public static final Identifier ADD_GLOBAL_PRE_MULTIPLIER =
            id("add_global_pre_multiplier");

    public static final Identifier ADD_GLOBAL_POST_MULTIPLIER =
            id("add_global_post_multiplier");

    public static final Identifier OVERRIDE_FINAL_DAMAGE =
            id("override_final_damage");

    public static final Identifier CANCEL_DAMAGE =
            id("cancel_damage");

    public static final Identifier ADD_TEMPORARY_RESISTANCE =
            id("add_temporary_resistance");

    public static final Identifier CONVERT_DAMAGE =
            id("convert_damage");

    public static final Identifier GAIN_EXTRA_DAMAGE =
            id("gain_extra_damage");

    public static final Identifier ADD_CHANNEL_MITIGATION =
            id("add_channel_mitigation");

    public static final Identifier ADD_GLOBAL_MITIGATION =
            id("add_global_mitigation");

    public static final Identifier MULTIPLY_ARMOR_EFFECTIVENESS =
            id("multiply_armor_effectiveness");

    public static final Identifier ADD_TRUE_DAMAGE =
            id("add_true_damage");

    static {
        register(ADD_BASE_DAMAGE, AddBaseDamageOperation.CODEC);
        register(ADD_CHANNEL_PRE_MULTIPLIER, AddChannelPreMultiplierOperation.CODEC);
        register(ADD_CHANNEL_POST_MULTIPLIER, AddChannelPostMultiplierOperation.CODEC);
        register(ADD_GLOBAL_POST_MULTIPLIER, AddGlobalPostMultiplierOperation.CODEC);
        register(OVERRIDE_FINAL_DAMAGE, OverrideFinalDamageOperation.CODEC);
        register(CANCEL_DAMAGE, CancelDamageOperation.CODEC);
        register(ADD_TEMPORARY_RESISTANCE, AddTemporaryResistanceOperation.CODEC);
        register(ADD_GLOBAL_PRE_MULTIPLIER, AddGlobalPreMultiplierOperation.CODEC);
        register(CONVERT_DAMAGE, ConvertDamageOperation.CODEC);
        register(GAIN_EXTRA_DAMAGE, GainExtraDamageOperation.CODEC);
        register(ADD_CHANNEL_MITIGATION, AddChannelMitigationOperation.CODEC);
        register(ADD_TRUE_DAMAGE, AddTrueDamageOperation.CODEC);
        register(ADD_GLOBAL_MITIGATION, AddGlobalMitigationOperation.CODEC);
        register(MULTIPLY_ARMOR_EFFECTIVENESS, MultiplyArmorEffectivenessOperation.CODEC);
    }

    private DamageRuleOperationTypes() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    public static synchronized void register(
            Identifier id,
            MapCodec<? extends DamageRuleOperation> codec
    ) {
        if (CODECS.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Duplicate DamageNexus rule operation type: " + id
            );
        }

        CODECS.put(id, codec);
    }

    public static synchronized MapCodec<? extends DamageRuleOperation> codec(Identifier id) {
        MapCodec<? extends DamageRuleOperation> codec = CODECS.get(id);

        if (codec == null) {
            throw new IllegalArgumentException(
                    "Unknown DamageNexus rule operation type: " + id
            );
        }

        return codec;
    }
}