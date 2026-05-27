package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.api.rule.DamageRuleCondition;
import io.github.naimjeg.damagenexus.builtin.rule.condition.*;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Locale;

public final class DefaultConditionTooltips {

    private DefaultConditionTooltips() {}

    public static void register() {
        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ALWAYS,
                (AlwaysCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.always",
                                "Always:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ALL_OF,
                (AllOfCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                        "condition.damagenexus.all_of",
                                        "When all conditions match: "
                                )
                                .append(joinConditions(condition.conditions(), mode))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ANY_OF,
                (AnyOfCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                        "condition.damagenexus.any_of",
                                        "When any condition matches: "
                                )
                                .append(joinConditions(condition.conditions(), mode))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.NOT,
                (NotCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                        "condition.damagenexus.not",
                                        "Unless "
                                )
                                .append(RuleTooltipDescriptions.describeCondition(
                                        condition.condition(),
                                        mode
                                ))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.IS_CRITICAL,
                (IsCriticalCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.is_critical",
                                "On critical hit:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_TYPE_IS,
                (DamageTypeIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.damage_type_is",
                                "When damage type is " + ctx.rawId(condition.damageType()) + ":",
                                ctx.rawId(condition.damageType())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_TYPE_TAG,
                (DamageTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.damage_type_tag",
                                "When damage type has tag " + ctx.tagNamePlain(condition.tag()) + ":",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_SOURCE_TAG,
                (DamageSourceTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.damage_source_tag",
                                "When damage type has legacy source tag " + ctx.tagNamePlain(condition.tag()) + ":",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_CHANNEL_IS,
                (DamageChannelIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.damage_channel_is",
                                "When active damage includes " + ctx.channelNamePlain(condition.channelId()) + ":",
                                ctx.channelName(condition.channelId())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ON_FIRE,
                (TargetOnFireCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_on_fire",
                                "When the target is burning:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HAS_EFFECT,
                (AttackerHasEffectCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                        "condition.damagenexus.attacker_has_effect",
                                        "When attacker has "
                                )
                                .append(ctx.effectName(condition.effect()))
                                .append(Component.literal(":"))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_HAS_EFFECT,
                (TargetHasEffectCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                        "condition.damagenexus.target_has_effect",
                                        "When target has "
                                )
                                .append(ctx.effectName(condition.effect()))
                                .append(Component.literal(":"))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HEALTH_BELOW,
                (AttackerHealthBelowCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_health_below",
                                "When attacker health is below " + ctx.percent(condition.threshold()) + "%:",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HEALTH_ABOVE,
                (AttackerHealthAboveCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_health_above",
                                "When attacker health is above " + ctx.percent(condition.threshold()) + "%:",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_HEALTH_BELOW,
                (TargetHealthBelowCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_health_below",
                                "When target health is below " + ctx.percent(condition.threshold()) + "%:",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_HEALTH_ABOVE,
                (TargetHealthAboveCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_health_above",
                                "When target health is above " + ctx.percent(condition.threshold()) + "%:",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ENTITY_TYPE_IS,
                (TargetEntityTypeIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_entity_type_is",
                                "Against " + ctx.entityTypeNamePlain(condition.entityType()) + ":",
                                ctx.entityTypeName(condition.entityType())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_ENTITY_TYPE_IS,
                (AttackerEntityTypeIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_entity_type_is",
                                "When attacker is " + ctx.entityTypeNamePlain(condition.entityType()) + ":",
                                ctx.entityTypeName(condition.entityType())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ENTITY_TYPE_TAG,
                (TargetEntityTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_entity_type_tag",
                                "Against targets in " + ctx.tagNamePlain(condition.tag()) + ":",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_ENTITY_TYPE_TAG,
                (AttackerEntityTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_entity_type_tag",
                                "When attacker is in " + ctx.tagNamePlain(condition.tag()) + ":",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_IS_BOSS,
                (TargetIsBossCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_is_boss",
                                "Against bosses:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_IS_BOSS,
                (AttackerIsBossCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_is_boss",
                                "When attacker is a boss:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_MOB_CATEGORY_IS,
                (TargetMobCategoryIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_mob_category_is",
                                "Against " + condition.category().name().toLowerCase(Locale.ROOT) + " entities:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_MOB_CATEGORY_IS,
                (AttackerMobCategoryIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_mob_category_is",
                                "When attacker is a " + condition.category().name().toLowerCase(Locale.ROOT) + " entity:"
                        )
        );
    }

    private static MutableComponent joinConditions(
            List<DamageRuleCondition> conditions,
            RuleTooltipMode mode
    ) {
        MutableComponent result = Component.empty();

        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                result.append(Component.literal(", "));
            }

            result.append(RuleTooltipDescriptions.describeCondition(
                    conditions.get(i),
                    mode
            ));
        }

        return result;
    }
}