package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.builtin.rule.condition.*;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleConditionTypes;
import net.minecraft.network.chat.Component;

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
                DamageRuleConditionTypes.TARGET_ON_FIRE,
                (TargetOnFireCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_on_fire",
                                "When the enemy is burning:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.ATTACKER_HEALTH_BELOW,
                (AttackerHealthBelowCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.attacker_health_below",
                                "When your health is below " + ctx.percent(condition.threshold()) + "%:",
                                ctx.percent(condition.threshold())
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.TARGET_ENTITY_TYPE_TAG,
                (TargetEntityTypeTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.target_entity_type_tag",
                                "Against matching targets:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_SOURCE_TAG,
                (DamageSourceTagCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.damage_source_tag",
                                "When the damage source matches:"
                        )
        );

        RuleTooltipDescriptions.registerCondition(
                DamageRuleConditionTypes.DAMAGE_CHANNEL_IS,
                (DamageChannelIsCondition condition, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "condition.damagenexus.damage_channel_is",
                                "When receiving " + ctx.channelNamePlain(condition.channel()) + " damage:",
                                ctx.channelName(condition.channel())
                        )
        );
    }
}