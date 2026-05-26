package io.github.naimjeg.damagenexus.registry.rule;

import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.builtin.rule.condition.*;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class DamageRuleConditionTypes {

    private static final Map<Identifier, MapCodec<? extends DamageRuleCondition>> CODECS =
            new HashMap<>();

    public static final Identifier DAMAGE_CHANNEL_IS =
            id("damage_channel_is");

    public static final Identifier ALWAYS =
            id("always");

    public static final Identifier TARGET_ON_FIRE =
            id("target_on_fire");

    public static final Identifier TARGET_ENTITY_TYPE_TAG =
            id("target_entity_type_tag");

    public static final Identifier DAMAGE_SOURCE_TAG =
            id("damage_source_tag");

    public static final Identifier ATTACKER_HEALTH_BELOW =
            id("attacker_health_below");

    static {
        register(ALWAYS, AlwaysCondition.CODEC);
        register(TARGET_ON_FIRE, TargetOnFireCondition.CODEC);
        register(TARGET_ENTITY_TYPE_TAG, TargetEntityTypeTagCondition.CODEC);
        register(DAMAGE_SOURCE_TAG, DamageSourceTagCondition.CODEC);
        register(ATTACKER_HEALTH_BELOW, AttackerHealthBelowCondition.CODEC);
        register(DAMAGE_CHANNEL_IS, DamageChannelIsCondition.CODEC);
    }

    private DamageRuleConditionTypes() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    private static void register(
            Identifier id,
            MapCodec<? extends DamageRuleCondition> codec
    ) {
        CODECS.put(id, codec);
    }

    public static MapCodec<? extends DamageRuleCondition> codec(Identifier id) {
        MapCodec<? extends DamageRuleCondition> codec = CODECS.get(id);

        if (codec == null) {
            throw new IllegalArgumentException(
                    "Unknown DamageNexus rule condition type: " + id
            );
        }

        return codec;
    }
}