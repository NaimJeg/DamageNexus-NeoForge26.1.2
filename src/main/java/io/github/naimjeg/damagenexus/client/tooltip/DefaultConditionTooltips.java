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
                        Component.translatable("condition.damagenexus.always")
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ALL_OF,
                (AllOfCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.all_of")
                                .append(joinConditions(condition.conditions(), mode))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ANY_OF,
                (AnyOfCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.any_of")
                                .append(joinConditions(condition.conditions(), mode))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.NOT,
                (NotCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.not")
                                .append(RuleTooltipDescriptions.describeCondition(
                                        condition.condition(),
                                        mode
                                ))
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.IS_CRITICAL,
                (IsCriticalCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.is_critical")
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_TYPE_IS,
                (DamageTypeIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.damage_type_is",
                                ctx.rawId(condition.damageType())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_TYPE_TAG,
                (DamageTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.damage_type_tag",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_SOURCE_TAG,
                (DamageSourceTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.damage_source_tag",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_CHANNEL_IS,
                (DamageChannelIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.damage_channel_is",
                                ctx.channelName(condition.channelId())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ON_FIRE,
                (TargetOnFireCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.target_on_fire")
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HAS_EFFECT,
                (AttackerHasEffectCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.attacker_has_effect",
                                ctx.effectName(condition.effect())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_HAS_EFFECT,
                (TargetHasEffectCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.target_has_effect",
                                ctx.effectName(condition.effect())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HEALTH_BELOW,
                (AttackerHealthBelowCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.attacker_health_below",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HEALTH_ABOVE,
                (AttackerHealthAboveCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.attacker_health_above",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_HEALTH_BELOW,
                (TargetHealthBelowCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.target_health_below",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_HEALTH_ABOVE,
                (TargetHealthAboveCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.target_health_above",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ENTITY_TYPE_IS,
                (TargetEntityTypeIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.target_entity_type_is",
                                ctx.entityTypeName(condition.entityType())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_ENTITY_TYPE_IS,
                (AttackerEntityTypeIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.attacker_entity_type_is",
                                ctx.entityTypeName(condition.entityType())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ENTITY_TYPE_TAG,
                (TargetEntityTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.target_entity_type_tag",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_ENTITY_TYPE_TAG,
                (AttackerEntityTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.attacker_entity_type_tag",
                                ctx.tagNamePlain(condition.tag())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_IS_BOSS,
                (TargetIsBossCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.target_is_boss")
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_IS_BOSS,
                (AttackerIsBossCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable("condition.damagenexus.attacker_is_boss")
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_MOB_CATEGORY_IS,
                (TargetMobCategoryIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.target_mob_category_is",
                                condition.category().name().toLowerCase(Locale.ROOT)
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_MOB_CATEGORY_IS,
                (AttackerMobCategoryIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatable(
                                "condition.damagenexus.attacker_mob_category_is",
                                condition.category().name().toLowerCase(Locale.ROOT)
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
                result.append(Component.translatable("tooltip.damagenexus.separator.comma"));
            }

            result.append(RuleTooltipDescriptions.describeCondition(
                    conditions.get(i),
                    mode
            ));
        }

        return result;
    }
}
