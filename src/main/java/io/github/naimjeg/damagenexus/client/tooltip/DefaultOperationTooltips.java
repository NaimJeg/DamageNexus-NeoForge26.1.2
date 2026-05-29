package io.github.naimjeg.damagenexus.client.tooltip;

import io.github.naimjeg.damagenexus.builtin.rule.operation.*;
import io.github.naimjeg.damagenexus.registry.rule.DamageRuleOperationTypes;
import io.github.naimjeg.damagenexus.util.IdentifierText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class DefaultOperationTooltips {

    private DefaultOperationTooltips() {}

    public static void register() {
        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_BASE_DAMAGE,
                (AddBaseDamageOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier channelId = operation.channelId();

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal(additiveMarker(operation.value()))
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_base_damage",
                                        ctx.signedNumber(operation.value()) + " "
                                                + ctx.channelNamePlain(channelId)
                                                + " Damage",
                                        ctx.signedNumber(operation.value()),
                                        ctx.channelName(channelId)
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_base_damage",
                            ctx.signedNumber(operation.value()) + " "
                                    + ctx.channelNamePlain(channelId)
                                    + " Base Damage",
                            ctx.signedNumber(operation.value()),
                            ctx.channelName(channelId)
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_CHANNEL_PRE_MULTIPLIER,
                (AddChannelPreMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier channelId = operation.channelId();

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal(preMultiplierMarker())
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_channel_pre_multiplier",
                                        ctx.signedPercent(operation.value()) + " "
                                                + ctx.channelNamePlain(channelId)
                                                + " Damage",
                                        ctx.signedPercent(operation.value()),
                                        ctx.channelName(channelId)
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_channel_pre_multiplier",
                            ctx.signedPercent(operation.value()) + " "
                                    + ctx.channelNamePlain(channelId)
                                    + " Pre-Multiplier",
                            ctx.signedPercent(operation.value()),
                            ctx.channelName(channelId)
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_CHANNEL_POST_MULTIPLIER,
                (AddChannelPostMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier channelId = operation.channelId();

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.translatableWithFallback(
                                "operation.damagenexus.normal.add_channel_post_multiplier",
                                ctx.signedPercent(operation.value()) + " "
                                        + ctx.channelNamePlain(channelId)
                                        + " Damage" + postMultiplierSuffix(),
                                ctx.signedPercent(operation.value()),
                                ctx.channelName(channelId)
                        );
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_channel_post_multiplier",
                            ctx.signedPercent(operation.value()) + " "
                                    + ctx.channelNamePlain(channelId)
                                    + " Post-Multiplier",
                            ctx.signedPercent(operation.value()),
                            ctx.channelName(channelId)
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_GLOBAL_PRE_MULTIPLIER,
                (AddGlobalPreMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    String bucketSuffix = operation.preMultiplierBucketId()
                            .map(id -> " [" + IdentifierText.path(id) + "]")
                            .orElse("");

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal(preMultiplierMarker())
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_global_pre_multiplier",
                                        ctx.signedPercent(operation.value()) + " Global Damage",
                                        ctx.signedPercent(operation.value())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_global_pre_multiplier",
                            ctx.signedPercent(operation.value()) + " Global Pre-Multiplier" + bucketSuffix,
                            ctx.signedPercent(operation.value()),
                            bucketSuffix
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_GLOBAL_POST_MULTIPLIER,
                (AddGlobalPostMultiplierOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.translatableWithFallback(
                                "operation.damagenexus.normal.add_global_post_multiplier",
                                ctx.signedPercent(operation.value()) + " Global Damage" + postMultiplierSuffix(),
                                ctx.signedPercent(operation.value())
                        );
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_global_post_multiplier",
                            ctx.signedPercent(operation.value()) + " Global Post-Multiplier",
                            ctx.signedPercent(operation.value())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_TEMPORARY_RESISTANCE,
                (AddTemporaryResistanceOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier channelId = operation.channelId();

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[+] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_temporary_resistance",
                                        ctx.signedNumber(operation.value()) + " " + ctx.channelNamePlain(channelId) + " Resistance",
                                        ctx.number(operation.value()),
                                        ctx.channelName(channelId)
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_temporary_resistance",
                            ctx.signedNumber(operation.value()) + " " + ctx.channelNamePlain(channelId) + " Temporary Resistance Rating",
                            ctx.number(operation.value()),
                            ctx.channelName(channelId)
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.CONVERT_DAMAGE,
                (ConvertDamageOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier from = operation.fromChannel();
                    Identifier to = operation.toChannel();

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[↔] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.convert_damage",
                                        "Convert " + ctx.percentWithSymbol(operation.ratio())
                                                + " " + ctx.channelNamePlain(operation.fromChannel())
                                                + " to " + ctx.channelNamePlain(operation.toChannel()),
                                        ctx.percentWithSymbol(operation.ratio()),
                                        ctx.channelName(operation.fromChannel()),
                                        ctx.channelName(operation.toChannel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.convert_damage",
                            "Convert " + ctx.percentWithSymbol(operation.ratio())
                                    + " of current base "
                                    + ctx.channelNamePlain(operation.fromChannel())
                                    + " damage into "
                                    + ctx.channelNamePlain(operation.toChannel())
                                    + " before channel multipliers.",
                            ctx.percentWithSymbol(operation.ratio()),
                            ctx.channelName(operation.fromChannel()),
                            ctx.channelName(operation.toChannel())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.GAIN_EXTRA_DAMAGE,
                (GainExtraDamageOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier basedOn = operation.basedOnChannel();
                    Identifier to = operation.toChannel();

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal("[+] ")
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.gain_extra_damage",
                                        "Gain " + ctx.percentWithSymbol(operation.ratio())
                                                + " of " + ctx.channelNamePlain(operation.basedOnChannel())
                                                + " as " + ctx.channelNamePlain(operation.toChannel()),
                                        ctx.percentWithSymbol(operation.ratio()),
                                        ctx.channelName(operation.basedOnChannel()),
                                        ctx.channelName(operation.toChannel())
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.gain_extra_damage",
                            "Gain extra " + ctx.channelNamePlain(operation.toChannel())
                                    + " damage equal to "
                                    + ctx.percentWithSymbol(operation.ratio())
                                    + " of current base "
                                    + ctx.channelNamePlain(operation.basedOnChannel())
                                    + " damage. This does not remove the original damage.",
                            ctx.channelName(operation.toChannel()),
                            ctx.percentWithSymbol(operation.ratio()),
                            ctx.channelName(operation.basedOnChannel())
                    );
                }
        );

        RuleTooltipDescriptions.registerOperation(
                DamageRuleOperationTypes.ADD_CHANNEL_MITIGATION,
                (AddChannelMitigationOperation operation, RuleTooltipContext ctx, RuleTooltipMode mode) -> {
                    Identifier channelId = operation.channelId();
                    String valueText = ctx.signedPercent(operation.value());

                    if (mode == RuleTooltipMode.NORMAL) {
                        return Component.literal(mitigationMarker(operation.value()))
                                .append(Component.translatableWithFallback(
                                        "operation.damagenexus.normal.add_channel_mitigation",
                                        valueText + " " + ctx.channelNamePlain(channelId) + " Mitigation",
                                        valueText,
                                        ctx.channelName(channelId)
                                ));
                    }

                    return Component.translatableWithFallback(
                            "operation.damagenexus.detail.add_channel_mitigation",
                            valueText + " " + ctx.channelNamePlain(channelId) + " Channel Mitigation",
                            valueText,
                            ctx.channelName(channelId)
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

    private static String additiveMarker(float value) {
        return value < 0.0f ? "[-] " : "[+] ";
    }

    private static String mitigationMarker(float value) { return value < 0.0f ? "[+] " : "[-] "; }

    private static String preMultiplierMarker() {
        return "[x] ";
    }

    private static String postMultiplierSuffix() {
        return " [x]";
    }
}