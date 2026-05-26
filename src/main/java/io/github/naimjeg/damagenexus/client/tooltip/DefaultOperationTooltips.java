package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import net.minecraft.network.chat.Component;

public final class DefaultOperationTooltips {

    private DefaultOperationTooltips() {}

    public static void register() {
        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_BASE_DAMAGE,
                (AddBaseDamageOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[+] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_base_damage",
                                        "+" + ctx.number(operation.value()) + " " + ctx.channelNamePlain(operation.channel()) + " Damage",
                                        ctx.number(operation.value()),
                                        ctx.channelName(operation.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_base_damage",
                            "+" + ctx.number(operation.value()) + " " + ctx.channelNamePlain(operation.channel()) + " Base Damage",
                            ctx.number(operation.value()),
                            ctx.channelName(operation.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER,
                (AddChannelPreMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[x] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_channel_pre_multiplier",
                                        "+" + ctx.percent(operation.value()) + "% " + ctx.channelNamePlain(operation.channel()) + " Damage",
                                        ctx.percent(operation.value()),
                                        ctx.channelName(operation.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_channel_pre_multiplier",
                            "+" + ctx.percent(operation.value()) + "% " + ctx.channelNamePlain(operation.channel()) + " Pre-Multiplier",
                            ctx.percent(operation.value()),
                            ctx.channelName(operation.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_CHANNEL_POST_MULTIPLIER,
                (AddChannelPostMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[x] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_channel_post_multiplier",
                                        "+" + ctx.percent(operation.value()) + "% " + ctx.channelNamePlain(operation.channel()) + " Damage",
                                        ctx.percent(operation.value()),
                                        ctx.channelName(operation.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_channel_post_multiplier",
                            "+" + ctx.percent(operation.value()) + "% " + ctx.channelNamePlain(operation.channel()) + " Post-Multiplier",
                            ctx.percent(operation.value()),
                            ctx.channelName(operation.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_GLOBAL_POST_MULTIPLIER,
                (AddGlobalPostMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[x] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_global_post_multiplier",
                                        "+" + ctx.percent(operation.value()) + "% Global Damage",
                                        ctx.percent(operation.value())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_global_post_multiplier",
                            "+" + ctx.percent(operation.value()) + "% Global Post-Multiplier",
                            ctx.percent(operation.value())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_TEMPORARY_RESISTANCE,
                (AddTemporaryResistanceOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[+] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_temporary_resistance",
                                        "+" + ctx.number(operation.value()) + " " + ctx.channelNamePlain(operation.channel()) + " Resistance",
                                        ctx.number(operation.value()),
                                        ctx.channelName(operation.channel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_temporary_resistance",
                            "+" + ctx.number(operation.value()) + " " + ctx.channelNamePlain(operation.channel()) + " Temporary Resistance Rating",
                            ctx.number(operation.value()),
                            ctx.channelName(operation.channel())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.OVERRIDE_FINAL_DAMAGE,
                (OverrideFinalDamageOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[!] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.override_final_damage",
                                        "Set Final Damage to " + ctx.number(operation.value()),
                                        ctx.number(operation.value())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.override_final_damage",
                            "Override final damage to " + ctx.number(operation.value()),
                            ctx.number(operation.value())
                    );
                }
        );
    }
}