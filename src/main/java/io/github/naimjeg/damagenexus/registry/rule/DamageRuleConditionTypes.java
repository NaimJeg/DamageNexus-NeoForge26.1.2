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


    public static final Identifier DAMAGE_SOURCE_TAG = id("damage_source_tag"); // legacy alias
    public static final Identifier DAMAGE_TYPE_TAG = id("damage_type_tag");
    public static final Identifier DAMAGE_TYPE_IS = id("damage_type_is");

    public static final Identifier ATTACKER_HAS_EFFECT = id("attacker_has_effect");
    public static final Identifier TARGET_HAS_EFFECT = id("target_has_effect");

    public static final Identifier DAMAGE_CHANNEL_IS = id("damage_channel_is");
    public static final Identifier ALWAYS = id("always");
    public static final Identifier ALL_OF = id("all_of");
    public static final Identifier ANY_OF = id("any_of");
    public static final Identifier NOT = id("not");
    public static final Identifier IS_CRITICAL = id("is_critical");
    public static final Identifier TARGET_ON_FIRE = id("target_on_fire");
    public static final Identifier TARGET_ENTITY_TYPE_TAG = id("target_entity_type_tag");
    public static final Identifier ATTACKER_HEALTH_BELOW = id("attacker_health_below");
    public static final Identifier TARGET_HEALTH_BELOW = id("target_health_below");
    public static final Identifier ATTACKER_HEALTH_ABOVE = id("attacker_health_above");
    public static final Identifier TARGET_HEALTH_ABOVE = id("target_health_above");

    static {
        register(ALWAYS, AlwaysCondition.CODEC);
        register(ALL_OF, AllOfCondition.CODEC);
        register(ANY_OF, AnyOfCondition.CODEC);
        register(NOT, NotCondition.CODEC);
        register(IS_CRITICAL, IsCriticalCondition.CODEC);

        register(TARGET_ON_FIRE, TargetOnFireCondition.CODEC);
        register(TARGET_ENTITY_TYPE_TAG, TargetEntityTypeTagCondition.CODEC);

        register(DAMAGE_SOURCE_TAG, DamageSourceTagCondition.CODEC); // deprecated alias
        register(DAMAGE_TYPE_TAG, DamageTypeTagCondition.CODEC);
        register(DAMAGE_TYPE_IS, DamageTypeIsCondition.CODEC);

        register(ATTACKER_HEALTH_BELOW, AttackerHealthBelowCondition.CODEC);
        register(ATTACKER_HEALTH_ABOVE, AttackerHealthAboveCondition.CODEC);
        register(TARGET_HEALTH_BELOW, TargetHealthBelowCondition.CODEC);
        register(TARGET_HEALTH_ABOVE, TargetHealthAboveCondition.CODEC);
        register(ATTACKER_HAS_EFFECT, AttackerHasEffectCondition.CODEC);
        register(TARGET_HAS_EFFECT, TargetHasEffectCondition.CODEC);
        register(DAMAGE_CHANNEL_IS, DamageChannelIsCondition.CODEC);
    }

    private DamageRuleConditionTypes() {}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DamageNexus.MODID, path);
    }

    public static void register(
            Identifier id,
            MapCodec<? extends DamageRuleCondition> codec
    ) {
        if (CODECS.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Duplicate DamageNexus rule condition type: " + id
            );
        }

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