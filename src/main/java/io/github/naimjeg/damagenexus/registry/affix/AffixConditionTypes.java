package io.github.naimjeg.damagenexus.registry.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.naimjeg.damagenexus.DamageNexus;
import io.github.naimjeg.damagenexus.api.affix.AffixCondition;
import io.github.naimjeg.damagenexus.api.affix.condition.*;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class AffixConditionTypes {

    private static final Map<Identifier, MapCodec<? extends AffixCondition>> CODECS =
            new HashMap<>();

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

    public static final Identifier ENTITY_COUNTER_AT_LEAST =
            id("entity_counter_at_least");

    static {
        register(ALWAYS, AlwaysCondition.CODEC);
        register(TARGET_ON_FIRE, TargetOnFireCondition.CODEC);
        register(TARGET_ENTITY_TYPE_TAG, TargetEntityTypeTagCondition.CODEC);
        register(DAMAGE_SOURCE_TAG, DamageSourceTagCondition.CODEC);
        register(ATTACKER_HEALTH_BELOW, AttackerHealthBelowCondition.CODEC);
        register(ENTITY_COUNTER_AT_LEAST, EntityCounterAtLeastCondition.CODEC);
    }

    private AffixConditionTypes() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    private static void register(
            Identifier id,
            MapCodec<? extends AffixCondition> codec
    ) {
        CODECS.put(id, codec);
    }

    public static MapCodec<? extends AffixCondition> codec(Identifier id) {
        MapCodec<? extends AffixCondition> codec = CODECS.get(id);

        if (codec == null) {
            throw new IllegalArgumentException(
                    "Unknown DamageNexus affix condition type: " + id
            );
        }

        return codec;
    }
}