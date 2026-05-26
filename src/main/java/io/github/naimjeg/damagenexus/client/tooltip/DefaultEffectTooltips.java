package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.network.chat.Component;

public final class DefaultEffectTooltips {

    private DefaultEffectTooltips() {}

    public static void register() {
        RuleTooltipDescriptions.registerEffect(
                DamageRuleOperationTypes.ADD_BASE_DAMAGE,
                (AddBaseDamageOperation effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[-] ")
                                .append(Component.translatableWithFallback(
                                        "effect.damagenexus.normal.add_base_damage",
                                        "+" + ctx.number(effect.value()) + " " + ctx.channelNamePlain(effect.channel()) + " Damage",
                                        ctx.number(effect.value()),
                                        ctx.channelName(effect.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_base_damage",
                            "+" + ctx.channelNamePlain(effect.channel()) + " Damage",
                            ctx.channelName(effect.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER,
                (AddChannelPreMultiplierOperation effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[x] ")
                                .append(Component.translatableWithFallback(
                                        "effect.damagenexus.normal.add_channel_pre_multiplier",
                                        "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage",
                                        ctx.percent(effect.value()),
                                        ctx.channelName(effect.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_channel_pre_multiplier",
                            "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage",
                            ctx.percent(effect.value()),
                            ctx.channelName(effect.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                DamageRuleOperationTypes.ADD_CHANNEL_POST_MULTIPLIER,
                (AddChannelPostMultiplierOperation effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.translatableWithFallback(
                                "effect.damagenexus.normal.add_channel_post_multiplier",
                                "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage [x]",
                                ctx.percent(effect.value()),
                                ctx.channelName(effect.channel())
                        );
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_channel_post_multiplier",
                            "+" + ctx.percent(effect.value()) + "% " + ctx.channelNamePlain(effect.channel()) + " Damage",
                            ctx.percent(effect.value()),
                            ctx.channelName(effect.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                DamageRuleOperationTypes.ADD_GLOBAL_POST_MULTIPLIER,
                (AddGlobalPostMultiplierOperation effect, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.translatableWithFallback(
                                "effect.damagenexus.normal.add_global_post_multiplier",
                                "+" + ctx.percent(effect.value()) + "% Damage [x]",
                                ctx.percent(effect.value())
                        );
                    }

                    return Component.translatableWithFallback(
                            "effect.damagenexus.detail.add_global_post_multiplier",
                            "+" + ctx.percent(effect.value()) + "% Damage",
                            ctx.percent(effect.value())
                    );
                }
        );

        RuleTooltipDescriptions.registerEffect(
                DamageRuleOperationTypes.OVERRIDE_FINAL_DAMAGE,
                (OverrideFinalDamageOperation effect, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "effect.damagenexus.override_final_damage",
                                "Set final damage to " + ctx.number(effect.value()),
                                ctx.number(effect.value())
                        )
        );

        RuleTooltipDescriptions.registerEffect(
                DamageRuleOperationTypes.ADD_TEMPORARY_RESISTANCE,
                (AddTemporaryResistanceOperation effect, RuleTooltipContext ctx, RuleTooltipMode mode) ->
                        Component.translatableWithFallback(
                                "effect.damagenexus.add_temporary_resistance",
                                "+" + ctx.number(effect.value()) + " " + ctx.channelNamePlain(effect.channel()) + " Resistance",
                                ctx.number(effect.value()),
                                ctx.channelName(effect.channel())
                        )
        );
    }
}